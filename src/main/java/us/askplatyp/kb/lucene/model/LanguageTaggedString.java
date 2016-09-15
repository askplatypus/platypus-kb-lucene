package us.askplatyp.kb.lucene.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Thomas pellissier Tanon
 */
public class LanguageTaggedString {

    private String value;
    private String languageCode;

    @JsonCreator
    public LanguageTaggedString(@JsonProperty("@value") String value, @JsonProperty("@language") String languageCode) {
        this.value = value;
        this.languageCode = languageCode;
    }

    @JsonProperty("@value")
    public String getValue() {
        return this.value;
    }

    @JsonProperty("@language")
    public String getLanguageCode() {
        return this.languageCode;
    }
}
