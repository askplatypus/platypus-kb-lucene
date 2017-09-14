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

import org.wikidata.wdtk.datamodel.interfaces.TimeValue;
import us.askplatyp.kb.lucene.model.Claim;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.util.stream.Stream;

/**
 * @author Thomas Pellissier Tanon
 */
class TimeStatementMapper implements StatementMainTimeValueMapper {

    private static DatatypeFactory DATATYPE_FACTORY;

    static {
        try {
            DATATYPE_FACTORY = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private String targetFieldName;

    TimeStatementMapper(String targetFieldName) {
        this.targetFieldName = targetFieldName;
    }

    @Override
    public Stream<Claim> mapMainTimeValue(TimeValue value) throws InvalidWikibaseValueException {
        return convertTimeValue(value).map(calendarValue ->
                new Claim(targetFieldName, calendarValue)
        );
    }

    private Stream<XMLGregorianCalendar> convertTimeValue(TimeValue value) throws InvalidWikibaseValueException {
        if (value.getBeforeTolerance() != 0 || value.getAfterTolerance() != 0) {
            throw new InvalidWikibaseValueException("Time values with before/after tolerances are not supported.");
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
                return Stream.empty(); //TODO: Precision not supported. We ignore the value.
        }

        try {
            return Stream.of(DATATYPE_FACTORY.newXMLGregorianCalendar(year, month, day, hour, minute, second, null, 0));
        } catch (IllegalArgumentException e) {
            throw new InvalidWikibaseValueException("Calendar value not supported by Java", e);
        }
    }
}
