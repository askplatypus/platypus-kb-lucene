package us.askplatyp.kb.lucene.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * @author Thomas Pellissier Tanon
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PartialCollection<T> extends Collection<T> {

    private PartialCollectionView view;

    @JsonCreator
    public PartialCollection(
            @JsonProperty("element") List<T> elements,
            @JsonProperty("totalItems") int totalNumber,
            @JsonProperty("hydra:first") String baseIRI,
            String currentContinue, //TODO
            String nextContinue
    ) {
        super(elements, totalNumber);

        this.view = new PartialCollectionView(baseIRI, currentContinue, nextContinue);
    }

    private static String encode(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    @JsonProperty("hydra:view")
    public PartialCollectionView getView() {
        return view;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private static class PartialCollectionView {

        private String baseIRI;
        private String currentContinue;
        private String nextContinue;

        PartialCollectionView(String baseIRI, String currentContinue, String nextContinue) {
            this.baseIRI = baseIRI;
            this.currentContinue = currentContinue;
            this.nextContinue = nextContinue;
        }

        @JsonProperty("@id")
        public String getId() {
            return (currentContinue == null) ? baseIRI : baseIRI + "&continue=" + encode(currentContinue);
        }

        @JsonProperty("@type")
        public String getType() {
            return "hydra:PartialCollectionView";
        }

        @JsonProperty("hydra:first")
        public String getFirst() {
            return baseIRI;
        }

        @JsonProperty("hydra:next")
        public String getNext() {
            return (nextContinue == null) ? null : baseIRI + "&continue=" + encode(nextContinue);
        }
    }
}
