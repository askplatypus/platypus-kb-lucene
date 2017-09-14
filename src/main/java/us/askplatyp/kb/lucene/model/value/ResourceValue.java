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
import us.askplatyp.kb.lucene.model.Namespaces;

/**
 * @author Thomas Pellissier Tanon
 */
public class ResourceValue implements Value {

    private String IRI;

    public ResourceValue(String IRI) {
        this.IRI = Namespaces.reduce(IRI);
    }

    @Override
    @JsonProperty("@type")
    public String getType() {
        return "@id";
    }

    @Override
    @JsonProperty("@value")
    public String toString() {
        return IRI;
    }

    @Override
    public int hashCode() {
        return IRI.hashCode();
    }

    @Override
    public boolean equals(Object value) {
        return (value instanceof ResourceValue) && ((ResourceValue) value).IRI.equals(value);
    }
}
