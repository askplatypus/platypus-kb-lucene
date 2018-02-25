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

package us.askplatyp.kb.lucene.model;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.vocab.XSDVocabulary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Thomas Pellissier Tanon
 */
public class Schema {

    private static final IRI ONTOLOGY_IRI = IRI.create("http://kb.askplatyp.us/api/v1/schema");

    private static final Logger LOGGER = LoggerFactory.getLogger(Schema.class);
    private static final OWLDataFactory DATA_FACTORY = OWLManager.getOWLDataFactory();
    private static final OWLDataRange CALENDAR_DATARANGE = DATA_FACTORY.getOWLDataUnionOf(
            DATA_FACTORY.getOWLDatatype(XSDVocabulary.DATE_TIME),
            DATA_FACTORY.getOWLDatatype(XSDVocabulary.DATE),
            DATA_FACTORY.getOWLDatatype(XSDVocabulary.G_YEAR_MONTH),
            DATA_FACTORY.getOWLDatatype(XSDVocabulary.G_YEAR)
    );
    private static final OWLDatatype INTEGER_DATARANGE = DATA_FACTORY.getIntegerOWLDatatype();
    private static final OWLDatatype LOCAL_STRING_DATARANGE = DATA_FACTORY.getOWLDatatype(
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#langString"
    );
    private static final OWLDatatype STRING_DATARANGE = DATA_FACTORY.getStringOWLDatatype();
    private static final OWLClassExpression GEO_CLASS = DATA_FACTORY.getOWLObjectUnionOf(
            DATA_FACTORY.getOWLClass("http://schema.org/GeoCoordinates"),
            DATA_FACTORY.getOWLClass("http://schema.org/GeoShape")
    );
    private static final OWLClassExpression ENUMERATION_CLASS = DATA_FACTORY.getOWLClass("http://schema.org/Enumeration");
    private static final OWLDatatype ANY_URI_DATARANGE = DATA_FACTORY.getOWLDatatype(XSDVocabulary.ANY_URI);
    private OWLOntology ontology;

    private static Schema SCHEMA;
    private OWLReasoner reasoner;
    private Map<String, Property> properties = new HashMap<>();

    private Schema(OWLOntology ontology) {
        this.ontology = ontology;
        reasoner = (new StructuralReasonerFactory()).createReasoner(ontology);
        loadProperties();
    }

    public static Schema getSchema() {
        if (SCHEMA == null) {
            SCHEMA = new Schema(loadOntology());
        }
        return SCHEMA;
    }

    private static OWLOntology loadOntology() {
        OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
        try {
            ontologyManager.loadOntologyFromOntologyDocument(Schema.class.getResourceAsStream("/schema.ttl"));
        } catch (OWLOntologyCreationException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ontologyManager.getOntology(ONTOLOGY_IRI);
    }

    private void loadProperties() {
        ontology.dataPropertiesInSignature().map(DataProperty::new)
                .forEach(property -> properties.put(property.getIRI(), property));
        ontology.objectPropertiesInSignature().map(ObjectProperty::new)
                .forEach(property -> properties.put(property.getIRI(), property));
    }

    public Optional<Property> getProperty(String propertyIRI) {
        return Optional.ofNullable(properties.get(propertyIRI));
    }

    public Stream<Property> getProperties() {
        return properties.values().stream();
    }

    public enum Range {
        CALENDAR,
        CONSTANT,
        INTEGER,
        GEO,
        LOCAL_STRING,
        RESOURCE,
        STRING,
        IRI
    }

    public interface Property {
        String getIRI();

        boolean isFunctionalProperty();

        Range getSimpleRange();
    }

    public class Class {
        private OWLClass self;

        private Class(OWLClass self) {
            this.self = self;
        }

        public String getIRI() {
            return Namespaces.reduce(self.getIRI().getIRIString());
        }

        public boolean isSubClassOf(Class other) {
            return reasoner.getSubClasses(other.self).containsEntity(self);
        }
    }

    private class ObjectProperty implements Property {
        private OWLObjectProperty self;

        private ObjectProperty(OWLObjectProperty self) {
            this.self = self;
        }

        @Override
        public String getIRI() {
            return Namespaces.reduce(self.getIRI().getIRIString());
        }

        @Override
        public Range getSimpleRange() {
            OWLClassExpression range = ontology.objectPropertyRangeAxioms(self)
                    .map(OWLObjectPropertyRangeAxiom::getRange)
                    .findAny().orElseGet(() -> {
                        LOGGER.info("The object property " + self.toStringID() + " has no range in the schema");
                        return DATA_FACTORY.getOWLThing();
                    });
            if (range.equals(GEO_CLASS)) {
                return Range.GEO;
            } else if (reasoner.superClasses(range).anyMatch(ENUMERATION_CLASS::equals)) {
                return Range.CONSTANT;
            } else {
                return Range.RESOURCE;
            }
        }

        @Override
        public boolean isFunctionalProperty() {
            return ontology.functionalObjectPropertyAxioms(self).count() > 0;
        }
    }

    private class DataProperty implements Property {
        private OWLDataProperty self;

        private DataProperty(OWLDataProperty self) {
            this.self = self;
        }

        @Override
        public String getIRI() {
            return Namespaces.reduce(self.getIRI().getIRIString());
        }

        @Override
        public Range getSimpleRange() {
            OWLDataRange range = ontology.dataPropertyRangeAxioms(self)
                    .map(OWLDataPropertyRangeAxiom::getRange)
                    .findAny().orElseGet(() -> {
                        LOGGER.info("The data property " + self.toStringID() + " has no range in the schema");
                        return STRING_DATARANGE;
                    });

            if (range.equals(CALENDAR_DATARANGE)) {
                return Range.CALENDAR;
            } else if (range.equals(INTEGER_DATARANGE)) {
                return Range.INTEGER;
            } else if (range.equals(LOCAL_STRING_DATARANGE)) {
                return Range.LOCAL_STRING;
            } else if (range.equals(ANY_URI_DATARANGE)) {
                return Range.IRI;
            } else if (range.equals(STRING_DATARANGE)) {
                return Range.STRING;
            } else {
                LOGGER.warn("Unknown data range for property: " + self.toStringID());
                return Range.STRING;
            }
        }

        @Override
        public boolean isFunctionalProperty() {
            return reasoner.getRootOntology().functionalDataPropertyAxioms(self).count() > 0;
        }
    }
}
