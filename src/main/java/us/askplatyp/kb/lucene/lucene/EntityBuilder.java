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
import us.askplatyp.kb.lucene.model.Entity;
import us.askplatyp.kb.lucene.model.Image;
import us.askplatyp.kb.lucene.wikimedia.rest.WikimediaREST;
import us.askplatyp.kb.lucene.wikimedia.rest.model.Summary;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;

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
        String[] types = document.getValues("@type");
        return new Entity(
                document.get("@id"),
                types,
                document.get("name@" + locale.getLanguage()),
                document.get("description@" + locale.getLanguage()),
                document.getValues("alternateName@" + locale.getLanguage()),
                document.get("url"),
                document.getValues("sameAs"),
                null,
                null,
                valuesIfType(document, "rangeIncludes", "Property", types)
        );
    }

    Entity buildFullEntityInLanguage(Document document, Locale locale) {
        Optional<String> wikipediaArticleIRI = findWikipediaArticleIRI(document.getValues("sameAs"), locale);

        String[] types = document.getValues("@type");
        return new Entity(
                document.get("@id"),
                types,
                document.get("name@" + locale.getLanguage()),
                document.get("description@" + locale.getLanguage()),
                document.getValues("alternateName@" + locale.getLanguage()),
                document.get("url"),
                document.getValues("sameAs"),
                wikipediaArticleIRI.flatMap(this::buildWikipediaImage).orElseGet(() -> null),
                wikipediaArticleIRI.map(this::buildWikipediaArticle).orElseGet(() -> null),
                valuesIfType(document, "rangeIncludes", "Property", types)
        );
    }

    private String[] valuesIfType(Document document, String name, String type, String[] types) {
        if (!valueInArray(type, types)) {
            return null;
        }
        return document.getValues(name);
    }

    private <T> boolean valueInArray(T value, T[] array) {
        for (T element : array) {
            if (element.equals(value)) {
                return true;
            }
        }
        return false;
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
            return null;
        }
    }
}
