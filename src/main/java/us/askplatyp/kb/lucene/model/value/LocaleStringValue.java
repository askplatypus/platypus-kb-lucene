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

package us.askplatyp.kb.lucene.model.value;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Locale;

/**
 * @author Thomas Pellissier Tanon
 */
public class LocaleStringValue implements Value {

    private String value;

    private Locale locale;

    public LocaleStringValue(String value, Locale locale) {
        this.value = value;
        this.locale = locale;
    }

    public LocaleStringValue(String value, String languageCode) {
        this(value, Locale.forLanguageTag(languageCode));
    }

    public String getType() {
        return "rdf:langString";
    }

    public Locale getLocale() {
        return locale;
    }

    @JsonProperty("@language")
    public String getLanguageCode() {
        return locale.toLanguageTag();
    }

    @Override
    @JsonProperty("@value")
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object value) {
        return (value instanceof LocaleStringValue) && ((LocaleStringValue) value).value.equals(value);
    }
}
