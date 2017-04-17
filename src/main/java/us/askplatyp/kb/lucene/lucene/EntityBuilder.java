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

package us.askplatyp.kb.lucene.lucene;

import com.google.common.collect.Sets;
import com.vividsolutions.jts.geom.Geometry;
import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.datamodel.interfaces.WikimediaLanguageCodes;
import us.askplatyp.kb.lucene.model.Entity;
import us.askplatyp.kb.lucene.model.Namespaces;
import us.askplatyp.kb.lucene.model.Schema;
import us.askplatyp.kb.lucene.model.value.Article;
import us.askplatyp.kb.lucene.model.value.CalendarValue;
import us.askplatyp.kb.lucene.model.value.GeoValue;
import us.askplatyp.kb.lucene.model.value.Image;
import us.askplatyp.kb.lucene.wikimedia.rest.KartographerAPI;
import us.askplatyp.kb.lucene.wikimedia.rest.WikimediaREST;
import us.askplatyp.kb.lucene.wikimedia.rest.model.Summary;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Thomas Pellissier Tanon
 */
class EntityBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityBuilder.class);
    private static final Set<String> SIMPLE_PROPERTIES = Sets.newHashSet("name", "description", "alternateName", "url", "sameAs", "range");
    private static final Schema SCHEMA = Schema.getSchema();

    private LuceneIndex.Reader indexReader;
    private String IRI;
    private List<String> types;
    private Map<String, Object> propertyValues = new TreeMap<>();

    private EntityBuilder(LuceneIndex.Reader indexReader, String IRI, List<String> types) {
        this.indexReader = indexReader;
        this.IRI = IRI;
        this.types = types;
    }

    static Entity buildSimpleEntityInLanguage(Document document, Locale locale, LuceneIndex.Reader indexReader) {
        EntityBuilder builder = new EntityBuilder(indexReader, document.get("@id"), Arrays.asList(document.getValues("@type")));
        addPropertiesValues(builder, document, locale, true);
        return builder.build();
    }

    static Entity buildFullEntityInLanguage(Document document, Locale locale, LuceneIndex.Reader indexReader) {
        EntityBuilder builder = new EntityBuilder(indexReader, document.get("@id"), Arrays.asList(document.getValues("@type")));
        addPropertiesValues(builder, document, locale, false);
        return builder.build();
    }

    private static void addPropertiesValues(EntityBuilder entityBuilder, Document document, Locale locale, boolean simpleOnly) {
        if (!simpleOnly) {
            findWikipediaArticleIRI(document.getValues("sameAs"), locale)
                    .map(EntityBuilder::buildWikipediaArticle)
                    .ifPresent(article -> entityBuilder.setPropertyValue("detailedDescription", article));
            findWikipediaArticleIRI(document.getValues("sameAs"), locale)
                    .flatMap(EntityBuilder::buildWikipediaImage)
                    .ifPresent(image -> entityBuilder.setPropertyValue("image", image));
            buildGeoValue(document)
                    .ifPresent(geoValue -> entityBuilder.setPropertyValue("geo", geoValue));
        }

        SCHEMA.getProperties().forEach(undeterminedProperty -> {
            if (undeterminedProperty instanceof Schema.DatatypeProperty) {
                Schema.DatatypeProperty property = (Schema.DatatypeProperty) undeterminedProperty;
                if (simpleOnly && !SIMPLE_PROPERTIES.contains(property.getShortURI())) {
                    return;
                }
                switch (property.getRange()) {
                    case STRING:
                        if (property.isFunctionalProperty()) {
                            entityBuilder.setPropertyValue(
                                    property.getShortURI(),
                                    document.get(property.getShortURI())
                            );
                        } else {
                            entityBuilder.setPropertyValue(
                                    property.getShortURI(),
                                    document.getValues(property.getShortURI())
                            );
                        }
                        break;
                    case LANGUAGE_TAGGED_STRING:
                        if (property.isFunctionalProperty()) {
                            entityBuilder.setPropertyValue(
                                    property.getShortURI(),
                                    document.get(property.getShortURI() + "@" + locale.getLanguage())
                            );
                        } else {
                            entityBuilder.setPropertyValue(
                                    property.getShortURI(),
                                    document.getValues(property.getShortURI() + "@" + locale.getLanguage())
                            );
                        }
                        break;
                    case CALENDAR:
                        if (property.isFunctionalProperty()) {
                            entityBuilder.setPropertyCalendarValue(
                                    property.getShortURI(),
                                    document.get(property.getShortURI())
                            );
                        } else {
                            entityBuilder.setPropertyCalendarValues(
                                    property.getShortURI(),
                                    document.getValues(property.getShortURI())
                            );
                        }
                        break;
                }
            } else if (!simpleOnly && undeterminedProperty instanceof Schema.ObjectProperty) {
                Schema.ObjectProperty property = (Schema.ObjectProperty) undeterminedProperty;
                try {
                    if (property.isFunctionalProperty()) {
                        entityBuilder.setPropertyEntityValueInLocale(
                                property.getShortURI(),
                                document.get(property.getShortURI()),
                                locale
                        );
                    } else {
                        entityBuilder.setPropertyEntityValuesInLocale(
                                property.getShortURI(),
                                document.getValues(property.getShortURI()),
                                locale
                        );
                    }
                } catch (IOException e) {
                    LOGGER.warn(e.getMessage(), e);
                }
            }
        });
    }

    private static Optional<String> findWikipediaArticleIRI(String[] IRIs, Locale locale) {
        for (String IRI : IRIs) {
            if (IRI.contains(locale.getLanguage() + ".wikipedia.org/wiki/")) { //TODO: support complex language codes
                return Optional.of(IRI);
            }
        }
        return Optional.empty();
    }

    private static Optional<Object> buildGeoValue(Document document) {
        try {
            Geometry shape = KartographerAPI.getInstance()
                    .getShapeForItemId(Namespaces.expand(document.get("@id")));
            if (!shape.isEmpty()) {
                return Optional.of(GeoValue.buildGeoValue(shape));
            }
            return Optional.ofNullable(document.get("geo")).map(GeoValue::buildGeoValue);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return Optional.empty();
        }
    }

    private static Article buildWikipediaArticle(String articleIRI) {
        try {
            Summary summary = WikimediaREST.getInstance().getSummary(articleIRI);
            return new Article(
                    articleIRI,
                    summary.getTitle(),
                    summary.getExtract(),
                    "http://creativecommons.org/licenses/by-sa/3.0/",
                    WikimediaLanguageCodes.getLanguageCode(summary.getLanguageCode())
            );
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    private static Optional<Image> buildWikipediaImage(String articleIRI) {
        try {
            return WikimediaREST.getInstance().getSummary(articleIRI).getThumbnail().map(thumbnail ->
                    new Image(thumbnail.getSource()) //TODO: license
            );
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return Optional.empty();
        }
    }

    public Entity build() {
        return new Entity(IRI, types, propertyValues);
    }

    private <T> void setPropertyValue(String property, T value) {
        if (value != null) {
            propertyValues.put(property, value);
        }
    }

    private void setPropertyCalendarValue(String property, String value) {
        if (value != null) {
            propertyValues.put(property, new CalendarValue(value));
        }
    }

    private void setPropertyCalendarValues(String property, String[] values) {
        if (values != null) {
            propertyValues.put(property, Arrays.stream(values).map(CalendarValue::new).collect(Collectors.toList()));
        }
    }

    private void setPropertyEntityValueInLocale(String property, String value, Locale locale) throws IOException {
        if (value != null) {
            indexReader.getDocumentForIRI(value).ifPresent(entity ->
                    propertyValues.put(property, buildSimpleEntityInLanguage(entity, locale, indexReader))
            );
        }
    }

    private void setPropertyEntityValuesInLocale(String property, String[] values, Locale locale) throws IOException {
        if (values != null) {
            List<Entity> entities = new ArrayList<>();
            for (String value : values) {
                indexReader.getDocumentForIRI(value).ifPresent(entity ->
                        entities.add(buildSimpleEntityInLanguage(entity, locale, indexReader))
                );
            }
            propertyValues.put(property, entities);
        }
    }
}
