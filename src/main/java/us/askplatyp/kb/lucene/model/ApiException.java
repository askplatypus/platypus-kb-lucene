package us.askplatyp.kb.lucene.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author Thomas Pellissier Tanon
 */
@JsonSerialize(as = ApiError.class)
public class ApiException extends Exception implements ApiError {

    private static Map<String, Object> CONTEXT = new TreeMap<>();

    static {
        CONTEXT.put("@vocab", "http://schema.org/");
    }

    private int httpStatus = 500;

    public ApiException(Throwable cause) {
        super(cause);
    }

    public ApiException(String message) {
        super(message);
    }

    public ApiException(String message, int httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApiException(String message, Throwable cause, int httpStatus) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    public Map<String, Object> getContext() {
        return CONTEXT;
    }

    public String getType() {
        return "Error";
    }

    public int getStatus() {
        return httpStatus;
    }
}
