package us.askplatyp.kb.lucene.wikidata.mapping;

import org.apache.lucene.document.Field;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.Value;

import java.util.Collections;
import java.util.List;

/**
 * @author Thomas Pellissier Tanon
 */
interface StatementMainValueMapper extends StatementMapper {

    @Override
    default List<Field> mapStatement(Statement statement) throws InvalidWikibaseValueException {
        Value value = statement.getValue();
        if (value == null) {
            return Collections.emptyList();
        }
        return mapMainValue(value);
    }

    List<Field> mapMainValue(Value value) throws InvalidWikibaseValueException;
}
