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

package us.askplatyp.kb.lucene.model;

import us.askplatyp.kb.lucene.model.value.ResourceValue;
import us.askplatyp.kb.lucene.model.value.Value;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;


public class Resource {
    private String IRI;
    private Set<String> types = new HashSet<>();
    private Set<Claim> claims = new HashSet<>();

    public Resource(String IRI) {
        this.IRI = Namespaces.reduce(IRI);
    }

    public String getIRI() {
        return IRI;
    }

    public Stream<String> getTypes() {
        return types.stream();
    }

    public void addType(String typeIRI) {
        types.add(Namespaces.reduce(typeIRI));
    }

    public Stream<Claim> getClaims() {
        return claims.stream();
    }

    public Stream<Claim> getClaimsForProperty(String propertyIRI) {
        return claims.stream().filter(claim -> claim.getProperty().equals(propertyIRI));
    }

    public Stream<Value> getValuesForProperty(String propertyIRI) {
        return getClaimsForProperty(propertyIRI).map(Claim::getValue);
    }

    public Stream<String> getStringValuesForProperty(String propertyIRI) {
        return getValuesForProperty(propertyIRI).map(Value::toString);
    }

    public void addClaim(Claim claim) {
        if (claim.getProperty().equals("@type")) {
            if (claim.getValue() instanceof ResourceValue) {
                types.add(claim.getValue().toString());
            } else {
                throw new IllegalArgumentException("The range of rdf:type is Resource");
            }
        } else {
            claims.add(claim);
        }
    }

    public void addClaim(String property, Value value) {
        addClaim(new Claim(property, value));
    }
}
