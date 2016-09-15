package us.askplatyp.kb.lucene.wikimedia.rest.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Thomas Pellissier Tanon
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Summary {

    private String title;
    private String extract;
    private Thumbnail thumbnail;
    private String lang;
    private String dir;

    @JsonCreator
    public Summary(
            @JsonProperty("title") String title,
            @JsonProperty("extract") String extract,
            @JsonProperty("thumbnail") Thumbnail thumbnail,
            @JsonProperty("lang") String lang,
            @JsonProperty("dir") String dir
    ) {
        this.title = title;
        this.extract = extract;
        this.thumbnail = thumbnail;
        this.lang = lang;
        this.dir = dir;
    }

    public String getTitle() {
        return title;
    }

    public String getExtract() {
        return extract;
    }

    public Thumbnail getThumbnail() {
        return thumbnail;
    }

    public String getLanguageCode() {
        return lang;
    }

    public String getLanguageDirection() {
        return dir;
    }
}
