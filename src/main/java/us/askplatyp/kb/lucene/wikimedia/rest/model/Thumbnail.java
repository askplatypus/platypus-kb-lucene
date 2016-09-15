package us.askplatyp.kb.lucene.wikimedia.rest.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Thomas Pellissier Tanon
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Thumbnail {

    private String source;
    private int width;
    private int height;

    @JsonCreator
    public Thumbnail(@JsonProperty("source") String source, @JsonProperty("width") int width, @JsonProperty("height") int height) {
        this.source = source;
        this.width = width;
        this.height = height;
    }

    public String getSource() {
        return source;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
