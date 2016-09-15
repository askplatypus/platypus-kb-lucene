package us.askplatyp.kb.lucene.wikidata.mapping;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;
import us.askplatyp.kb.lucene.wikidata.WikidataTypes;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Thomas Pellissier Tanon
 */
class P31StatementMapper implements StatementMainItemIdValueMapper {

    @Override
    public List<Field> mapMainItemIdValue(ItemIdValue value) throws InvalidWikibaseValueException {
        return WikidataTypes.getSchemaOrgTypes(value).stream()
                .map(type -> new StringField("@type", type, Field.Store.YES))
                .collect(Collectors.toList());
    }
}
