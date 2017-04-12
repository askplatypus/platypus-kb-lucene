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

package us.askplatyp.kb.lucene.wikimedia.rest;

import jersey.repackaged.com.google.common.cache.CacheBuilder;
import jersey.repackaged.com.google.common.cache.CacheLoader;
import jersey.repackaged.com.google.common.cache.LoadingCache;
import org.apache.commons.io.IOUtils;
import org.apache.lucene.geo.Polygon;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author Thomas Pellissier Tanon
 */
public class KartographerAPI { //TODO: check name

    private static final KartographerAPI INSTANCE = new KartographerAPI();
    private LoadingCache<String, Optional<Polygon[]>> shapeCache = CacheBuilder.newBuilder()
            .maximumSize(16384) //TODO: configure?
            .expireAfterWrite(1, TimeUnit.DAYS)
            .build(new CacheLoader<String, Optional<Polygon[]>>() {
                @Override
                public Optional<Polygon[]> load(String itemId) throws IOException, ParseException {
                    return requestShapeForItemId(itemId);
                }
            });

    private KartographerAPI() {
    }

    public static KartographerAPI getInstance() {
        return INSTANCE;
    }

    public Optional<Polygon[]> getShapeForItemId(String itemURI) throws IOException {
        if (!itemURI.startsWith("http://www.wikidata.org/entity/")) {
            return Optional.empty();
        }

        String itemId = itemURI.replace("http://www.wikidata.org/entity/", "");
        try {
            return shapeCache.get(itemId);
        } catch (ExecutionException e) {
            throw new IOException(e);
        }
    }

    private Optional<Polygon[]> requestShapeForItemId(String itemId) throws IOException, ParseException {
        URL targetURL = UriBuilder.fromUri("https://maps.wikimedia.org/geoshape?getgeojson=1")
                .queryParam("ids", itemId)
                .build().toURL();
        return geoGeoJSONRequest(targetURL);
        //TODO: support geolines
    }

    private Optional<Polygon[]> geoGeoJSONRequest(URL targetURL) throws IOException, ParseException {
        try (InputStream inputStream = targetURL.openStream()) {
            String value = IOUtils.toString(inputStream);
            if (value.equals("{\"type\":\"FeatureCollection\",\"features\":[]}")) {
                return Optional.empty();
            }
            return Optional.of(Polygon.fromGeoJSON(value));
        }
    }
}
