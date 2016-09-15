package us.askplatyp.kb.lucene.wikidata.mapping;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.wikidata.wdtk.datamodel.interfaces.StringValue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

/**
 * @author Thomas Pellissier Tanon
 */
class URIStatementMapper implements StatementMainStringValueMapper {

    private String targetFieldName;

    URIStatementMapper(String targetFieldName) {
        this.targetFieldName = targetFieldName;
    }

    @Override
    public List<Field> mapMainStringValue(StringValue value) throws InvalidWikibaseValueException {
        try {
            URI parsedURL = new URI(value.getString()).normalize();
            if ((parsedURL.getScheme().equals("http") || parsedURL.getScheme().equals("https")) && parsedURL.getPath().isEmpty()) {
                parsedURL = parsedURL.resolve("/");
            }
            return Collections.singletonList(new StoredField(targetFieldName, parsedURL.toString()));
        } catch (URISyntaxException e) {
            throw new InvalidWikibaseValueException(value + " is an invalid URI", e);
        }
    }
}
