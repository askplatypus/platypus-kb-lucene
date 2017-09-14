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

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * @author Thomas Pellissier Tanon
 */
public class CalendarValue implements Value {

    private static DatatypeFactory DATATYPE_FACTORY;
    static {
        try {
            DATATYPE_FACTORY = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private XMLGregorianCalendar value;

    public CalendarValue(XMLGregorianCalendar value) {
        this.value = value;
    }

    public CalendarValue(String value) {
        this(DATATYPE_FACTORY.newXMLGregorianCalendar(value));
    }

    @Override
    @JsonProperty("@type")
    public String getType() {
        return "xsd:" + this.value.getXMLSchemaType().getLocalPart();
    }

    @Override
    @JsonProperty("@value")
    public String toString() {
        return this.value.toXMLFormat();
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object value) {
        return (value instanceof CalendarValue) && ((CalendarValue) value).value.equals(value);
    }


}
