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

package us.askplatyp.kb.lucene.lucene;

import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.datamodel.interfaces.WikimediaLanguageCodes;
import us.askplatyp.kb.lucene.model.*;
import us.askplatyp.kb.lucene.model.Class;
import us.askplatyp.kb.lucene.wikimedia.rest.WikimediaREST;
import us.askplatyp.kb.lucene.wikimedia.rest.model.Summary;

import java.io.IOException;
import java.util.*;

/**
 * @author Thomas Pellissier Tanon
 */
class EntityBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityBuilder.class);

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
        addDatatypePropertiesValues(builder, document, locale, DatatypeProperty.SIMPLE_PROPERTIES);
        return builder.build();
    }

    static Entity buildFullEntityInLanguage(Document document, Locale locale, LuceneIndex.Reader indexReader) throws IOException {
        EntityBuilder builder = new EntityBuilder(indexReader, document.get("@id"), Arrays.asList(document.getValues("@type")));
        addDatatypePropertiesValues(builder, document, locale, DatatypeProperty.PROPERTIES);
        for (ObjectProperty property : ObjectProperty.PROPERTIES) {
            builder.setPropertyEntityValueInLocale(
                    property.getLabel(),
                    document.get(property.getLabel()),
                    locale,
                    property.getDomains()
            );
        }
        return builder.build();
    }

    private static void addDatatypePropertiesValues(EntityBuilder entityBuilder, Document document, Locale locale, List<DatatypeProperty> properties) {
        for (DatatypeProperty property : properties) {
            switch (property.getRange()) {
                case STRING:
                    if (property.withMultipleValues()) {
                        entityBuilder.setPropertyValue(
                                property.getLabel(),
                                document.getValues(property.getLabel()),
                                property.getDomains()
                        );
                    } else {
                        entityBuilder.setPropertyValue(
                                property.getLabel(),
                                document.get(property.getLabel()),
                                property.getDomains()
                        );
                    }
                    break;
                case LANGUAGE_TAGGED_STRING:
                    if (property.withMultipleValues()) {
                        entityBuilder.setPropertyValue(
                                property.getLabel(),
                                document.getValues(property.getLabel() + "@" + locale.getLanguage()),
                                property.getDomains()
                        );
                    } else {
                        entityBuilder.setPropertyValue(
                                property.getLabel(),
                                document.get(property.getLabel() + "@" + locale.getLanguage()),
                                property.getDomains()
                        );
                    }
                    break;
                case CALENDAR:
                    entityBuilder.setPropertyDateValue(
                            property.getLabel(),
                            document.get(property.getLabel()),
                            property.getDomains()
                    );
                    break;
                case ARTICLE:
                    findWikipediaArticleIRI(document.getValues("sameAs"), locale)
                            .map(EntityBuilder::buildWikipediaArticle)
                            .ifPresent(article -> entityBuilder.setPropertyValue(property.getLabel(), article, property.getDomains()));
                    break;
                case IMAGE:
                    findWikipediaArticleIRI(document.getValues("sameAs"), locale)
                            .flatMap(EntityBuilder::buildWikipediaImage)
                            .ifPresent(image -> entityBuilder.setPropertyValue(property.getLabel(), image, property.getDomains()));
                    break;
            }
        }
    }

    private static Optional<String> findWikipediaArticleIRI(String[] IRIs, Locale locale) {
        for (String IRI : IRIs) {
            if (IRI.contains(locale.getLanguage() + ".wikipedia.org/wiki/")) { //TODO: support complex language codes
                return Optional.of(IRI);
            }
        }
        return Optional.empty();
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

    private <T> void setPropertyValue(String property, T value, List<Class> possibleTypes) {
        if (value != null && hasOneType(possibleTypes)) {
            propertyValues.put(property, value);
        }
    }

    private void setPropertyDateValue(String property, String value, List<Class> possibleTypes) {
        if (value != null && hasOneType(possibleTypes)) {
            propertyValues.put(property, new CalendarValue(value));
        }
    }

    private void setPropertyEntityValueInLocale(String property, String value, Locale locale, List<Class> possibleTypes) throws IOException {
        if (value != null && hasOneType(possibleTypes)) {
            indexReader.getDocumentForIRI(value).ifPresent(entity ->
                    propertyValues.put(property, buildSimpleEntityInLanguage(entity, locale, indexReader))
            );
        }
    }

    private boolean hasOneType(List<Class> possibleTypes) {
        return possibleTypes.contains(Class.THING) || possibleTypes.stream().anyMatch(type -> types.contains(type.getLabel()));
    }
}
