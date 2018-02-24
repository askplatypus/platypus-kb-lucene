/*
 * Copyright (c) 2018 Platypus Knowledge Base developers.
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
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.algebra.evaluation.TripleSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.askplatyp.kb.lucene.Configuration;
import us.askplatyp.kb.lucene.lucene.LuceneIndex;
import us.askplatyp.kb.lucene.model.Namespaces;
import us.askplatyp.kb.lucene.model.Schema;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author Thomas Pellissier Tanon
 */
public class LuceneTripleSource implements TripleSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(LuceneTripleSource.class);
    private static final int QUERY_LOAD_SIZE = 262144;
    private static final ValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();
    private static final Schema SCHEMA = Schema.getSchema();
    private static final Set<IRI> TOP_INDIVIDUAL_CLASSES = Sets.newHashSet(
            OWL.THING, OWL.INDIVIDUAL, VALUE_FACTORY.createIRI("http://schema.org/Thing")
    );
    private static final DatatypeFactory DATATYPE_FACTORY;

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
                    throw new QueryEvaluationException("Could not retrieve statements from predicate only"); //TODO
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
            return reader.getDocumentForTerm(new Term("@id", Namespaces.reduce(subj.stringValue()))).map(document ->
                    (CloseableIteration<Statement, QueryEvaluationException>) new CloseableIteratorIteration<Statement, QueryEvaluationException>(
                            document.getFields().stream()
                                    .filter(field -> !field.name().equals("@id"))
                                    .flatMap(field -> formatField(subj, field))
                                    .iterator()
                    )
            ).orElse(new EmptyIteration<>());
        }
    }

    private CloseableIteration<Statement, QueryEvaluationException> getStatementsForSubjectPredicate(Resource subj, IRI pred)
            throws IOException {
        try (LuceneIndex.Reader reader = index.getReader()) {
            //TODO: selective load
            return reader.getDocumentForTerm(new Term("@id", Namespaces.reduce(subj.stringValue()))).map(document ->
                    (CloseableIteration<Statement, QueryEvaluationException>) new CloseableIteratorIteration<Statement, QueryEvaluationException>(
                            statementFromDocumentSubjectProperty(document, subj, pred).iterator()
                    )
            ).orElse(new EmptyIteration<>());
        }
    }

    private CloseableIteration<Statement, QueryEvaluationException> getStatementsForSubjectPredicateObject(Resource subj, IRI pred, Value obj)
            throws IOException {
        Statement inputStatement = VALUE_FACTORY.createStatement(subj, pred, obj);
        try (LuceneIndex.Reader reader = index.getReader()) {
            //TODO: selective load
            return reader.getDocumentForTerm(new Term("@id", Namespaces.reduce(subj.stringValue()))).map(document ->
                    (CloseableIteration<Statement, QueryEvaluationException>) new CloseableIteratorIteration<Statement, QueryEvaluationException>(
                            statementFromDocumentSubjectProperty(document, subj, pred)
                                    .filter(statement -> statement.equals(inputStatement))
                                    .iterator()
                    )
            ).orElse(new EmptyIteration<>());
        }
    }

    private CloseableIteration<Statement, QueryEvaluationException> getStatementsForPredicateObject(IRI pred, Value obj) throws IOException {
        Query query = (pred.equals(RDF.TYPE) && TOP_INDIVIDUAL_CLASSES.contains(obj))
                ? new MatchAllDocsQuery()
                : new TermQuery(termForPredicateObject(pred, obj));
        return new QueryIteration(
                index.getReader(),
                query,
                document -> Iterators.singletonIterator(VALUE_FACTORY.createStatement(getIRIFromDocument(document), pred, obj)),
                Collections.singleton("@id")
        );
    }

    private CloseableIteration<Statement, QueryEvaluationException> getStatementsForPredicate(IRI pred) throws IOException {
        //TODO: does not seems to work
        BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
        fieldsFromPropertyIRI(pred).forEach(field ->
                booleanQueryBuilder.add(new BooleanClause(new DocValuesFieldExistsQuery(field), BooleanClause.Occur.SHOULD))
        );
        return new QueryIteration(
                index.getReader(),
                booleanQueryBuilder.build(),
                document -> {
                    System.out.print("fo");
                    return statementFromDocumentSubjectProperty(document, getIRIFromDocument(document), pred).iterator();
                },
                null
        );
    }

    private CloseableIteration<Statement, QueryEvaluationException> getAllStatements() throws IOException {
        return new QueryIteration(
                index.getReader(),
                new MatchAllDocsQuery(),
                document -> document.getFields().stream()
                        .filter(field -> !field.name().equals("@id"))
                        .flatMap(field -> formatField(getIRIFromDocument(document), field))
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

    private Stream<Statement> statementFromDocumentSubjectProperty(Document document, Resource subject, IRI propertyIRI) {
        return fieldsFromPropertyIRI(propertyIRI)
                .flatMap(field -> Arrays.stream(document.getValues(field)).flatMap(value -> formatField(subject, field, value)));
    }

    private Stream<String> fieldsFromPropertyIRI(IRI propertyIRI) {
        String name = Namespaces.reduce(propertyIRI.stringValue());
        return SCHEMA.getProperty(name).map(property -> {
            if (property.getSimpleRange() == Schema.Range.LOCAL_STRING) {
                return Arrays.stream(Configuration.SUPPORTED_LOCALES)
                        .map(locale -> name + "@" + locale.getLanguage());
            } else {
                return Stream.of(name);
            }
        }).orElse(Stream.of(name));
    }

    private Stream<Statement> formatField(Resource subject, IndexableField field) {
        return formatField(subject, field.name(), field.stringValue());
    }

    private Stream<Statement> formatField(Resource subject, String fieldName, String fieldValue) {
        return fieldToValue(fieldName, fieldValue).map(object ->
                VALUE_FACTORY.createStatement(subject, formatPropertyName(fieldName), object)
        );
    }

    private Stream<Value> fieldToValue(String name, String value) {
        String propertyName = normalizePropertyName(name);
        if (propertyName.equals("@type")) {
            return Stream.of(VALUE_FACTORY.createIRI(Namespaces.expand(value)));
        }
        return SCHEMA.getProperty(propertyName).flatMap(property -> {
            switch (property.getSimpleRange()) {
                case CALENDAR:
                    return Optional.of(VALUE_FACTORY.createLiteral(DATATYPE_FACTORY.newXMLGregorianCalendar(value)));
                case INTEGER:
                    return Optional.of(VALUE_FACTORY.createLiteral(value, XMLSchema.INTEGER));
                case LOCAL_STRING:
                    return Optional.of(VALUE_FACTORY.createLiteral(value, name.split("@")[1]));
                case CONSTANT:
                case RESOURCE:
                    return Optional.of(VALUE_FACTORY.createIRI(Namespaces.expand(value)));
                case STRING:
                    return Optional.of(VALUE_FACTORY.createLiteral(value));
                case IRI:
                    return Optional.of(VALUE_FACTORY.createLiteral(value, XMLSchema.ANYURI));
                default:
                    //TODO: add missing fields
                    LOGGER.warn("Unsupported simple range type: " + property.getSimpleRange().toString());
                    return Optional.empty();
            }
        }).map(Stream::of).orElseGet(() -> {
            LOGGER.info("Unsupported field " + name);
            return Stream.empty();
        });
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
