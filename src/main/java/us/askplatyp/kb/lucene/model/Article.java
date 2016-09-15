package us.askplatyp.kb.lucene.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Thomas Pellissier Tanon
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Article {

    private String IRI;
    private String title;
    private String articleHeadlines;
    private String licenseIRI;
    private String languageCode;

    @JsonCreator
    public Article(
            @JsonProperty("@id") String IRI,
            @JsonProperty("name") String title,
            @JsonProperty("articleBody") String articleHeadlines,
            @JsonProperty("license") String licenseIRI,
            @JsonProperty("inLanguage") String languageCode
    ) {
        this.IRI = IRI;
        this.title = title;
        this.articleHeadlines = articleHeadlines;
        this.licenseIRI = licenseIRI;
        this.languageCode = languageCode;
    }

    @JsonProperty("@id")
    public String getIRI() {
        return IRI;
    }

    @JsonProperty("@type")
    public String getType() {
        return "Article";
    }

    @JsonProperty("name")
    public String getTitle() {
        return title;
    }

    @JsonProperty("articleBody")
    public String getArticleHeadlines() {
        return articleHeadlines;
    }

    @JsonProperty("license")
    public String getLicenseIRI() {
        return licenseIRI;
    }

    @JsonProperty("inLanguage")
    public String getLanguageCode() {
        return languageCode;
    }
}
