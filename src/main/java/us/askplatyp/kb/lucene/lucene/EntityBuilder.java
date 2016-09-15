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
import us.askplatyp.kb.lucene.wikimedia.rest.model.Thumbnail;

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
                wikipediaArticleIRI.map(this::buildWikipediaImage).orElseGet(() -> null),
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

    private Image buildWikipediaImage(String articleIRI) {
        try {
            Thumbnail thumbnail = WikimediaREST.getInstance().getSummary(articleIRI).getThumbnail();
            return new Image(thumbnail.getSource()); //TODO: license
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }
}
