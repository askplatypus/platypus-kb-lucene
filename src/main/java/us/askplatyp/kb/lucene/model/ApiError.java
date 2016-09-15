package us.askplatyp.kb.lucene.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * @author Thomas Pellissier Tanon
 */
interface ApiError {

    @JsonProperty("@context")
    Map<String, Object> getContext();

    @JsonProperty("@type")
    String getType();

    @JsonProperty("message")
    String getMessage();
}
