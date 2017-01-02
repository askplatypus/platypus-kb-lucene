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

import graphql.Scalars;
import graphql.language.StringValue;
import graphql.relay.Relay;
import graphql.schema.*;
import us.askplatyp.kb.lucene.lucene.DataFetcherBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInterfaceType.newInterface;
import static graphql.schema.GraphQLObjectType.newObject;
import static graphql.schema.GraphQLSchema.newSchema;

/**
 * @author Thomas Pellissier Tanon
 */
public class GraphQLSchemaBuilder {

    //Scalar types
    private static GraphQLScalarType BCP47 = new GraphQLScalarType("BCP47", "BCP47 locale", new Coercing() {
        @Override
        public String serialize(Object input) {
            if (input instanceof Locale) {
                return ((Locale) input).toLanguageTag();
            } else if (input instanceof String) {
                return (String) input;
            } else {
                return null;
            }
        }

        @Override
        public Locale parseValue(Object input) {
            if (input instanceof Locale) {
                return (Locale) input;
            } else if (input instanceof String) {
                return Locale.forLanguageTag((String) input);
            } else {
                return null;
            }
        }

        @Override
        public Locale parseLiteral(Object input) {
            if (input instanceof StringValue) {
                return Locale.forLanguageTag(((StringValue) input).getValue());
            } else {
                return null;
            }
        }
    });


    private DataFetcherBuilder dataFetcherBuilder;
    private Relay relay;
    private GraphQLInterfaceType node;
    private GraphQLInterfaceType entity;
    private GraphQLObjectType image;
    private GraphQLObjectType article;
    private GraphQLObjectType namedIndividual;
    private GraphQLObjectType property;

    public GraphQLSchemaBuilder(DataFetcherBuilder dataFetcherBuilder) {
        this.dataFetcherBuilder = dataFetcherBuilder;
        relay = new Relay();
        node = relay.nodeInterface(buildEntityTypeResolver());
        entity = buildEntity();
        image = buildImage();
        article = buildArticle();
        namedIndividual = buildNamedIndividual();
        property = buildProperty();
    }

    private GraphQLNonNull nonNullList(GraphQLType elementType) {
        return new GraphQLNonNull(new GraphQLList(new GraphQLNonNull(elementType)));
    }

    private List<GraphQLFieldDefinition> buildFieldsForClass(Class dmClass) {
        List<GraphQLFieldDefinition> fields = new ArrayList<>();
        fields.add(newFieldDefinition()
                .name("id")
                .description("The IRI of the entity")
                .type(new GraphQLNonNull(Scalars.GraphQLID))
                .dataFetcher(dataFetcherBuilder.stringPropertyFetcher("@id"))
                .build()
        );
        fields.add(newFieldDefinition()
                .name("type")
                .description("The types of the entity")
                .type(nonNullList(Scalars.GraphQLString))
                .dataFetcher(dataFetcherBuilder.stringsPropertyFetcher("@type"))
                .build()
        );

        for (ObjectProperty property : ObjectProperty.PROPERTIES) {
            if (property.getDomains().stream().anyMatch(dmClass::isSubClassOf)) {
                if (property.withMultipleValues()) {
                    fields.add(newFieldDefinition()
                            .name(property.getLabel())
                            .description(property.getDescription())
                            .type(nonNullList(new GraphQLTypeReference(property.getRange().getLabel())))
                            .dataFetcher(dataFetcherBuilder.entityPropertyFetcher(property.getLabel()))
                            .build()
                    );
                } else {
                    fields.add(newFieldDefinition()
                            .name(property.getLabel())
                            .description(property.getDescription())
                            .type(new GraphQLTypeReference(property.getRange().getLabel()))
                            .dataFetcher(dataFetcherBuilder.entitiesPropertyFetcher(property.getLabel()))
                            .build()
                    );
                }
            }
        }
        for (DatatypeProperty property : DatatypeProperty.PROPERTIES) {
            if (property.getDomains().stream().anyMatch(dmClass::isSubClassOf)) {
                GraphQLFieldDefinition.Builder fieldBuilder = newFieldDefinition()
                        .name(property.getLabel())
                        .description(property.getDescription());
                switch (property.getRange()) {
                    case STRING:
                        if (property.withMultipleValues()) {
                            fieldBuilder.type(nonNullList(Scalars.GraphQLString))
                                    .dataFetcher(dataFetcherBuilder.stringsPropertyFetcher(property.getLabel()));
                        } else {
                            fieldBuilder.type(Scalars.GraphQLString)
                                    .dataFetcher(dataFetcherBuilder.stringPropertyFetcher(property.getLabel()));
                        }
                        break;
                    case LANGUAGE_TAGGED_STRING:
                        if (property.withMultipleValues()) {
                            fieldBuilder.type(nonNullList(Scalars.GraphQLString))
                                    .argument(newArgument()
                                            .name("language")
                                            .description("The required language of the values")
                                            .type(new GraphQLNonNull(BCP47))
                                    )
                                    .dataFetcher(dataFetcherBuilder.languageStringsPropertyFetcher(property.getLabel()));
                        } else {
                            fieldBuilder.type(Scalars.GraphQLString)
                                    .argument(newArgument()
                                            .name("language")
                                            .description("The required language of the value")
                                            .type(new GraphQLNonNull(BCP47))
                                    )
                                    .dataFetcher(dataFetcherBuilder.languageStringPropertyFetcher(property.getLabel()));
                        }
                        break;
                    case CALENDAR:
                        //TODO
                        if (property.withMultipleValues()) {
                            fieldBuilder.type(nonNullList(Scalars.GraphQLString))
                                    .dataFetcher(dataFetcherBuilder.stringsPropertyFetcher(property.getLabel()));
                        } else {
                            fieldBuilder.type(Scalars.GraphQLString)
                                    .dataFetcher(dataFetcherBuilder.stringPropertyFetcher(property.getLabel()));
                        }
                        break;
                    case ARTICLE:
                        fieldBuilder.type(article)
                                .argument(newArgument()
                                        .name("language")
                                        .description("The language of the article to retrieve")
                                        .type(new GraphQLNonNull(BCP47))
                                )
                                .dataFetcher(dataFetcherBuilder.wikipediaArticleFetcher());
                        break;
                    case IMAGE:
                        fieldBuilder.type(image)
                                .dataFetcher(dataFetcherBuilder.wikipediaImageFetcher());
                        break;
                }
                fields.add(fieldBuilder.build());
            }
        }
        return fields;
    }

    private TypeResolver buildEntityTypeResolver() {
        return (document -> {
            List<String> types = dataFetcherBuilder.retrieveTypes(document);
            if (types.contains("Property")) {
                return property;
            } else {
                return namedIndividual;
            }
        });
    }

    private GraphQLInterfaceType buildEntity() {
        return newInterface()
                .name("Entity")
                .description("Something described by the knowledge base")
                .fields(buildFieldsForClass(Class.THING))
                .typeResolver(buildEntityTypeResolver())
                .build();
    }

    private GraphQLObjectType buildImage() {
        return newObject()
                .name("Image")
                .description("An image")
                .field(newFieldDefinition()
                        .name("type")
                        .description("RDF type for the image")
                        .type(nonNullList(Scalars.GraphQLString))
                        .staticValue(Collections.singletonList("ImageObject"))
                )
                .field(newFieldDefinition()
                        .name("contentUrl")
                        .description("The URL of the image")
                        .type(new GraphQLNonNull(Scalars.GraphQLString))
                        .dataFetcher(environment -> ((Image) environment.getSource()).getContentURL())
                )
                .build();
    }

    private GraphQLObjectType buildArticle() {
        return newObject()
                .name("Article")
                .description("An article")
                .withInterfaces(node, entity)
                .field(newFieldDefinition()
                        .name("id")
                        .description("The URL of the article")
                        .type(new GraphQLNonNull(Scalars.GraphQLID))
                        .dataFetcher(environment -> ((Article) environment.getSource()).getIRI())
                )
                .field(newFieldDefinition()
                        .name("type")
                        .description("RDF type for the article")
                        .type(nonNullList(Scalars.GraphQLString))
                        .staticValue(Collections.singletonList("Article"))
                )
                .field(newFieldDefinition()
                        .name("name")
                        .description("The name of the article")
                        .type(Scalars.GraphQLString)
                        .argument(newArgument()
                                .name("language")
                                .description("The language of the name to retrieve")
                                .type(new GraphQLNonNull(BCP47))
                        )
                        .dataFetcher(environment -> ((Article) environment.getSource()).getTitle())
                )
                .field(newFieldDefinition()
                        .name("description")
                        .description("The article description (no value for now)")
                        .type(Scalars.GraphQLString)
                        .argument(newArgument()
                                .name("language")
                                .description("The language of the description to retrieve")
                                .type(new GraphQLNonNull(BCP47))
                        )
                        .staticValue(null)
                        .build()
                )
                .field(newFieldDefinition()
                        .name("alternateName")
                        .description("Article alternative names (no values for now)")
                        .type(nonNullList(Scalars.GraphQLString))
                        .argument(newArgument()
                                .name("language")
                                .description("The language of the alternate names to retrieve")
                                .type(new GraphQLNonNull(BCP47))
                        )
                        .staticValue(Collections.emptyList())
                        .build())
                .field(newFieldDefinition()
                        .name("articleBody")
                        .description("The first lines of the Article")
                        .type(new GraphQLNonNull(Scalars.GraphQLString))
                        .dataFetcher(environment -> ((Article) environment.getSource()).getArticleHeadlines())
                )
                .field(newFieldDefinition()
                        .name("licence")
                        .description("The licence the Article is in")
                        .type(new GraphQLNonNull(Scalars.GraphQLString))
                        .dataFetcher(environment -> ((Article) environment.getSource()).getLicenseIRI())
                )
                .field(newFieldDefinition()
                        .name("inLanguage")
                        .description("The language the Article is in")
                        .type(new GraphQLNonNull(BCP47))
                        .dataFetcher(environment -> ((Article) environment.getSource()).getLanguageCode())
                )
                .build();
    }

    private GraphQLObjectType buildNamedIndividual() {
        return newObject()
                .name("NamedIndividual")
                .description("An individual described by the knowledge base")
                .withInterfaces(node, entity)
                .fields(buildFieldsForClass(Class.NAMED_INDIVIDUAL))
                .build();
    }

    private GraphQLObjectType buildProperty() {
        return newObject()
                .name("Property")
                .description("A property stored in the knowledge base. For internal Platypus use only.")
                .withInterfaces(node, entity)
                .fields(buildFieldsForClass(Class.PROPERTY))
                .build();
    }

    private GraphQLObjectType buildQuery() {
        return newObject()
                .name("Query")
                .field(relay.nodeField(node, dataFetcherBuilder.entityForIRIFetcher()))
                .field(newFieldDefinition()
                        .type(entity)
                        .name("entity")
                        .argument(newArgument()
                                .name("id")
                                .description("The id of the entity to retrieve")
                                .type(new GraphQLNonNull(Scalars.GraphQLID))
                        )
                        .dataFetcher(dataFetcherBuilder.entityForIRIFetcher())
                )
                .field(newFieldDefinition()
                        .type(namedIndividual)
                        .name("individual")
                        .argument(newArgument()
                                .name("id")
                                .description("The id of the individual to retrieve")
                                .type(new GraphQLNonNull(Scalars.GraphQLID))
                        )
                        .dataFetcher(dataFetcherBuilder.entityForIRIFetcher("NamedIndividual"))
                )
                .field(newFieldDefinition()
                        .type(property)
                        .name("property")
                        .argument(newArgument()
                                .name("id")
                                .description("The id of the property to retrieve. For internal use only.")
                                .type(new GraphQLNonNull(Scalars.GraphQLID))
                        )
                        .dataFetcher(dataFetcherBuilder.entityForIRIFetcher("Property"))
                )
                .build();
    }

    public GraphQLSchema schema() {
        return newSchema()
                .query(buildQuery())
                .build();
    }
}
