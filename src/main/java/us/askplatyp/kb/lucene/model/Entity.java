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

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * @author Thomas Pellissier Tanon
 */
public class Entity {

    private String IRI;
    private List<String> types;
    private Map<String, Object> propertyValues;

    public Entity(String IRI, List<String> types, Map<String, Object> propertyValues) {
        this.IRI = IRI;
        this.types = types;
        this.propertyValues = propertyValues;
    }

    @JsonCreator
    public Entity(Map<String, Object> content) {
        this((String) content.get("@id"), (List) content.get("@type"), content);
    }

    @JsonProperty("@id")
    public String getIRI() {
        return IRI;
    }

    @JsonProperty("@type")
    public List<String> getTypes() {
        return types;
    }

    @JsonAnyGetter
    public Map<String, Object> getPropertyValues() {
        return propertyValues;
    }

    public Object getPropertyValue(String property) {
        return propertyValues.get(property);
    }

    @JsonAnySetter
    public void setPropertyValue(String property, Object value) {
        propertyValues.put(property, value);
    }
}
