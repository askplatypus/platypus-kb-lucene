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
import com.vividsolutions.jts.geom.Point;

/**
 * @author Thomas Pellissier Tanon
 */
public class GeoCoordinatesValue extends GeoValue {

    private Point point;

    GeoCoordinatesValue(Point point) {
        super(point);

        this.point = point;
    }

    @JsonProperty("@id")
    public String getIRI() {
        return "geo:" + getLatitude() + "," + getLongitude();
    }

    @JsonProperty("@type")
    public String getType() {
        return "GeoCoordinates";
    }

    @JsonProperty("latitude")
    public double getLatitude() {
        return point.getY();
    }

    @JsonProperty("longitude")
    public double getLongitude() {
        return point.getX();
    }
}
