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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.wikidata.wdtk.datamodel.interfaces.GlobeCoordinatesValue;
import us.askplatyp.kb.lucene.model.Claim;
import us.askplatyp.kb.lucene.model.value.GeoValue;

import java.util.stream.Stream;

/**
 * @author Thomas Pellissier Tanon
 */
class GlobeCoordinatesStatementMapper implements StatementMainGlobeCoordinatesValueMapper {
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    private String targetFieldName;

    GlobeCoordinatesStatementMapper(String targetFieldName) {
        this.targetFieldName = targetFieldName;
    }

    @Override
    public Stream<Claim> mapMainGlobeCoordinatesValue(GlobeCoordinatesValue value) {
        if (!value.getGlobe().equals(GlobeCoordinatesValue.GLOBE_EARTH)) {
            return Stream.empty(); //TODO: support other globes
        }

        return Stream.of(
                new Claim(targetFieldName, GeoValue.buildGeoValue(valueToGeometry(value)))
        );
    }

    private Geometry valueToGeometry(GlobeCoordinatesValue value) {
        return GEOMETRY_FACTORY.createPoint(new Coordinate(
                roundDegrees(value.getLongitude(), value.getPrecision()),
                roundDegrees(value.getLatitude(), value.getPrecision())
        ));
    }

    private double roundDegrees(double degrees, double precision) {
        if (precision <= 0) {
            precision = 1 / 3600d;
        }
        double sign = degrees > 0 ? 1 : -1;
        double reduced = Math.round(Math.abs(degrees) / precision);
        double expanded = reduced * precision;
        return sign * expanded;
    }
}
