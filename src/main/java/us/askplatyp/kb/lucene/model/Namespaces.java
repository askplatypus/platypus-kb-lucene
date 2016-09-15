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
