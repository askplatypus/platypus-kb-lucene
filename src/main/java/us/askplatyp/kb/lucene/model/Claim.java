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

package us.askplatyp.kb.lucene.model;

import us.askplatyp.kb.lucene.model.value.*;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.util.Locale;

public class Claim {
    private String property;
    private Value value;

    public Claim(String property, Value value) {
        this.property = Namespaces.reduce(property);
        this.value = value;
    }

    public Claim(String property, String value) {
        this(property, new StringValue(value));
    }

    public Claim(String property, String value, Locale locale) {
        this(property, new LocaleStringValue(value, locale));
    }

    public Claim(String property, String value, String languageCode) {
        this(property, new LocaleStringValue(value, languageCode));
    }

    public Claim(String property, XMLGregorianCalendar value) {
        this(property, new CalendarValue(value));
    }

    public Claim(String property, BigInteger value) {
        this(property, new IntegerValue(value));
    }

    public String getProperty() {
        return property;
    }

    public Value getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return property.hashCode() ^ value.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Claim)) {
            return false;
        }
        Claim claim = (Claim) other;
        return property.equals(claim.property) && value.equals(claim.value);
    }
}
