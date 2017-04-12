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
import org.apache.lucene.geo.Polygon;

/**
 * @author Thomas Pellissier Tanon
 */
public class GeoShapeValue {

    private Polygon[] polygons;

    public GeoShapeValue(Polygon[] polygons) {
        this.polygons = polygons;
    }

    private static String serializePolygonInner(org.apache.lucene.geo.Polygon polygon) {
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        double[] lats = polygon.getPolyLats();
        double[] longs = polygon.getPolyLons();
        for (int i = 0; i < longs.length; i++) {
            if (i > 0) {
                builder.append(',').append(' ');
            }
            builder.append(longs[i]);
            builder.append(' ');
            builder.append(lats[i]);
        }
        builder.append(')');
        for (org.apache.lucene.geo.Polygon hole : polygon.getHoles()) {
            builder.append(',').append(' ');
            builder.append(serializePolygonInner(hole));
        }
        return builder.toString();
    }

    @JsonProperty("@type")
    public String getType() {
        return "GeoShape";
    }

    @JsonProperty("geo:asWKT")
    public String getAsWKT() {
        if (polygons.length == 1) {
            return "POLYGON(" + serializePolygonInner(polygons[0]) + ")";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("MULTIPOLYGON(");
        for (int i = 0; i < polygons.length; i++) {
            if (i > 0) {
                builder.append(',').append(' ');
            }
            builder.append(serializePolygonInner(polygons[i]));
        }
        builder.append(')');
        return builder.toString();
    }
}
