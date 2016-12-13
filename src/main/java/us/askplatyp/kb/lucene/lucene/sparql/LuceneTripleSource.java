/*
 * Copyright (c) 2016 Platypus Knowledge Base developers.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package us.askplatyp.kb.lucene.lucene.sparql;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.common.iteration.CloseableIteratorIteration;
import org.eclipse.rdf4j.common.iteration.EmptyIteration;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.algebra.evaluation.TripleSource;
import us.askplatyp.kb.lucene.lucene.LuceneIndex;
import us.askplatyp.kb.lucene.model.DatatypeProperty;
import us.askplatyp.kb.lucene.model.Namespaces;
import us.askplatyp.kb.lucene.model.ObjectProperty;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;

/**
 * @author Thomas Pellissier Tanon
 */
public class LuceneTripleSource implements TripleSource {

    private static final int QUERY_LOAD_SIZE = 1000;
    private static final ValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();
    private static DatatypeFactory DATATYPE_FACTORY;

    static {
        try {
            DATATYPE_FACTORY = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private LuceneIndex index;

    public LuceneTripleSource(LuceneIndex index) {
        this.index = index;
    }

    @Override
    public ValueFactory getValueFactory() {
        return VALUE_FACTORY;
    }

    @Override
    public CloseableIteration<Statement, QueryEvaluationException> getStatements(
            Resource subj, IRI pred, Value obj, Resource... contexts
    ) throws QueryEvaluationException {
        if (contexts.length != 0) {
            throw new QueryEvaluationException("Contexts are not supported");
        }

        try {
            return dispatchGetStatements(subj, pred, obj);
        } catch (IOException e) {
            throw new QueryEvaluationException(e);
        }
    }

    private CloseableIteration<Statement, QueryEvaluationException> dispatchGetStatements(Resource subj, IRI pred, Value obj)
            throws QueryEvaluationException, IOException {
        if (subj == null) {
            if (pred == null) {
                if (obj == null) {
                    return getAllStatements();
                } else {
                    throw new QueryEvaluationException("Could not retrieve statements from object only"); //TODO
                }
            } else {
                if (obj == null) {
                    return getStatementsForPredicate(pred);
                } else {
                    return getStatementsForPredicateObject(pred, obj);
                }
            }
        } else {
            if (pred == null) {
                if (obj == null) {
                    return getStatementsForSubject(subj);
                } else {
                    throw new QueryEvaluationException("Could not retrieve statements with subject and object only"); //TODO: do filter?
                }
            } else {
                if (obj == null) {
                    return getStatementsForSubjectPredicate(subj, pred);
                } else {
                    return getStatementsForSubjectPredicateObject(subj, pred, obj);
                }
            }
        }
    }

    private CloseableIteration<Statement, QueryEvaluationException> getStatementsForSubject(Resource subj)
            throws IOException {
        try (LuceneIndex.Reader reader = index.getReader()) {
            return reader.getDocumentForIRI(Namespaces.reduce(subj.stringValue())).map(document ->
                    (CloseableIteration<Statement, QueryEvaluationException>) new CloseableIteratorIteration<Statement, QueryEvaluationException>(
                            document.getFields().stream()
                                    .filter(field -> !field.name().equals("@id"))
                                    .map(field -> formatField(subj, field))
                                    .iterator()
                    )
            ).orElse(new EmptyIteration<>());
        }
    }

    private CloseableIteration<Statement, QueryEvaluationException> getStatementsForSubjectPredicate(Resource subj, IRI pred)
            throws IOException {
        try (LuceneIndex.Reader reader = index.getReader()) {
            //TODO: selective load
            return reader.getDocumentForIRI(Namespaces.reduce(subj.stringValue())).map(document ->
                    (CloseableIteration<Statement, QueryEvaluationException>) new CloseableIteratorIteration<Statement, QueryEvaluationException>(
                            Arrays.stream(document.getFields(parsePropertyName(pred))).map(field -> formatField(subj, field)).iterator()
                    )
            ).orElse(new EmptyIteration<>());
        }
    }

    private CloseableIteration<Statement, QueryEvaluationException> getStatementsForSubjectPredicateObject(Resource subj, IRI pred, Value obj)
            throws IOException {
        Statement inputStatement = VALUE_FACTORY.createStatement(subj, pred, obj);
        try (LuceneIndex.Reader reader = index.getReader()) {
            //TODO: selective load
            return reader.getDocumentForIRI(Namespaces.reduce(subj.stringValue())).map(document ->
                    (CloseableIteration<Statement, QueryEvaluationException>) new CloseableIteratorIteration<Statement, QueryEvaluationException>(
                            Arrays.stream(document.getFields(parsePropertyName(pred)))
                                    .map(field -> formatField(subj, field))
                                    .filter(statement -> statement.equals(inputStatement))
                                    .iterator()
                    )
            ).orElse(new EmptyIteration<>());
        }
    }

    private CloseableIteration<Statement, QueryEvaluationException> getStatementsForPredicateObject(IRI pred, Value obj) throws IOException {
        return new QueryIteration(
                index.getReader(),
                new TermQuery(termForPredicateObject(pred, obj)), //TODO: better query?
                document -> Iterators.singletonIterator(VALUE_FACTORY.createStatement(getIRIFromDocument(document), pred, obj)),
                Collections.singleton("@id")
        );
    }

    private CloseableIteration<Statement, QueryEvaluationException> getStatementsForPredicate(IRI pred) throws IOException {
        return new QueryIteration(
                index.getReader(),
                new FieldValueQuery(parsePropertyName(pred)),
                document -> Arrays.stream(document.getFields(parsePropertyName(pred)))
                        .map(field -> formatField(getIRIFromDocument(document), field))
                        .iterator(),
                Sets.newHashSet("@id", parsePropertyName(pred))
        );
    }

    private CloseableIteration<Statement, QueryEvaluationException> getAllStatements() throws IOException {
        return new QueryIteration(
                index.getReader(),
                new MatchAllDocsQuery(),
                document -> document.getFields().stream()
                        .filter(field -> !field.name().equals("@id"))
                        .map(field -> formatField(getIRIFromDocument(document), field))
                        .iterator(),
                null
        );
    }

    private Term termForPredicateObject(IRI predicate, Value object) {
        String key = Namespaces.reduce(predicate.stringValue());
        String value = "";
        if (object instanceof IRI) {
            value = Namespaces.reduce(object.stringValue());
        } else if (object instanceof Literal) {
            Literal literal = (Literal) object;
            if (literal.getLanguage().isPresent()) {
                key += "@" + literal.getLanguage().get();
            }
            value = literal.getLabel();
        } else {
            throw new IllegalArgumentException("Value of unknown type: " + value);
        }
        return new Term(key, value);
    }

    private String parsePropertyName(IRI propertyIRI) {
        return Namespaces.reduce(propertyIRI.stringValue());
    }

    private Statement formatField(Resource subject, IndexableField field) {
        return VALUE_FACTORY.createStatement(subject, formatPropertyName(field.name()), fieldToValue(field));
    }

    private Value fieldToValue(IndexableField field) {
        String propertyName = normalizePropertyName(field.name());
        if (propertyName.equals("@type")) {
            return VALUE_FACTORY.createIRI(Namespaces.expand(field.stringValue()));
        }
        for (ObjectProperty property : ObjectProperty.PROPERTIES) {
            if (propertyName.equals(property.getLabel())) {
                return VALUE_FACTORY.createIRI(Namespaces.expand(field.stringValue()));
            }
        }
        for (DatatypeProperty property : DatatypeProperty.PROPERTIES) {
            if (propertyName.equals(property.getLabel())) {
                switch (property.getRange()) {
                    case STRING:
                        return VALUE_FACTORY.createLiteral(field.stringValue());
                    case LANGUAGE_TAGGED_STRING:
                        return VALUE_FACTORY.createLiteral(field.stringValue(), field.name().split("@")[1]);
                    case CALENDAR:
                        return VALUE_FACTORY.createLiteral(DATATYPE_FACTORY.newXMLGregorianCalendar(field.stringValue()));
                    //TODO: other types
                }
            }
        }
        throw new QueryEvaluationException("Unsupported field " + field.name());
    }

    private IRI formatPropertyName(String name) {
        return VALUE_FACTORY.createIRI(Namespaces.expand(normalizePropertyName(name)));
    }

    private String normalizePropertyName(String name) {
        if (name.lastIndexOf("@") > 0) {
            name = name.split("@")[0];
        }
        return name;
    }

    private Resource getIRIFromDocument(Document document) {
        String id = document.get("@id");
        if (id == null) {
            return VALUE_FACTORY.createBNode(); //TODO: same BNode for same document?
        } else {
            return VALUE_FACTORY.createIRI(Namespaces.expand(id));
        }
    }

    private static class QueryIteration implements CloseableIteration<Statement, QueryEvaluationException> {

        LuceneIndex.Reader reader;
        Query query;
        Function<Document, Iterator<Statement>> statementsFromDocument;
        Set<String> fieldsToLoad;
        ScoreDoc[] scoreDocs;
        int currentPointer = 0;
        Iterator<Statement> currentStatements = Collections.emptyIterator();

        QueryIteration(LuceneIndex.Reader reader, Query query, Function<Document, Iterator<Statement>> statementsFromDocument, Set<String> fieldsToLoad) {
            this.reader = reader;
            this.query = query;
            this.statementsFromDocument = statementsFromDocument;
            this.fieldsToLoad = fieldsToLoad;
            doSearch(null);
        }

        @Override
        public void close() throws QueryEvaluationException {
            try {
                reader.close();
            } catch (IOException e) {
                throw new QueryEvaluationException(e);
            }
        }

        @Override
        public boolean hasNext() throws QueryEvaluationException {
            while (!currentStatements.hasNext()) {
                // Case of next batch from query
                if (currentPointer == scoreDocs.length && scoreDocs.length == QUERY_LOAD_SIZE) {
                    doSearch(scoreDocs[currentPointer - 1]);
                }
                //No more documents
                if (currentPointer >= scoreDocs.length) {
                    return false;
                }
                retrieveNextStatements();
            }
            return true;
        }

        @Override
        public Statement next() throws QueryEvaluationException {
            if (hasNext()) {
                return currentStatements.next();
            }
            throw new QueryEvaluationException("No more results");
        }

        @Override
        public void remove() throws QueryEvaluationException {
            throw new IllegalArgumentException("The remove method is not implemented");
        }

        private void doSearch(ScoreDoc after) {
            try {
                scoreDocs = reader.searchAfter(after, query, QUERY_LOAD_SIZE).scoreDocs;
                currentPointer = 0;
            } catch (IOException e) {
                throw new QueryEvaluationException(e);
            }
        }

        private void retrieveNextStatements() {
            try {
                currentStatements = statementsFromDocument.apply(reader.getDocumentForDocId(scoreDocs[currentPointer].doc, fieldsToLoad));
                currentPointer++;
            } catch (IOException e) {
                throw new QueryEvaluationException(e);
            }
        }
    }
}
