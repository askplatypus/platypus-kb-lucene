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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Thomas Pellissier Tanon
 */
public class GeoCoordinatesValue {

    private final static Pattern WKT_PATTERN = Pattern.compile(" *POINT\\( *([+-]?\\d+\\.?\\d*), *([+-]?\\d+\\.?\\d*) *\\) *");

    private double latitude;
    private double longitude;

    public GeoCoordinatesValue(String WKT) {
        Matcher matcher = WKT_PATTERN.matcher(WKT);
        if (matcher.matches()) {
            longitude = Double.valueOf(matcher.group(1));
            latitude = Double.valueOf(matcher.group(2));
        } else {
            throw new IllegalArgumentException("Invalid POINT WKT: " + WKT);
        }
    }

    public GeoCoordinatesValue(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @JsonProperty("@id")
    public String getIRI() {
        return "geo:" + latitude + "," + longitude;
    }

    @JsonProperty("@type")
    public String getType() {
        return "GeoCoordinates";
    }

    @JsonProperty("latitude")
    public double getLatitude() {
        return latitude;
    }

    @JsonProperty("longitude")
    public double getLongitude() {
        return longitude;
    }

    @JsonProperty("geo:asWKT")
    public String getAsWKT() {
        return "POINT(" + longitude + ", " + latitude + ")";
    }
}
