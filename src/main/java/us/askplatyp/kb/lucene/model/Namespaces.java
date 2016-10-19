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

    static final String DEFAULT_NAMESPACE = "http://schema.org/";
    static final Map<String, String> NAMESPACES = new TreeMap<>();

    static {
        NAMESPACES.put("goog", "http://schema.googleapis.com/");
        NAMESPACES.put("hydra", "http://www.w3.org/ns/hydra/core#");
        NAMESPACES.put("kg", "http://g.co/kg");
        NAMESPACES.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        NAMESPACES.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        NAMESPACES.put("wd", "http://www.wikidata.org/entity/");
        NAMESPACES.put("xsd", "http://www.w3.org/2001/XMLSchema#");
    }

    public static String expand(String qualifiedName) {
        if (qualifiedName.equals("@type")) {
            return "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
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
        if (IRI.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
            return "@type";
        }

        if (IRI.startsWith(DEFAULT_NAMESPACE)) {
            return IRI.substring(DEFAULT_NAMESPACE.length());
        }

        for (Map.Entry<String, String> namespace : NAMESPACES.entrySet()) {
            if (IRI.startsWith(namespace.getValue())) {
                return namespace.getKey() + ":" + IRI.substring(namespace.getValue().length());
            }
        }

        return IRI;
    }
}
