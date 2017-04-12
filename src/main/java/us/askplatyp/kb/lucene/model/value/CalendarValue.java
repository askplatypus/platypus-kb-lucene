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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import us.askplatyp.kb.lucene.model.Namespaces;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * @author Thomas Pellissier Tanon
 */
public class CalendarValue {

    private static DatatypeFactory DATATYPE_FACTORY;
    static {
        try {
            DATATYPE_FACTORY = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private XMLGregorianCalendar value;

    public CalendarValue(String value) {
        this.value = DATATYPE_FACTORY.newXMLGregorianCalendar(value);
    }

    @JsonCreator
    public CalendarValue(@JsonProperty("@value") String value, @JsonProperty("@type") String type) {
        this(value);

        if (!this.getType().equals(Namespaces.reduce(type))) {
            throw new IllegalArgumentException(value + " should have the datatype " + this.getType() + " and not " + type);
        }
    }

    @JsonProperty("@value")
    public String getValue() {
        return this.value.toXMLFormat();
    }

    @JsonProperty("@type")
    public String getType() {
        return "xsd:" + this.value.getXMLSchemaType().getLocalPart();
    }
}
