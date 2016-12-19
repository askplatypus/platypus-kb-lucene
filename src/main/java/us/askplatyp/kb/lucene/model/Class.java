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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Thomas Pellissier Tanon
 */
public class Class {

    public static final Class THING = new Class("Thing", "Any entity of the knowledge base", Collections.emptyList());
    public static final Class NAMED_INDIVIDUAL = new Class("NamedIndividual", "An entity described by the knowledge base (i.e. not a property or a class)", Collections.singletonList(THING));
    public static final Class PERSON = new Class("Person", "A person (real or fictional)", Arrays.asList(NAMED_INDIVIDUAL, THING));
    public static final Class PROPERTY = new Class("Property", "A property (for internal use only)", Collections.singletonList(THING));

    public static final List<Class> CLASSES = Arrays.asList(THING, NAMED_INDIVIDUAL, PERSON, PROPERTY);

    private String label;
    private String description;
    private List<Class> subClassOf;

    private Class(String label, String description, List<Class> subClassOf) {
        this.label = label;
        this.description = description;
        this.subClassOf = subClassOf;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public List<Class> getSubClassOf() {
        return subClassOf;
    }

    public boolean isSubClassOf(Class other) {
        return this.equals(other) || this.subClassOf.stream().anyMatch(superClass -> superClass.isSubClassOf(other));
    }
}
