/*
 * Copyright (c) 2016 Platypus Knowledge Base developers.
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
import org.apache.lucene.document.StoredField;
import org.wikidata.wdtk.datamodel.interfaces.TimeValue;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

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
    public List<Field> mapMainTimeValue(TimeValue value) throws InvalidWikibaseValueException {
        return Collections.singletonList(new StoredField(targetFieldName, convertTimeValue(value).toXMLFormat()));
    }

    private XMLGregorianCalendar convertTimeValue(TimeValue value) throws InvalidWikibaseValueException {
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
            case TimeValue.PREC_DAY:
                day = value.getDay();
            case TimeValue.PREC_MONTH:
                month = value.getMonth();
            case TimeValue.PREC_YEAR:
                year = BigInteger.valueOf(value.getYear());
                break;
            default:
                throw new InvalidWikibaseValueException(value.getPrecision() + " is not a supported precision.");
        }
        return DATATYPE_FACTORY.newXMLGregorianCalendar(year, month, day, hour, minute, second, null, 0);
    }
}
