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
import us.askplatyp.kb.lucene.model.Article;
import us.askplatyp.kb.lucene.model.CalendarValue;
import us.askplatyp.kb.lucene.model.Entity;
import us.askplatyp.kb.lucene.model.Image;
import us.askplatyp.kb.lucene.wikimedia.rest.WikimediaREST;
import us.askplatyp.kb.lucene.wikimedia.rest.model.Summary;

import java.io.IOException;
import java.util.*;

/**
 * @author Thomas Pellissier Tanon
 */
class EntityBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityBuilder.class);
    private static final EntityBuilder INSTANCE = new EntityBuilder();

    static EntityBuilder getInstance() {
        return INSTANCE;
    }

    Entity buildSimpleEntityInLanguage(Document document, Locale locale) {
        FluentEntityBuilder entityBuilder = new FluentEntityBuilder(document.get("@id"), document.getValues("@type"));
        entityBuilder.setPropertyValue("name", document.get("name@" + locale.getLanguage()));
        entityBuilder.setPropertyValue("description", document.get("description@" + locale.getLanguage()));
        entityBuilder.setPropertyValue("alternateName", document.getValues("alternateName@" + locale.getLanguage()));
        entityBuilder.setPropertyValue("url", document.get("url"));
        entityBuilder.setPropertyValue("sameAs", document.getValues("sameAs"));
        entityBuilder.setPropertyValueIfType("rangeIncludes", document.getValues("rangeIncludes"), "Property");
        return entityBuilder.build();
    }

    Entity buildFullEntityInLanguage(Document document, Locale locale) {
        FluentEntityBuilder entityBuilder = new FluentEntityBuilder(document.get("@id"), document.getValues("@type"));
        entityBuilder.setPropertyValue("name", document.get("name@" + locale.getLanguage()));
        entityBuilder.setPropertyValue("description", document.get("description@" + locale.getLanguage()));
        entityBuilder.setPropertyValue("alternateName", document.getValues("alternateName@" + locale.getLanguage()));
        entityBuilder.setPropertyValue("url", document.get("url"));
        entityBuilder.setPropertyValue("sameAs", document.getValues("sameAs"));
        Optional<String> wikipediaArticleIRI = findWikipediaArticleIRI(document.getValues("sameAs"), locale);
        wikipediaArticleIRI.flatMap(this::buildWikipediaImage).ifPresent(image ->
                entityBuilder.setPropertyValue("image", image)
        );
        wikipediaArticleIRI.map(this::buildWikipediaArticle).ifPresent(image ->
                entityBuilder.setPropertyValue("detailedDescription", image)
        );
        entityBuilder.setPropertyValueIfType("rangeIncludes", document.getValues("rangeIncludes"), "Property");
        entityBuilder.setPropertyDateValueIfType("birthDate", document.get("birthDate"), "Person");
        entityBuilder.setPropertyValueIfType("birthPlace", document.get("birthPlace"), "Person");
        entityBuilder.setPropertyDateValueIfType("deathDate", document.get("deathDate"), "Person");
        entityBuilder.setPropertyValueIfType("deathPlace", document.get("deathPlace"), "Person");
        entityBuilder.setPropertyDateValueIfType("nationality", document.get("nationality"), "Person");
        return entityBuilder.build();
    }

    private Optional<String> findWikipediaArticleIRI(String[] IRIs, Locale locale) {
        for (String IRI : IRIs) {
            if (IRI.contains(locale.getLanguage() + ".wikipedia.org/wiki/")) { //TODO: support complex language codes
                return Optional.of(IRI);
            }
        }
        return Optional.empty();
    }

    private Article buildWikipediaArticle(String articleIRI) {
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

    private Optional<Image> buildWikipediaImage(String articleIRI) {
        try {
            return WikimediaREST.getInstance().getSummary(articleIRI).getThumbnail().map(thumbnail ->
                    new Image(thumbnail.getSource()) //TODO: license
            );
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return Optional.empty();
        }
    }

    private static class FluentEntityBuilder {
        private String IRI;
        private List<String> types;
        private Map<String, Object> propertyValues = new TreeMap<>();

        FluentEntityBuilder(String IRI, String[] types) {
            this.IRI = IRI;
            this.types = Arrays.asList(types);
        }

        <T> void setPropertyValue(String property, T value) {
            if (value != null) {
                propertyValues.put(property, value);
            }
        }

        void setPropertyDateValue(String property, String value) {
            if (value != null) {
                propertyValues.put(property, new CalendarValue(value));
            }
        }

        void setPropertyValueIfType(String property, Object value, String type) {
            if (types.contains(type)) {
                setPropertyValue(property, value);
            }
        }

        void setPropertyDateValueIfType(String property, String value, String type) {
            if (types.contains(type)) {
                setPropertyDateValue(property, value);
            }
        }

        Entity build() {
            return new Entity(IRI, types, propertyValues);
        }
    }
}
