package us.askplatyp.kb.lucene.wikidata.mapping;

import org.apache.lucene.document.Field;
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;
import org.wikidata.wdtk.datamodel.interfaces.Value;

import java.util.List;

/**
 * @author Thomas Pellissier Tanon
 */
interface StatementMainItemIdValueMapper extends StatementMainValueMapper {

    @Override
    default List<Field> mapMainValue(Value value) throws InvalidWikibaseValueException {
        if (!(value instanceof ItemIdValue)) {
            throw new InvalidWikibaseValueException(value + " should be a ItemIdValue");
        }
        return mapMainItemIdValue((ItemIdValue) value);
    }

    List<Field> mapMainItemIdValue(ItemIdValue value) throws InvalidWikibaseValueException;
}
