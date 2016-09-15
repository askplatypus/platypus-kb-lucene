package us.askplatyp.kb.lucene.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Thomas Pellissier Tanon
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Image {

    private String contentURL;

    @JsonCreator
    public Image(@JsonProperty("contentUrl") String contentURL) {
        this.contentURL = contentURL;
    }

    @JsonProperty("@type")
    public String getType() {
        return "ImageObject";
    }

    @JsonProperty("contentUrl")
    public String getContentURL() {
        return contentURL;
    }
}
