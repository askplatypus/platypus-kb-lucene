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

import com.fasterxml.jackson.annotation.*;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Thomas Pellissier Tanon
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Context {

    private static final Map<String, Object> ID_CONTEXT = Collections.singletonMap("@type", "@id");
    private static final Map<String, Object> BASIC_CONTEXT = new TreeMap<>();

    static {
        BASIC_CONTEXT.put("@vocab", Namespaces.DEFAULT_NAMESPACE);
        BASIC_CONTEXT.putAll(Namespaces.NAMESPACES);
        BASIC_CONTEXT.put("detailedDescription", "goog:detailedDescription");
        BASIC_CONTEXT.put("EntitySearchResult", "goog:EntitySearchResult");
        BASIC_CONTEXT.put("Class", "owl:Class");
        BASIC_CONTEXT.put("DatatypeProperty", "owl:DatatypeProperty");
        BASIC_CONTEXT.put("contentUrl", ID_CONTEXT);
        BASIC_CONTEXT.put("geo:asWKT", Collections.singletonMap("@type", "geo:wktLiteral"));
        BASIC_CONTEXT.put("hydra:first", ID_CONTEXT);
        BASIC_CONTEXT.put("inLanguage", Collections.singletonMap("@type", "xsd:language"));
        BASIC_CONTEXT.put("license", ID_CONTEXT);
        BASIC_CONTEXT.put("member", "hydra:member");
        BASIC_CONTEXT.put("NamedIndividual", "owl:NamedIndividual");
        BASIC_CONTEXT.put("hydra:next", ID_CONTEXT);
        BASIC_CONTEXT.put("ObjectProperty", "owl:ObjectProperty");
        BASIC_CONTEXT.put("Property", "rdf:Property");
        BASIC_CONTEXT.put("resultScore", "goog:resultScore");
        BASIC_CONTEXT.put("sameAs", ID_CONTEXT);
        BASIC_CONTEXT.put("totalItems", "hydra:totalItems");
        BASIC_CONTEXT.put("url", ID_CONTEXT);
    }

    private Locale locale;

    public Context(Locale locale) {
        this.locale = locale;
    }

    @JsonCreator
    public Context(@JsonProperty("@language") String languageCode) {
        this(Locale.forLanguageTag(languageCode));
    }

    @JsonIgnore
    public Locale getLocale() {
        return this.locale;
    }

    @JsonProperty("@language")
    public String getLanguageCode() {
        return this.locale.toLanguageTag();
    }

    @JsonAnyGetter
    public Map<String, Object> getBasicContext() {
        return BASIC_CONTEXT;
    }
}
