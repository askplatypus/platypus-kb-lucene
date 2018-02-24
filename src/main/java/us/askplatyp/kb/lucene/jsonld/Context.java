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

package us.askplatyp.kb.lucene.jsonld;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import us.askplatyp.kb.lucene.model.Namespaces;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * @author Thomas Pellissier Tanon
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Context {

    private static final Map<String, Object> ID_CONTEXT = Collections.singletonMap("@type", "@id");
    private static final Map<String, Object> URI_CONTEXT = Collections.singletonMap("@type", "xsd:anyURI");
    private static final Map<String, Object> BASIC_CONTEXT = new TreeMap<>();
    private static final Map<String, Object> COLLECTION_CONTEXT = new TreeMap<>();

    static {
        BASIC_CONTEXT.put("@vocab", Namespaces.DEFAULT_NAMESPACE);
        BASIC_CONTEXT.putAll(Namespaces.NAMESPACES);
        BASIC_CONTEXT.put("detailedDescription", "goog:detailedDescription");
        BASIC_CONTEXT.put("geo:asWKT", Collections.singletonMap("@type", "geo:wktLiteral"));
        BASIC_CONTEXT.put("inLanguage", Collections.singletonMap("@type", "xsd:language"));
        BASIC_CONTEXT.put("license", ID_CONTEXT);
    }

    static {
        COLLECTION_CONTEXT.put("EntitySearchResult", "goog:EntitySearchResult");
        COLLECTION_CONTEXT.put("hydra:first", ID_CONTEXT);
        COLLECTION_CONTEXT.put("member", "hydra:member");
        COLLECTION_CONTEXT.put("hydra:next", ID_CONTEXT);
        COLLECTION_CONTEXT.put("resultScore", "goog:resultScore");
        COLLECTION_CONTEXT.put("totalItems", "hydra:totalItems");
    }

    private Map<String, Object> context;

    @JsonCreator
    Context(Map<String, Object> context) {
        this.context = context;
    }

    static Context buildBasicContext() {
        return new Context(new TreeMap<>(BASIC_CONTEXT));
    }

    static Context buildCollectionContext() {
        Context context = buildBasicContext();
        context.context.putAll(COLLECTION_CONTEXT);
        return context;
    }

    void propertyRangeIsXsdAnyUri(String property) {
        context.put(property, URI_CONTEXT);
    }

    @JsonAnyGetter
    Map<String, Object> getContext() {
        return context;
    }

    @JsonIgnore
    public Optional<String> getRange(String property) {
        Object val = property;
        while (val instanceof String && context.containsKey(val)) {
            val = context.get(val);
        }
        if (val instanceof Map) {
            return Optional.ofNullable((String) ((Map) val).get("@type"));
        } else {
            return Optional.empty();
        }
    }
}
