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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Thomas Pellissier Tanon
 */
public class DatatypeProperty {

    public static final List<DatatypeProperty> SIMPLE_PROPERTIES = Arrays.asList(
            new DatatypeProperty("name", "The entity name", Collections.singletonList(Class.THING), Datatype.LANGUAGE_TAGGED_STRING, false),
            new DatatypeProperty("description", "A description of the entity", Collections.singletonList(Class.THING), Datatype.LANGUAGE_TAGGED_STRING, false),
            new DatatypeProperty("alternateName", "Alternative names for the entity", Collections.singletonList(Class.THING), Datatype.LANGUAGE_TAGGED_STRING, true),
            new DatatypeProperty("url", "URL of the official website of the entity", Collections.singletonList(Class.NAMED_INDIVIDUAL), Datatype.STRING, false),
            new DatatypeProperty("sameAs", "Authoritative URLs about the entity", Collections.singletonList(Class.NAMED_INDIVIDUAL), Datatype.STRING, true),
            new DatatypeProperty("range", "Range of the property", Collections.singletonList(Class.PROPERTY), Datatype.STRING, false)
    );
    public static final List<DatatypeProperty> PROPERTIES = new ArrayList<>(SIMPLE_PROPERTIES);

    static {
        PROPERTIES.add(new DatatypeProperty("detailedDescription", "An article describing the entity", Collections.singletonList(Class.NAMED_INDIVIDUAL), Datatype.ARTICLE, false));
        PROPERTIES.add(new DatatypeProperty("image", "An image of the entity", Collections.singletonList(Class.NAMED_INDIVIDUAL), Datatype.IMAGE, false));
        PROPERTIES.add(new DatatypeProperty("birthDate", "Birth date of the person", Collections.singletonList(Class.PERSON), Datatype.CALENDAR, false));
        PROPERTIES.add(new DatatypeProperty("deathDate", "Death date of the person", Collections.singletonList(Class.PERSON), Datatype.CALENDAR, false));
    }

    private String label;
    private String description;
    private List<Class> domains;
    private Datatype range;
    private boolean multipleValues;

    private DatatypeProperty(String label, String description, List<Class> domains, Datatype range, boolean multipleValues) {
        this.label = label;
        this.description = description;
        this.domains = domains;
        this.range = range;
        this.multipleValues = multipleValues;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public List<Class> getDomains() {
        return domains;
    }

    public Datatype getRange() {
        return range;
    }

    public boolean withMultipleValues() {
        return multipleValues;
    }

    public enum Datatype {STRING, LANGUAGE_TAGGED_STRING, CALENDAR, ARTICLE, IMAGE}
}
