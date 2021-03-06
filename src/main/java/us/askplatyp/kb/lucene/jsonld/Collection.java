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

package us.askplatyp.kb.lucene.jsonld;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author Thomas Pellissier Tanon
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Collection<T> {

    private List<T> elements;
    private long totalNumber;

    @JsonCreator
    Collection(@JsonProperty("member") List<T> elements, @JsonProperty("totalItems") long totalNumber) {
        this.elements = elements;
        this.totalNumber = totalNumber;
    }

    @JsonProperty("@type")
    public String getType() {
        return "hydra:Collection";
    }

    @JsonProperty("member")
    public List<T> getElements() {
        return this.elements;
    }

    @JsonProperty("totalItems")
    public long getTotalNumber() {
        return this.totalNumber;
    }
}
