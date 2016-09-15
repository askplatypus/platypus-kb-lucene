package us.askplatyp.kb.lucene.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author Thomas Pellissier Tanon
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Collection<T> {

    private List<T> elements;
    private int totalNumber;

    @JsonCreator
    Collection(@JsonProperty("member") List<T> elements, @JsonProperty("totalItems") int totalNumber) {
        this.elements = elements;
        this.totalNumber = totalNumber;
    }

    @JsonProperty("@type")
    public String getType() {
        return "hydra:Collection";
    }

    @JsonProperty("member")
    public List<T> getElements() {
        return this.elements;
    }

    @JsonProperty("totalItems")
    public int getTotalNumber() {
        return this.totalNumber;
    }
}
