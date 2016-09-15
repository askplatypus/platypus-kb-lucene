package us.askplatyp.kb.lucene.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

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
        BASIC_CONTEXT.put("hydra:first", ID_CONTEXT);
        BASIC_CONTEXT.put("inLanguage", Collections.singletonMap("@type", "xsd:language"));
        BASIC_CONTEXT.put("license", ID_CONTEXT);
        BASIC_CONTEXT.put("member", "hydra:member");
        BASIC_CONTEXT.put("hydra:next", ID_CONTEXT);
        BASIC_CONTEXT.put("rangeIncludes", ID_CONTEXT);
        BASIC_CONTEXT.put("resultScore", "goog:resultScore");
        BASIC_CONTEXT.put("sameAs", ID_CONTEXT);
        BASIC_CONTEXT.put("totalItems", "hydra:totalItems");
        BASIC_CONTEXT.put("url", ID_CONTEXT);
    }

    private Locale locale;


    @JsonCreator
    public Context(@JsonProperty("@language") String languageCode) {
        this.locale = Locale.forLanguageTag(languageCode);
    } //TODO: is it useful

    @JsonCreator
    public Context(@JsonProperty("@language") Locale locale) {
        this.locale = locale;
    }

    @JsonProperty("@language")
    public Locale getLocale() {
        return this.locale;
    }

    @JsonAnyGetter
    public Map<String, Object> getBasicContext() {
        return BASIC_CONTEXT;
    }
}
