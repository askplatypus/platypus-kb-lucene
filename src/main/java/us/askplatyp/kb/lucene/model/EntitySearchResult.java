package us.askplatyp.kb.lucene.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Thomas Pellissier Tanon
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EntitySearchResult<T> extends SearchResult<T> {

    @JsonCreator
    public EntitySearchResult(@JsonProperty("result") T result, @JsonProperty("resultScore") float score) {
        super(result, score);
    }

    @Override
    public String getType() {
        return "EntitySearchResult";
    }
}
