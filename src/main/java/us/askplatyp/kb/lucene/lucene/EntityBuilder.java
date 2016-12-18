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
        fillSimpleEntityInLanguage(builder, document, locale);
        return builder.build();
    }

    static Entity buildFullEntityInLanguage(Document document, Locale locale, LuceneIndex.Reader indexReader) throws IOException {
        EntityBuilder builder = new EntityBuilder(indexReader, document.get("@id"), Arrays.asList(document.getValues("@type")));
        fillSimpleEntityInLanguage(builder, document, locale);
        fillExtraEntityInLanguage(builder, document, locale);
        return builder.build();
    }

    private static void fillSimpleEntityInLanguage(EntityBuilder builder, Document document, Locale locale) {
        builder.setPropertyValue("name", document.get("name@" + locale.getLanguage()));
        builder.setPropertyValue("description", document.get("description@" + locale.getLanguage()));
        builder.setPropertyValue("alternateName", document.getValues("alternateName@" + locale.getLanguage()));
        builder.setPropertyValue("url", document.get("url"));
        builder.setPropertyValue("sameAs", document.getValues("sameAs"));
        builder.setPropertyValueIfType("range", document.get("range"), "Property");
    }

    private static void fillExtraEntityInLanguage(EntityBuilder builder, Document document, Locale locale) throws IOException {
        Optional<String> wikipediaArticleIRI = findWikipediaArticleIRI(document.getValues("sameAs"), locale);
        wikipediaArticleIRI.flatMap(EntityBuilder::buildWikipediaImage).ifPresent(image ->
                builder.setPropertyValue("image", image)
        );
        wikipediaArticleIRI.map(EntityBuilder::buildWikipediaArticle).ifPresent(image ->
                builder.setPropertyValue("detailedDescription", image)
        );
        builder.setPropertyDateValueIfType("birthDate", document.get("birthDate"), "Person");
        builder.setPropertyEntityValueInLocaleIfType("birthPlace", document.get("birthPlace"), locale, "Person");
        builder.setPropertyDateValueIfType("deathDate", document.get("deathDate"), "Person");
        builder.setPropertyEntityValueInLocaleIfType("deathPlace", document.get("deathPlace"), locale, "Person");
        builder.setPropertyDateValueIfType("nationality", document.get("nationality"), "Person");
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

    private <T> void setPropertyValue(String property, T value) {
        if (value != null) {
            propertyValues.put(property, value);
        }
    }

    private void setPropertyDateValue(String property, String value) {
        if (value != null) {
            propertyValues.put(property, new CalendarValue(value));
        }
    }

    private void setPropertyEntityValueInLocale(String property, String value, Locale locale) throws IOException {
        if (value != null) {
            indexReader.getDocumentForIRI(value).ifPresent(entity ->
                    propertyValues.put(property, buildSimpleEntityInLanguage(entity, locale, indexReader))
            );
        }
    }

    private void setPropertyValueIfType(String property, Object value, String type) {
        if (types.contains(type)) {
            setPropertyValue(property, value);
        }
    }

    private void setPropertyDateValueIfType(String property, String value, String type) {
        if (types.contains(type)) {
            setPropertyDateValue(property, value);
        }
    }

    private void setPropertyEntityValueInLocaleIfType(String property, String value, Locale locale, String type) throws IOException {
        if (types.contains(type)) {
            setPropertyEntityValueInLocale(property, value, locale);
        }
    }
}
