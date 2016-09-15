package us.askplatyp.kb.lucene.wikidata.mapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Thomas Pellissier Tanon
 */
public class MapperRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(MapperRegistry.class);
    private static final Map<PropertyIdValue, StatementMapper> MAPPER_FOR_PROPERTY = new HashMap<>();

    static {
        //TODO: LinkedIn, Myspace, Pinterest, SoundCloud, Tumblr...
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P31"), new P31StatementMapper());
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P646"), new ExternalIdentifierStatementMapper("sameAs", "kg:$1", "(/m/0[0-9a-z_]{2,6}|/m/01[0123][0-9a-z_]{5})"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P856"), new URIStatementMapper("url"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P2002"), new ExternalIdentifierStatementMapper("sameAs", "http://twitter.com/$1", "[A-Za-z0-9_]{1,15}"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P2003"), new ExternalIdentifierStatementMapper("sameAs", "http://www.instagram.com/$1", "[a-z0-9_\\.]{1,30}"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P2013"), new ExternalIdentifierStatementMapper("sameAs", "http://www.facebook.com/$1", "[A-Za-zА-Яа-яёäöüßЁ0-9.-]+"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P2397"), new ExternalIdentifierStatementMapper("sameAs", "http://www.youtube.com/channel/$1", "UC([A-Za-z0-9_\\-]){22}"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P2671"), new ExternalIdentifierStatementMapper("sameAs", "kg:$1", "\\/g\\/[0-9a-zA-Z]+"));
        MAPPER_FOR_PROPERTY.put(Datamodel.makeWikidataPropertyIdValue("P2847"), new ExternalIdentifierStatementMapper("sameAs", "http://plus.google.com/$1", "\\d{22}|\\+[-\\w_\\u00C0-\\u00FF]+"));
    }

    public static Optional<StatementMapper> getMapperForProperty(PropertyIdValue propertyId) {
        return Optional.ofNullable(MAPPER_FOR_PROPERTY.get(propertyId));
    }
}
