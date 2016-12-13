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

import java.util.Map;
import java.util.TreeMap;

/**
 * @author Thomas Pellissier Tanon
 */
public class Namespaces {

    public static final Map<String, String> NAMESPACES = new TreeMap<>();
    static final String DEFAULT_NAMESPACE = "http://schema.org/";
    private static final Map<String, String> SPECIAL_CASES = new TreeMap<>();

    static {
        NAMESPACES.put("goog", "http://schema.googleapis.com/");
        NAMESPACES.put("hydra", "http://www.w3.org/ns/hydra/core#");
        NAMESPACES.put("kg", "http://g.co/kg");
        NAMESPACES.put("owl", "http://www.w3.org/2002/07/owl#");
        NAMESPACES.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        NAMESPACES.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        NAMESPACES.put("schema", "http://schema.org/");
        NAMESPACES.put("wd", "http://www.wikidata.org/entity/");
        NAMESPACES.put("xsd", "http://www.w3.org/2001/XMLSchema#");

        SPECIAL_CASES.put("rdf:type", "@type");
        SPECIAL_CASES.put("rdf:Property", "Property");
        SPECIAL_CASES.put("rdfs:domain", "domain");
        SPECIAL_CASES.put("rdfs:range", "range");
        SPECIAL_CASES.put("owl:Class", "Class");
        SPECIAL_CASES.put("rdfs:Class", "Class");
        SPECIAL_CASES.put("owl:DatatypeProperty", "DatatypeProperty");
        SPECIAL_CASES.put("owl:ObjectProperty", "ObjectProperty");
        SPECIAL_CASES.put("owl:NamedIndividual", "NamedIndividual");
        SPECIAL_CASES.put("owl:Thing", "Thing");
    }

    public static String expand(String qualifiedName) {
        for (Map.Entry<String, String> specialCase : SPECIAL_CASES.entrySet()) {
            if (specialCase.getValue().equals(qualifiedName)) {
                qualifiedName = specialCase.getKey();
                break;
            }
        }

        int namespaceEnd = qualifiedName.indexOf(':');
        if (namespaceEnd == -1) {
            return DEFAULT_NAMESPACE + qualifiedName;
        }

        String prefix = qualifiedName.substring(0, namespaceEnd);
        try {
            return NAMESPACES.get(prefix) + qualifiedName.substring(namespaceEnd + 1);
        } catch (NullPointerException e) {
            return qualifiedName;
        }
    }

    public static String reduce(String IRI) {
        for (Map.Entry<String, String> namespace : NAMESPACES.entrySet()) {
            if (IRI.startsWith(namespace.getValue())) {
                IRI = namespace.getKey() + ":" + IRI.substring(namespace.getValue().length());
                break;
            }
        }
        if (IRI.startsWith(DEFAULT_NAMESPACE)) {
            IRI = IRI.substring(DEFAULT_NAMESPACE.length());
        }
        if (SPECIAL_CASES.containsKey(IRI)) {
            IRI = SPECIAL_CASES.get(IRI);
        }
        return IRI;
    }
}
