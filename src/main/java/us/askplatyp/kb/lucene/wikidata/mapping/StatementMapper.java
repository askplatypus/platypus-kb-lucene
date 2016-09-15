package us.askplatyp.kb.lucene.wikidata.mapping;

import org.apache.lucene.document.Field;
import org.wikidata.wdtk.datamodel.interfaces.Statement;

import java.util.List;

/**
 * @author Thomas Pellissier Tanon
 */
public interface StatementMapper {
    List<Field> mapStatement(Statement statement) throws InvalidWikibaseValueException;
}
