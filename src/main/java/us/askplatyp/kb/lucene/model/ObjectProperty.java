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
public class ObjectProperty {

    public static final List<ObjectProperty> PROPERTIES = Arrays.asList(
            new ObjectProperty("birthPlace", "Birth place of the person", Collections.singletonList(Class.PERSON), Class.PERSON, false),
            new ObjectProperty("children", "Children of the person", Collections.singletonList(Class.PERSON), Class.COUNTRY, true),
            new ObjectProperty("deathPlace", "Death place of the person", Collections.singletonList(Class.PERSON), Class.PERSON, false),
            new ObjectProperty("nationality", "Nationality of the person", Collections.singletonList(Class.PERSON), Class.COUNTRY, false),
            new ObjectProperty("parent", "Parents of the person", Collections.singletonList(Class.PERSON), Class.PERSON, true)
            //TODO Role? new ObjectProperty("spouse", "Spouse of the person", Collections.singletonList(Class.PERSON), Class.PERSON, true)
    );

    private String label;
    private String description;
    private List<Class> domains;
    private Class range;
    private boolean multipleValues;

    private ObjectProperty(String label, String description, List<Class> domains, Class range, boolean multipleValues) {
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

    public Class getRange() {
        return range;
    }

    public boolean withMultipleValues() {
        return multipleValues;
    }
}
