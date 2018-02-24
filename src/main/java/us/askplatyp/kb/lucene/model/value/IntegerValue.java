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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigInteger;

/**
 * @author Thomas Pellissier Tanon
 */
public class IntegerValue implements Value {

    private BigInteger value;

    public IntegerValue(BigInteger value) {
        this.value = value;
    }

    @Override
    @JsonProperty("@type")
    public String getType() {
        return "xsd:integer";
    }

    @JsonProperty("@value")
    public BigInteger getValue() {
        return value;
    }

    @Override
    @JsonIgnore
    public String toString() {
        return value.toString();
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof IntegerValue) && ((IntegerValue) other).value.equals(value);
    }
}
