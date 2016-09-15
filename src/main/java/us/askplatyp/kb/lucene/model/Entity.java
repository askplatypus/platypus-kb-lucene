package us.askplatyp.kb.lucene.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Thomas Pellissier Tanon
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Entity {

    private String IRI;
    private String[] types;
    private String name;
    private String description;
    private String[] alternateNames;
    private String officialWebsiteIRI;
    private String[] sameAsIRIs;
    private Image image;
    private Article detailedDescription;
    private String[] rangeIncludes;

    @JsonCreator
    public Entity(
            @JsonProperty("@id") String IRI,
            @JsonProperty("@type") String[] types,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("alternateName") String[] alternateNames,
            @JsonProperty("url") String officialWebsiteIRI,
            @JsonProperty("sameAs") String[] sameAsIRIs,
            @JsonProperty("image") Image image,
            @JsonProperty("detailedDescription") Article detailedDescription,
            @JsonProperty("rangeIncludes") String[] rangeIncludes
    ) {
        this.IRI = IRI;
        this.types = types;
        this.name = name;
        this.description = description;
        this.alternateNames = alternateNames;
        this.officialWebsiteIRI = officialWebsiteIRI;
        this.sameAsIRIs = sameAsIRIs;
        this.image = image;
        this.detailedDescription = detailedDescription;
        this.rangeIncludes = rangeIncludes;
    }

    @JsonProperty("@id")
    public String getIRI() {
        return IRI;
    }

    @JsonProperty("@type")
    public String[] getTypes() {
        return types;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("alternateName")
    public String[] getAlternateNames() {
        return alternateNames;
    }

    @JsonProperty("url")
    public String getOfficialWebsiteIRI() {
        return officialWebsiteIRI;
    }

    @JsonProperty("sameAs")
    public String[] getSameAsIRIs() {
        return sameAsIRIs;
    }

    @JsonProperty("image")
    public Image getImage() {
        return image;
    }

    @JsonProperty("detailedDescription")
    public Article getDetailedDescription() {
        return detailedDescription;
    }


    @JsonProperty("rangeIncludes")
    public String[] getRangeIncludes() {
        return rangeIncludes;
    }
}
