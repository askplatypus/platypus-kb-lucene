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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Thomas Pellissier Tanon
 */
public class MapperRegistry {

    private static final Map<PropertyIdValue, StatementMapper> MAPPER_FOR_PROPERTY = new HashMap<>();

    static {
        //TODO: IMDB, LinkedIn, Myspace, Pinterest, Tumblr...
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P19"), new ItemIdStatementMapper("birthPlace"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P20"), new ItemIdStatementMapper("deathPlace"));
        //TODO: use http://schema.org/(Male|Female)? MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P21"), new ItemIdStatementMapper("gender"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P22"), new ItemIdStatementMapper("parent"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P25"), new ItemIdStatementMapper("parent"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P26"), new ItemIdStatementMapper("spouse"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P27"), new ItemIdStatementMapper("nationality"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P31"), TypeMapper.getInstance());
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P40"), new ItemIdStatementMapper("children"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P50"), new ItemIdStatementMapper("author"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P57"), new ItemIdStatementMapper("director"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P98"), new ItemIdStatementMapper("editor"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P108"), new ItemIdStatementMapper("worksFor"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P110"), new ItemIdStatementMapper("illustrator"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P112"), new ItemIdStatementMapper("founder"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P123"), new ItemIdStatementMapper("publisher"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P136"), new ItemIdStatementMapper("genre"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P161"), new ItemIdStatementMapper("actor"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P162"), new ItemIdStatementMapper("producer"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P162"), new ItemIdStatementMapper("award"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P170"), new ItemIdStatementMapper("creator"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P175"), new ItemIdStatementMapper("byArtist"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P176"), new ItemIdStatementMapper("provider"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P212"), new ISBNStatementMapper());
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P214"), new ExternalIdentifierStatementMapper("http://viaf.org/viaf/$1", "[1-9]\\d(\\d{0,7}|\\d{17,20})"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P229"), new StringStatementMapper("iataCode", "[A-Z0-9]{2}"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P230"), new StringStatementMapper("icaoCode", "[A-Z]{3}"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P236"), new ISSNStatementMapper());
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P238"), new StringStatementMapper("iataCode", "[A-Z]{3}"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P239"), new StringStatementMapper("icaoCode", "([A-Z]{2}|[CKY][A-Z0-9])[A-Z0-9]{2}"));
        //TODO: P249 tickerSymbol have ISO15022 compliant code
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P275"), new ItemIdStatementMapper("license"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P276"), new ItemIdStatementMapper("location"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P361"), new ItemIdStatementMapper("isPartOf"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P433"), new StringStatementMapper("issueNumber"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P434"), new ExternalIdentifierStatementMapper("http://musicbrainz.org/artist/$1", "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P435"), new ExternalIdentifierStatementMapper("http://musicbrainz.org/work/$1", "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P436"), new ExternalIdentifierStatementMapper("http://musicbrainz.org/release-group/$1", "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P453"), new ItemIdStatementMapper("roleName"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P463"), new ItemIdStatementMapper("memberOf"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P478"), new StringStatementMapper("volumeNumber"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P483"), new ItemIdStatementMapper("recordedAt"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P495"), new ItemIdStatementMapper("countryOfOrigin"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P527"), new ItemIdStatementMapper("hasPart"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P551"), new ItemIdStatementMapper("homeLocation"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P569"), new TimeStatementMapper("birthDate"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P570"), new TimeStatementMapper("deathDate"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P571"), new TimeStatementMapper("dateCreated"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P577"), new TimeStatementMapper("datePublished"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P625"), new GlobeCoordinatesStatementMapper("geo"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P646"), new ExternalIdentifierStatementMapper("http://g.co/kg$1", "(/m/0[0-9a-z_]{2,6}|/m/01[0123][0-9a-z_]{5})"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P655"), new ItemIdStatementMapper("translator"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P674"), new ItemIdStatementMapper("character"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P734"), new ItemIdStatementMapper("familyName"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P735"), new ItemIdStatementMapper("givenName"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P767"), new ItemIdStatementMapper("contributor"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P840"), new ItemIdStatementMapper("contentLocation"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P856"), new URIStatementMapper("url"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P859"), new ItemIdStatementMapper("sponsor"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P921"), new ItemIdStatementMapper("about"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P957"), new ISBNStatementMapper());
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P966"), new ExternalIdentifierStatementMapper("http://musicbrainz.org/label/$1", "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P982"), new ExternalIdentifierStatementMapper("http://musicbrainz.org/area/$1", "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P1004"), new ExternalIdentifierStatementMapper("http://musicbrainz.org/place/$1", "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P1243"), new StringStatementMapper("isrcCode", "[A-Z]{2}[A-Z0-9]{3}[0-9]{7}"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P1281"), new ExternalIdentifierStatementMapper("http://www.flickr.com/places/info/$1", "[1-9][0-9]{0,9}"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P1330"), new ExternalIdentifierStatementMapper("http://musicbrainz.org/instrument/$1", "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P1407"), new ExternalIdentifierStatementMapper("http://musicbrainz.org/series/$1", "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P1566"), new ExternalIdentifierStatementMapper("http://sws.geonames.org/$1", "[1-9]\\d{0,8}"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P1657"), new ItemIdStatementMapper("contentRating"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P1716"), new ItemIdStatementMapper("brand"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P1733"), new ExternalIdentifierStatementMapper("http://store.steampowered.com/app/$1", "[1-9]\\d{0,5}"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P1874"), new ExternalIdentifierStatementMapper("http://www.netflix.com/title/$1", "\\d{6,8}"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P1953"), new ExternalIdentifierStatementMapper("http://www.discogs.com/artist/$1", "[1-9][0-9]*"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P1954"), new ExternalIdentifierStatementMapper("http://www.discogs.com/master/$1", "[1-9][0-9]*"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P1902"), new ExternalIdentifierStatementMapper("http://open.spotify.com/artist/$1", "[0-9A-Za-z]{22}"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P1968"), new ExternalIdentifierStatementMapper("http://foursquare.com/v/$1", "[0-9a-f]+"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P2002"), new ExternalIdentifierStatementMapper("http://twitter.com/$1", "[A-Za-z0-9_]{1,15}"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P2003"), new ExternalIdentifierStatementMapper("http://www.instagram.com/$1", "[a-z0-9_\\.]{1,30}"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P2013"), new ExternalIdentifierStatementMapper("http://www.facebook.com/$1", "[A-Za-zА-Яа-яёäöüßЁ0-9.-]+"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P2019"), new ExternalIdentifierStatementMapper("http://www.allmovie.com/artist/$1", "p[1-9][0-9]*"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P2037"), new ExternalIdentifierStatementMapper("http://github.com/$1", "[A-Za-z0-9]([A-Za-z0-9\\-]{0,37}[A-Za-z0-9])?"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P2205"), new ExternalIdentifierStatementMapper("http://open.spotify.com/album/$1", "[0-9A-Za-z]{22}"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P2207"), new ExternalIdentifierStatementMapper("http://open.spotify.com/track/$1", "[0-9A-Za-z]{22}"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P2397"), new ExternalIdentifierStatementMapper("http://www.youtube.com/channel/$1", "UC([A-Za-z0-9_\\-]){22}"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P2671"), new ExternalIdentifierStatementMapper("http://g.co/kg$1", "\\/g\\/[0-9a-zA-Z]+"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P2847"), new ExternalIdentifierStatementMapper("http://plus.google.com/$1", "\\d{22}|\\+[-\\w_\\u00C0-\\u00FF]+"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P2360"), new ItemIdStatementMapper("audience"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P2860"), new ItemIdStatementMapper("citation"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P3040"), new ExternalIdentifierStatementMapper("http://soundcloud.com/$1", "[a-zA-Z0-9/_-]+"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P3090"), new StringStatementMapper("flightNumber", "([A-Z]{2,3}|[A-Z][0-9]|[0-9][A-Z])\\d{1,4}[A-Z]?"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P3108"), new ExternalIdentifierStatementMapper("http://www.yelp.com/biz/$1", "[^\\s]+")); //TODO: bad validation
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P3192"), new ExternalIdentifierStatementMapper("http://www.last.fm/music/$1", "[^\\s]+")); //TODO: bad validation
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P3267"), new ExternalIdentifierStatementMapper("http://www.flickr.com/photos/$1", "[a-zA-Z0-9@]+"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P3207"), new ExternalIdentifierStatementMapper("http://vine.co/u/$1", "\\d+"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P3417"), new ExternalIdentifierStatementMapper("http://www.quora.com/topic/$1", "[^\\s\\/]+"));
    }

    public static Optional<StatementMapper> getMapperForProperty(PropertyIdValue propertyId) {
        return Optional.ofNullable(MAPPER_FOR_PROPERTY.get(propertyId));
    }
}
