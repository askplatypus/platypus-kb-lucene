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

package us.askplatyp.kb.lucene.wikimedia.rest;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.geojson.GeoJsonReader;
import org.apache.commons.io.IOUtils;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Thomas Pellissier Tanon
 */
public class KartographerAPI {

    private static final Pattern FEATURE_COLLECTION_PATTERN = Pattern.compile("\\{.*\"features\":\\[\\{.*\"geometry\":(.*)\\}\\].*\\}");
    private static final String EMPTY_FEATURE_COLLECTION = "{\"type\":\"FeatureCollection\",\"features\":[]}";
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    private static final GeoJsonReader GEO_JSON_READER = new GeoJsonReader(GEOMETRY_FACTORY);
    private static final Geometry EMPTY_GEOMETRY = GEOMETRY_FACTORY.createGeometryCollection(new Geometry[]{});
    private static final KartographerAPI INSTANCE = new KartographerAPI();
    private LoadingCache<String, Geometry> shapeCache = CacheBuilder.newBuilder()
            .maximumSize(16384) //TODO: configure?
            .expireAfterWrite(7, TimeUnit.DAYS)
            .build(new CacheLoader<String, Geometry>() {
                @Override
                public Geometry load(String itemId) throws IOException, ParseException {
                    return requestShapeForItemId(itemId);
                }
            });

    private KartographerAPI() {
    }

    public static KartographerAPI getInstance() {
        return INSTANCE;
    }

    public Geometry getShapeForItemId(String itemURI) throws IOException {
        if (!itemURI.startsWith("http://www.wikidata.org/entity/")) {
            return EMPTY_GEOMETRY;
        }

        String itemId = itemURI.replace("http://www.wikidata.org/entity/", "");
        try {
            return shapeCache.get(itemId);
        } catch (ExecutionException e) {
            throw new IOException(e);
        }
    }

    private Geometry requestShapeForItemId(String itemId) throws IOException, ParseException {
        URL targetURLShape = UriBuilder.fromUri("https://maps.wikimedia.org/geoshape?getgeojson=1")
                .queryParam("ids", itemId)
                .build().toURL();
        Geometry geoShape = geoGeoJSONRequest(targetURLShape);
        if (!geoShape.isEmpty()) {
            return geoShape;
        }

        URL targetURLLine = UriBuilder.fromUri("https://maps.wikimedia.org/geoline?getgeojson=1")
                .queryParam("ids", itemId)
                .build().toURL();
        return geoGeoJSONRequest(targetURLLine);
    }

    private Geometry geoGeoJSONRequest(URL targetURL) throws IOException, ParseException {
        try (InputStream inputStream = targetURL.openStream()) {
            String geoJSON = IOUtils.toString(inputStream, "UTF-8");
            if (geoJSON.equals(EMPTY_FEATURE_COLLECTION)) {
                return EMPTY_GEOMETRY;
            }

            Matcher matcher = FEATURE_COLLECTION_PATTERN.matcher(geoJSON);
            if (matcher.matches()) {
                return GEO_JSON_READER.read(matcher.group(1));
            } else {
                throw new ParseException("The GeoJSON root should be a FeatureCollection");
            }
        }
    }
}
