package us.askplatyp.kb.lucene.wikidata.mapping;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.wikidata.wdtk.datamodel.interfaces.StringValue;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Thomas Pellissier Tanon
 */
class ExternalIdentifierStatementMapper implements StatementMainStringValueMapper {

    private String targetFieldName;
    private String URITemplate;
    private Pattern pattern;

    ExternalIdentifierStatementMapper(String targetFieldName, String URITemplate, String pattern) {
        this.targetFieldName = targetFieldName;
        this.URITemplate = URITemplate;
        this.pattern = Pattern.compile(pattern);
    }

    @Override
    public List<Field> mapMainStringValue(StringValue value) throws InvalidWikibaseValueException {
        if (!pattern.matcher(value.getString()).matches()) {
            throw new InvalidWikibaseValueException(value + " is not a valid identifier. It does not matches the pattern " + pattern);
        }
        return Collections.singletonList(new StoredField(targetFieldName, URITemplate.replace("$1", value.getString())));
    }
}

