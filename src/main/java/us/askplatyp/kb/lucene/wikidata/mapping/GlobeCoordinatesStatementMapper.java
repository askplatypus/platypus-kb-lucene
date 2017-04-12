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

import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.wikidata.wdtk.datamodel.interfaces.GlobeCoordinatesValue;
import us.askplatyp.kb.lucene.model.value.GeoCoordinatesValue;

import java.util.Collections;
import java.util.List;

/**
 * @author Thomas Pellissier Tanon
 */
class GlobeCoordinatesStatementMapper implements StatementMainGlobeCoordinatesValueMapper {

    private String targetFieldName;

    GlobeCoordinatesStatementMapper(String targetFieldName) {
        this.targetFieldName = targetFieldName;
    }

    @Override
    public List<Field> mapMainGlobeCoordinatesValue(GlobeCoordinatesValue value) throws InvalidWikibaseValueException {
        if (!value.getGlobe().equals(GlobeCoordinatesValue.GLOBE_EARTH)) {
            return Collections.emptyList(); //TODO: support other globes
        }

        return Collections.singletonList(
                new StringField(targetFieldName, valueToWKT(value), Field.Store.YES)
        );
    }

    private String valueToWKT(GlobeCoordinatesValue value) {
        return (new GeoCoordinatesValue(
                roundDegrees(value.getLatitude(), value.getPrecision()),
                roundDegrees(value.getLongitude(), value.getPrecision())
        )).getAsWKT();
    }

    private double roundDegrees(double degrees, double precision) {
        if (precision <= 0) {
            precision = 1 / 3600;
        }
        double sign = degrees > 0 ? 1 : -1;
        double reduced = Math.round(Math.abs(degrees) / precision);
        double expanded = reduced * precision;
        return sign * expanded;
    }
}
