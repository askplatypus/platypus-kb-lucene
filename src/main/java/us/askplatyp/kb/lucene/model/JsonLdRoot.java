package us.askplatyp.kb.lucene.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

/**
 * @author Thomas Pellissier Tanon
 */
public class JsonLdRoot<T> {

    private Context context;
    private T content;

    @JsonCreator
    public JsonLdRoot(@JsonProperty("@context") Context context, @JsonUnwrapped T content) {
        this.context = context;
        this.content = content;
    }

    @JsonProperty("@context")
    public Context getContext() {
        return context;
    }

    @JsonUnwrapped
    public T getContent() {
        return content;
    }
}
