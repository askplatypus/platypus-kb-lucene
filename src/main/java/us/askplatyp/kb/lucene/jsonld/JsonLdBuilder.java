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

package us.askplatyp.kb.lucene.jsonld;

import com.google.common.collect.Sets;
import com.vividsolutions.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.datamodel.interfaces.WikimediaLanguageCodes;
import us.askplatyp.kb.lucene.model.*;
import us.askplatyp.kb.lucene.model.value.*;
import us.askplatyp.kb.lucene.wikimedia.rest.KartographerAPI;
import us.askplatyp.kb.lucene.wikimedia.rest.WikimediaREST;
import us.askplatyp.kb.lucene.wikimedia.rest.model.Summary;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Thomas Pellissier Tanon
 */
public class JsonLdBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonLdBuilder.class);
    private static final Set<String> SIMPLE_PROPERTIES = Sets.newHashSet(
            "name", "description", "alternateName", "url", "sameAs", "range"
    );
    private static final Schema SCHEMA = Schema.getSchema();

    private StorageLookup storageLookup;

    public JsonLdBuilder(StorageLookup storageLookup) {
        this.storageLookup = storageLookup;
    }

    private static Optional<String> findWikipediaArticleIRI(Stream<String> IRIs, Locale locale) {
        //TODO: support complex language codes
        return IRIs.filter(IRI -> IRI.contains(locale.getLanguage() + ".wikipedia.org/wiki/")).findAny();
    }

    private static Optional<Object> buildGeoValueFromKartographer(Resource resource) {
        try {
            //We only do geoshape lookup for Places in order to don't overload the servers
            if (resource.getTypes().anyMatch(type -> type.equals("Place"))) {
                Geometry shape = KartographerAPI.getInstance()
                        .getShapeForItemId(Namespaces.expand(resource.getIRI()));
                if (!shape.isEmpty()) {
                    return Optional.of(GeoValue.buildGeoValue(shape));
                }
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
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
                    new Image(thumbnail.getSource(), thumbnail.getWidth(), thumbnail.getHeight()) //TODO: license
            );
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return Optional.empty();
        }
    }

    public JsonLdRoot<Entity> buildEntityInLanguage(Resource resource, Locale locale) {
        return new JsonLdRoot<>(
                new Context(locale),
                buildEntity(resource, locale, true)
        );
    }

    public JsonLdRoot<Collection<EntitySearchResult<Entity>>> buildEntitySearchResultInLanguage(
            ResourceSearchResult searchResult, String baseURI, Locale locale
    ) {
        return new JsonLdRoot<>(new Context(locale), new PartialCollection<>(
                searchResult.getResources().stream().map(resource -> new EntitySearchResult<>(
                        buildEntity(resource.getResource(), locale, false), resource.getScore()
                )).collect(Collectors.toList()),
                searchResult.getTotalHits(), baseURI, searchResult.getCurrentContinue(), searchResult.getNextContinue()
        ));
    }

    private Entity buildEntity(Resource resource, Locale locale, boolean fullEntity) {
        return new Entity(
                resource.getIRI(),
                resource.getTypes().collect(Collectors.toList()),
                buildPropertiesValues(resource, locale, fullEntity)
        );
    }

    private Map<String, Object> buildPropertiesValues(Resource resource, Locale locale, boolean fullEntity) {
        Map<String, Object> propertyValues = new HashMap<>();

        SCHEMA.getProperties().forEach(property -> {
            String propertyIRI = property.getIRI();

            if (!fullEntity && !SIMPLE_PROPERTIES.contains(propertyIRI)) {
                return;
            }

            List<Object> results = resource.getValuesForProperty(propertyIRI).flatMap(value -> {
                switch (property.getSimpleRange()) {
                    case CALENDAR:
                    case GEO:
                        return Stream.of(value);
                    case LOCAL_STRING:
                        if (value instanceof LocaleStringValue && ((LocaleStringValue) value).getLocale().equals(locale)) {
                            return Stream.of(value.toString());
                        } else {
                            return Stream.empty();
                        }
                    case RESOURCE:
                        if (value instanceof ResourceValue && fullEntity) {
                            try {
                                return storageLookup.getResourceForIRI(value.toString())
										.map(object -> buildEntity(object, locale, false))
										.map(Stream::of).orElseGet(Stream::empty);
							} catch (IOException e) {
                                LOGGER.warn("Error when retirving the resource: " + value.toString());
                            }
                        }
                        return Stream.empty();
                    case STRING:
                    case IRI:
                        return Stream.of(value.toString());
                    default:
                        LOGGER.warn("Unsupported simple range type: " + property.getSimpleRange().toString());
                        return Stream.empty();
                }
            }).collect(Collectors.toList());

            if (!results.isEmpty()) {
                if (property.isFunctionalProperty()) {
                    propertyValues.put(propertyIRI, results.get(0));
                } else {
                    propertyValues.put(propertyIRI, results);
                }
            }
        });

        if (fullEntity) {
            Optional<String> wikipediaArticleIRI = findWikipediaArticleIRI(resource.getStringValuesForProperty("sameAs"), locale);
            wikipediaArticleIRI
                    .map(JsonLdBuilder::buildWikipediaArticle)
                    .ifPresent(article -> propertyValues.put("detailedDescription", article));
            wikipediaArticleIRI
                    .flatMap(JsonLdBuilder::buildWikipediaImage)
                    .ifPresent(image -> propertyValues.put("image", image));
            buildGeoValueFromKartographer(resource)
                    .ifPresent(geoValue -> propertyValues.put("geo", geoValue));
        }

        return propertyValues;
    }
}
