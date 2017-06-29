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

package us.askplatyp.kb.lucene.wikidata.mapping;

import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue;
import us.askplatyp.kb.lucene.wikidata.WikidataTypeHierarchy;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Thomas Pellissier Tanon
 */
public class MapperRegistry {

    private Map<PropertyIdValue, StatementMapper> mapperForProperty;

    public MapperRegistry(WikidataTypeHierarchy typeHierarchy) {
        this.mapperForProperty = buildMappers(typeHierarchy);
    }

    private Map<PropertyIdValue, StatementMapper> buildMappers(WikidataTypeHierarchy typeHierarchy) {
        Map<PropertyIdValue, StatementMapper> mapperForProperty = new HashMap<>();
        //TODO: IMDB, LinkedIn, Myspace, Pinterest, Tumblr...
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P19"), new ItemIdStatementMapper("birthPlace"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P20"), new ItemIdStatementMapper("deathPlace"));
        //TODO: use http://schema.org/(Male|Female)? MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P21"), new ItemIdStatementMapper("gender"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P22"), new ItemIdStatementMapper("parent"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P25"), new ItemIdStatementMapper("parent"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P26"), new ItemIdStatementMapper("spouse"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P27"), new ItemIdStatementMapper("nationality"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P31"), new TypeMapper(typeHierarchy));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P40"), new ItemIdStatementMapper("children"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P50"), new ItemIdStatementMapper("author"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P57"), new ItemIdStatementMapper("director"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P98"), new ItemIdStatementMapper("editor"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P108"), new ItemIdStatementMapper("worksFor"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P110"), new ItemIdStatementMapper("illustrator"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P112"), new ItemIdStatementMapper("founder"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P123"), new ItemIdStatementMapper("publisher"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P136"), new ItemIdStatementMapper("genre"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P161"), new ItemIdStatementMapper("actor"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P162"), new ItemIdStatementMapper("producer"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P162"), new ItemIdStatementMapper("award"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P170"), new ItemIdStatementMapper("creator"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P175"), new ItemIdStatementMapper("byArtist"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P176"), new ItemIdStatementMapper("provider"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P212"), new ISBNStatementMapper());
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P214"), new ExternalIdentifierStatementMapper("http://viaf.org/viaf/$1", "[1-9]\\d(\\d{0,7}|\\d{17,20})"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P229"), new StringStatementMapper("iataCode", "[A-Z0-9]{2}"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P230"), new StringStatementMapper("icaoCode", "[A-Z]{3}"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P236"), new ISSNStatementMapper());
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P238"), new StringStatementMapper("iataCode", "[A-Z]{3}"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P239"), new StringStatementMapper("icaoCode", "([A-Z]{2}|[CKY][A-Z0-9])[A-Z0-9]{2}"));
        //TODO: P249 tickerSymbol have ISO15022 compliant code
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P275"), new ItemIdStatementMapper("license"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P276"), new ItemIdStatementMapper("location"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P361"), new ItemIdStatementMapper("isPartOf"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P433"), new StringStatementMapper("issueNumber"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P434"), new ExternalIdentifierStatementMapper("http://musicbrainz.org/artist/$1", "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P435"), new ExternalIdentifierStatementMapper("http://musicbrainz.org/work/$1", "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P436"), new ExternalIdentifierStatementMapper("http://musicbrainz.org/release-group/$1", "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P453"), new ItemIdStatementMapper("roleName"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P463"), new ItemIdStatementMapper("memberOf"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P478"), new StringStatementMapper("volumeNumber"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P483"), new ItemIdStatementMapper("recordedAt"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P495"), new ItemIdStatementMapper("countryOfOrigin"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P527"), new ItemIdStatementMapper("hasPart"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P551"), new ItemIdStatementMapper("homeLocation"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P569"), new TimeStatementMapper("birthDate"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P570"), new TimeStatementMapper("deathDate"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P571"), new TimeStatementMapper("dateCreated"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P577"), new TimeStatementMapper("datePublished"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P625"), new GlobeCoordinatesStatementMapper("geo"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P646"), new ExternalIdentifierStatementMapper("http://g.co/kg$1", "(/m/0[0-9a-z_]{2,6}|/m/01[0123][0-9a-z_]{5})"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P655"), new ItemIdStatementMapper("translator"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P674"), new ItemIdStatementMapper("character"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P734"), new ItemIdStatementMapper("familyName"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P735"), new ItemIdStatementMapper("givenName"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P767"), new ItemIdStatementMapper("contributor"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P840"), new ItemIdStatementMapper("contentLocation"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P856"), new URIStatementMapper("url"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P859"), new ItemIdStatementMapper("sponsor"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P921"), new ItemIdStatementMapper("about"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P957"), new ISBNStatementMapper());
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P966"), new ExternalIdentifierStatementMapper("http://musicbrainz.org/label/$1", "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P982"), new ExternalIdentifierStatementMapper("http://musicbrainz.org/area/$1", "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P1004"), new ExternalIdentifierStatementMapper("http://musicbrainz.org/place/$1", "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P1243"), new StringStatementMapper("isrcCode", "[A-Z]{2}[A-Z0-9]{3}[0-9]{7}"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P1281"), new ExternalIdentifierStatementMapper("http://www.flickr.com/places/info/$1", "[1-9][0-9]{0,9}"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P1330"), new ExternalIdentifierStatementMapper("http://musicbrainz.org/instrument/$1", "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P1407"), new ExternalIdentifierStatementMapper("http://musicbrainz.org/series/$1", "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P1566"), new ExternalIdentifierStatementMapper("http://sws.geonames.org/$1", "[1-9]\\d{0,8}"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P1657"), new ItemIdStatementMapper("contentRating"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P1716"), new ItemIdStatementMapper("brand"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P1733"), new ExternalIdentifierStatementMapper("http://store.steampowered.com/app/$1", "[1-9]\\d{0,5}"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P1874"), new ExternalIdentifierStatementMapper("http://www.netflix.com/title/$1", "\\d{6,8}"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P1953"), new ExternalIdentifierStatementMapper("http://www.discogs.com/artist/$1", "[1-9][0-9]*"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P1954"), new ExternalIdentifierStatementMapper("http://www.discogs.com/master/$1", "[1-9][0-9]*"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P1902"), new ExternalIdentifierStatementMapper("http://open.spotify.com/artist/$1", "[0-9A-Za-z]{22}"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P1968"), new ExternalIdentifierStatementMapper("http://foursquare.com/v/$1", "[0-9a-f]+"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P2002"), new ExternalIdentifierStatementMapper("http://twitter.com/$1", "[A-Za-z0-9_]{1,15}"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P2003"), new ExternalIdentifierStatementMapper("http://www.instagram.com/$1", "[a-z0-9_\\.]{1,30}"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P2013"), new ExternalIdentifierStatementMapper("http://www.facebook.com/$1", "[A-Za-zА-Яа-яёäöüßЁ0-9.-]+"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P2019"), new ExternalIdentifierStatementMapper("http://www.allmovie.com/artist/$1", "p[1-9][0-9]*"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P2037"), new ExternalIdentifierStatementMapper("http://github.com/$1", "[A-Za-z0-9]([A-Za-z0-9\\-]{0,37}[A-Za-z0-9])?"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P2205"), new ExternalIdentifierStatementMapper("http://open.spotify.com/album/$1", "[0-9A-Za-z]{22}"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P2207"), new ExternalIdentifierStatementMapper("http://open.spotify.com/track/$1", "[0-9A-Za-z]{22}"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P2397"), new ExternalIdentifierStatementMapper("http://www.youtube.com/channel/$1", "UC([A-Za-z0-9_\\-]){22}"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P2671"), new ExternalIdentifierStatementMapper("http://g.co/kg$1", "\\/g\\/[0-9a-zA-Z]+"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P2847"), new ExternalIdentifierStatementMapper("http://plus.google.com/$1", "\\d{22}|\\+[-\\w_\\u00C0-\\u00FF]+"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P2360"), new ItemIdStatementMapper("audience"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P2860"), new ItemIdStatementMapper("citation"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P3040"), new ExternalIdentifierStatementMapper("http://soundcloud.com/$1", "[a-zA-Z0-9/_-]+"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P3090"), new StringStatementMapper("flightNumber", "([A-Z]{2,3}|[A-Z][0-9]|[0-9][A-Z])\\d{1,4}[A-Z]?"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P3108"), new ExternalIdentifierStatementMapper("http://www.yelp.com/biz/$1", "[^\\s]+")); //TODO: bad validation
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P3192"), new ExternalIdentifierStatementMapper("http://www.last.fm/music/$1", "[^\\s]+")); //TODO: bad validation
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P3267"), new ExternalIdentifierStatementMapper("http://www.flickr.com/photos/$1", "[a-zA-Z0-9@]+"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P3207"), new ExternalIdentifierStatementMapper("http://vine.co/u/$1", "\\d+"));
        mapperForProperty.put(Datamodel.makeWikidataPropertyIdValue("P3417"), new ExternalIdentifierStatementMapper("http://www.quora.com/topic/$1", "[^\\s\\/]+"));
        return mapperForProperty;
    }

    public Optional<StatementMapper> getMapperForProperty(PropertyIdValue propertyId) {
        return Optional.ofNullable(mapperForProperty.get(propertyId));
    }
}
