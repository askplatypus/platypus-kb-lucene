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

package us.askplatyp.kb.lucene.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Thomas pellissier Tanon
 */
public class LanguageTaggedString {

    private String value;
    private String languageCode;

    @JsonCreator
    public LanguageTaggedString(@JsonProperty("@value") String value, @JsonProperty("@language") String languageCode) {
        this.value = value;
        this.languageCode = languageCode;
    }

    @JsonProperty("@value")
    public String getValue() {
        return this.value;
    }

    @JsonProperty("@language")
    public String getLanguageCode() {
        return this.languageCode;
    }
}
