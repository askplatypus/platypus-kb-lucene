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

package us.askplatyp.kb.lucene.wikidata;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.datamodel.interfaces.GlobeCoordinatesValue;
import org.wikidata.wdtk.datamodel.interfaces.TimeValue;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.util.Optional;

public class WikibaseValueUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(WikibaseValueUtils.class);
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();
    private static DatatypeFactory DATATYPE_FACTORY;

    static {
        try {
            DATATYPE_FACTORY = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public static Optional<XMLGregorianCalendar> toXmlGregorianCalendar(TimeValue value) {
        if (value.getBeforeTolerance() != 0 || value.getAfterTolerance() != 0) {
            return Optional.empty();
        }

        BigInteger year;
        int month = DatatypeConstants.FIELD_UNDEFINED;
        int day = DatatypeConstants.FIELD_UNDEFINED;
        int hour = DatatypeConstants.FIELD_UNDEFINED;
        int minute = DatatypeConstants.FIELD_UNDEFINED;
        int second = DatatypeConstants.FIELD_UNDEFINED;
        switch (value.getPrecision()) {
            case TimeValue.PREC_SECOND:
                second = value.getSecond();
                minute = value.getMinute();
                hour = value.getHour();
            case TimeValue.PREC_HOUR:
            case TimeValue.PREC_MINUTE:
            case TimeValue.PREC_DAY:
                day = value.getDay();
            case TimeValue.PREC_MONTH:
                month = value.getMonth();
            case TimeValue.PREC_YEAR:
                year = BigInteger.valueOf(value.getYear());
                break;
            default:
                return Optional.empty(); //TODO: Precision not supported. We ignore the value.
        }

        try {
            return Optional.of(DATATYPE_FACTORY.newXMLGregorianCalendar(year, month, day, hour, minute, second, null, 0));
        } catch (IllegalArgumentException e) {
            LOGGER.info(e.getMessage(), e);
            return Optional.empty();
        }
    }

    public static Geometry toGeometry(GlobeCoordinatesValue value) {
        return GEOMETRY_FACTORY.createPoint(new Coordinate(
                roundDegrees(value.getLongitude(), value.getPrecision()),
                roundDegrees(value.getLatitude(), value.getPrecision())
        ));
    }

    public static Optional<String> toGeoURI(GlobeCoordinatesValue value) {
        if (!value.getGlobe().equals(GlobeCoordinatesValue.GLOBE_EARTH)) {
            return Optional.empty(); //TODO: support other globes
        }
        return Optional.of("geo:" +
                roundDegrees(value.getLatitude(), value.getPrecision()) + "," +
                roundDegrees(value.getLongitude(), value.getPrecision())
        );
    }

    private static double roundDegrees(double degrees, double precision) {
        if (precision <= 0) {
            precision = 1 / 3600;
        }
        double sign = degrees > 0 ? 1 : -1;
        double reduced = Math.round(Math.abs(degrees) / precision);
        double expanded = reduced * precision;
        return sign * expanded;
    }
}
