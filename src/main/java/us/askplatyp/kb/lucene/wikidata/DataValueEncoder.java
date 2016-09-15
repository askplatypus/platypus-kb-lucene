package us.askplatyp.kb.lucene.wikidata;

import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.interfaces.EntityIdValue;

/**
 * @author Thomas Pellissier Tanon
 */
class DataValueEncoder {

    static String encode(EntityIdValue value) {
        if (value.getSiteIri().equals(Datamodel.SITE_WIKIDATA)) {
            return "wd:" + value.getId();
        } else {
            return value.getIri();
        }
    }
}
