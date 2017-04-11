/*
 * Copyright (c) 2017 Platypus Knowledge Base developers.
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

package us.askplatyp.kb.lucene.model;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.util.RDFCollections;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Thomas Pellissier Tanon
 */
public class Schema {

    private static final Logger LOGGER = LoggerFactory.getLogger(Schema.class);
    private static Schema SCHEMA;
    private Model model;
    private Map<String, Class> classes = new HashMap<>();
    private Map<String, Property> properties = new HashMap<>();

    private Schema(Model model) {
        this.model = model;
        for (Resource subject : model.subjects()) {
            if (model.contains(subject, RDF.TYPE, RDFS.CLASS)) {
                classes.put(subject.toString(), new Class(subject));
            } else if (model.contains(subject, RDF.TYPE, OWL.OBJECTPROPERTY)) {
                properties.put(subject.toString(), new ObjectProperty(subject));
            } else if (model.contains(subject, RDF.TYPE, OWL.DATATYPEPROPERTY)) {
                properties.put(subject.toString(), new DatatypeProperty(subject));
            }
        }
    }

    public static Schema getSchema() {
        if (SCHEMA == null) {
            try {
                RDFParser rdfParser = Rio.createParser(RDFFormat.TURTLE);
                Model model = new LinkedHashModel();
                rdfParser.setRDFHandler(new StatementCollector(model));
                try (InputStream inputStream = Schema.class.getResourceAsStream("/schema.ttl")) {
                    rdfParser.parse(inputStream, "http://schema.org/");
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
                SCHEMA = new Schema(model);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return SCHEMA;
    }

    private Stream<Class> getPropertyClasses(Resource subject, IRI predicate) {
        return Models.getPropertyIRIs(model, subject, predicate).stream()
                .map(iri -> Schema.this.getClass(iri.toString()));
    }

    public Class getClass(String URI) {
        URI = Namespaces.expand(URI);
        Class result = classes.get(Namespaces.expand(URI));
        if (result == null) {
            result = new Class(SimpleValueFactory.getInstance().createIRI(URI));
        }
        return result;
    }

    public Optional<Property> getProperty(String URI) {
        return Optional.ofNullable(properties.get(Namespaces.expand(URI)));
    }

    public Stream<Property> getProperties() {
        return properties.values().stream();
    }

    public enum Datatype {STRING, LANGUAGE_TAGGED_STRING, CALENDAR}

    private abstract class SchemaResource {
        protected Resource iri;

        private SchemaResource(Resource iri) {
            this.iri = iri;
        }

        public String getLabel() {
            return Models.getPropertyString(model, iri, RDFS.LABEL).orElseThrow(() ->
                    new IllegalArgumentException("Schema resource without a rdfs:label: " + iri.toString())
            );
        }

        public String getDescription() {
            return Models.getPropertyString(model, iri, RDFS.COMMENT).orElseThrow(() ->
                    new IllegalArgumentException("Schema resource without a rdfs:comment: " + iri.toString())
            );
        }

        public String getShortURI() {
            return Namespaces.reduce(iri.stringValue());
        }
    }

    public class Class extends SchemaResource {
        private Class(Resource iri) {
            super(iri);
        }

        public boolean isSubClassOf(Class other) {
            return this.equals(other) || getPropertyClasses(iri, RDFS.SUBCLASSOF).anyMatch(superClass -> superClass.isSubClassOf(other));
        }
    }

    public abstract class Property extends SchemaResource {
        private Property(Resource iri) {
            super(iri);
        }

        public Stream<Class> getDomains() {
            return Models.getPropertyResources(model, iri, RDFS.DOMAIN).stream().flatMap(this::expandClass);
        }

        Stream<Class> expandClass(Resource resource) {
            if (resource instanceof IRI) {
                return Stream.of(Schema.this.getClass(resource.toString()));
            } else {
                return Models.getPropertyIRIs(model, resource, OWL.UNIONOF).stream()
                        .flatMap(collection -> RDFCollections.asValues(model, resource, new ArrayList<>()).stream())
                        .map(iri -> Schema.this.getClass(iri.toString()));
            }
        }

        public boolean isFunctionalProperty() {
            return model.contains(iri, RDF.TYPE, OWL.FUNCTIONALPROPERTY);
        }
    }

    public class ObjectProperty extends Property {
        private ObjectProperty(Resource iri) {
            super(iri);
        }

        public Stream<Class> getRanges() {
            return Models.getPropertyResources(model, iri, RDFS.RANGE).stream().flatMap(this::expandClass);
        }
    }

    public class DatatypeProperty extends Property {
        private DatatypeProperty(Resource iri) {
            super(iri);
        }

        public Datatype getRange() {
            return Models.getPropertyIRIs(model, iri, RDFS.RANGE).stream().flatMap(datatype -> {
                if (datatype.equals(XMLSchema.STRING) || datatype.equals(XMLSchema.ANYURI)) {
                    return Stream.of(Datatype.STRING);
                } else if (datatype.equals(RDF.LANGSTRING)) {
                    return Stream.of(Datatype.LANGUAGE_TAGGED_STRING);
                } else if (datatype.equals(RDFS.LITERAL)) {
                    return Stream.of(Datatype.CALENDAR); //TODO: improve
                } else {
                    return Stream.empty(); //TODO
                }
            }).findAny().orElseThrow(() ->
                    new IllegalArgumentException("Schema datatype property without a rdfs:range: " + iri.toString())
            );
        }
    }
}
