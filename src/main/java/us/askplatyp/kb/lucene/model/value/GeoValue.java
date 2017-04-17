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

package us.askplatyp.kb.lucene.model.value;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

/**
 * @author Thomas Pellissier Tanon
 */
public abstract class GeoValue {

    private static final WKTReader WKT_READER = new WKTReader();
    private static final WKTWriter WKT_WRITER = new WKTWriter();
    private Geometry geometry;

    GeoValue(Geometry geometry) {
        this.geometry = geometry;
    }

    public static GeoValue buildGeoValue(String WKT) {
        try {
            return buildGeoValue(WKT_READER.read(WKT));
        } catch (ParseException e) {
            throw new IllegalArgumentException(WKT + " should be a valid Well-Known-Text string", e);
        }
    }

    public static GeoValue buildGeoValue(Geometry geometry) {
        if (geometry instanceof Point) {
            return new GeoCoordinatesValue((Point) geometry);
        } else {
            return new GeoShapeValue(geometry);
        }
    }

    @JsonProperty("geo:asWKT")
    public String getAsWKT() {
        return WKT_WRITER.write(geometry);
    }
}
