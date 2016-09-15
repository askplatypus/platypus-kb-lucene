package us.askplatyp.kb.lucene.wikidata.mapping;

/**
 * @author Thomas Pellissier Tanon
 */
public class InvalidWikibaseValueException extends Exception {

    InvalidWikibaseValueException(String message) {
        super(message);
    }

    InvalidWikibaseValueException(String message, Throwable cause) {
        super(message, cause);
    }
}
