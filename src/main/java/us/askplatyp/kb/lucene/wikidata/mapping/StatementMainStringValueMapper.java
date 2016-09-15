package us.askplatyp.kb.lucene.wikidata.mapping;

import org.apache.lucene.document.Field;
import org.wikidata.wdtk.datamodel.interfaces.StringValue;
import org.wikidata.wdtk.datamodel.interfaces.Value;

import java.util.List;

/**
 * @author Thomas Pellissier Tanon
 */
interface StatementMainStringValueMapper extends StatementMainValueMapper {

    @Override
    default List<Field> mapMainValue(Value value) throws InvalidWikibaseValueException {
        if (!(value instanceof StringValue)) {
            throw new InvalidWikibaseValueException(value + " should be a StringValue");
        }
        return mapMainStringValue((StringValue) value);
    }

    List<Field> mapMainStringValue(StringValue value) throws InvalidWikibaseValueException;
}
