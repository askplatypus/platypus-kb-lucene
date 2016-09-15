package us.askplatyp.kb.lucene.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Thomas Pellissier Tanon
 */
abstract class SearchResult<T> {

    private T result;
    private float score;

    SearchResult(T result, float score) {
        this.result = result;
        this.score = score;
    }

    @JsonProperty("@type")
    public abstract String getType();

    @JsonProperty("result")
    public T getResult() {
        return this.result;
    }

    @JsonProperty("resultScore")
    public float getScore() {
        return this.score;
    }
}
