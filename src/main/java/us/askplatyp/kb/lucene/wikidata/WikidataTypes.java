package us.askplatyp.kb.lucene.wikidata;

import jersey.repackaged.com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.interfaces.DatatypeIdValue;
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;
import us.askplatyp.kb.lucene.model.Locales;

import java.util.*;

/**
 * @author Thomas Pellissier Tanon
 */
public class WikidataTypes {

    static final Map<Locale, String> WIKIMEDIA_LANGUAGE_CODES = new HashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(LuceneUpdateProcessor.class);
    private static final Map<ItemIdValue, List<String>> SCHEMA_TYPES = new HashMap<>();
    //TODO: retrieve them with the SPARQL query "?item wdt:P279* wd:Q17379835"?
    private static final Set<ItemIdValue> FILTERED_TYPES = Sets.newHashSet(
            Datamodel.makeWikidataItemIdValue("Q4167410"),
            Datamodel.makeWikidataItemIdValue("Q17362920"),
            Datamodel.makeWikidataItemIdValue("Q4167836"),
            Datamodel.makeWikidataItemIdValue("Q13406463"),
            Datamodel.makeWikidataItemIdValue("Q11266439"),
            Datamodel.makeWikidataItemIdValue("Q14204246"),
            Datamodel.makeWikidataItemIdValue("Q21286738"),
            Datamodel.makeWikidataItemIdValue("Q17633526"),
            Datamodel.makeWikidataItemIdValue("Q18340514"), //article about events in a specific year or time period
            Datamodel.makeWikidataItemIdValue("Q26267864"),
            Datamodel.makeWikidataItemIdValue("Q4663903"),
            Datamodel.makeWikidataItemIdValue("Q17362920"),
            Datamodel.makeWikidataItemIdValue("Q20010800"),
            Datamodel.makeWikidataItemIdValue("Q19692233"),
            Datamodel.makeWikidataItemIdValue("Q17524420"),
            Datamodel.makeWikidataItemIdValue("Q21025364"),
            Datamodel.makeWikidataItemIdValue("Q15647814"),
            Datamodel.makeWikidataItemIdValue("Q19842659"),
            Datamodel.makeWikidataItemIdValue("Q15184295")
    );
    //TODO Fits for Wikidata Query service but should be improved
    private static final Map<String, List<String>> XSD_FOR_DATATYPE = new HashMap<>();

    static {
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q5"), Collections.singletonList("Person")); //human
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q532"), Collections.singletonList("Place")); //village
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q13100073"), Collections.singletonList("Place")); //Chinese village TODO
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q79007"), Collections.singletonList("Place")); //street
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q486972"), Collections.singletonList("Place")); //human settlement
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q23397"), Arrays.asList("Place", "LakeBodyOfWater")); //lake
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q8502"), Arrays.asList("Place", "Mountain")); //mountain
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q4022"), Arrays.asList("Place", "RiverBodyOfWater")); //river
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q11424"), Arrays.asList("CreativeWork", "Movie")); //film
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q482994"), Arrays.asList("CreativeWork", "MusicPlaylist", "MusicAlbum")); //album
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q13442814"), Arrays.asList("CreativeWork", "Article", "ScholarlyArticle")); //scientific article
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q16970"), Arrays.asList("Place", "CivicStructure", "PlaceOfWorship", "Church")); //
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q571"), Arrays.asList("CreativeWork", "Book")); //
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q215380"), Arrays.asList("Organization", "MusicGroup")); //
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q7889"), Arrays.asList("CreativeWork", "Game", "SoftwareApplication", "VideoGame")); //
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q43229"), Collections.singletonList("Organization")); //
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q515"), Arrays.asList("Place", "AdministrativeArea", "City")); //
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q191067"), Arrays.asList("CreativeWork", "Article")); //
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q41298"), Arrays.asList("CreativeWork", "CreativeWorkSeries", "Periodical")); //
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q207628"), Arrays.asList("CreativeWork", "MusicComposition")); //
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q125191"), Arrays.asList("CreativeWork", "Photograph")); //
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q11707"), Arrays.asList("Place", "Organization", "LocalBusiness", "FoodEstablishment", "Restaurant")); //
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q1420"), Arrays.asList("Product", "Vehicle", "Car")); //
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q431289"), Collections.singletonList("Brand")); //
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q11410"), Arrays.asList("CreativeWork", "Game")); //
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q166142"), Arrays.asList("CreativeWork", "SoftwareApplication")); //
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q132241"), Arrays.asList("Event", "Festival")); //
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q277759"), Arrays.asList("CreativeWork", "CreativeWorkSeries", "BookSeries")); //
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q56061"), Arrays.asList("Place", "AdministrativeArea")); //
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q30022"), Arrays.asList("Place", "Organization", "LocalBusiness", "FoodEstablishment", "CafeOrCoffeeShop")); //
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q3305213"), Arrays.asList("CreativeWork", "Painting")); //
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q1248784"), Arrays.asList("Place", "CivicStructure", "Airport")); //
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q1983062"), Arrays.asList("CreativeWork", "Episode")); //
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q1983062"), Arrays.asList("CreativeWork", "CreativeWorkSeries", "VideoGameSeries")); //
    }

    static {
        XSD_FOR_DATATYPE.put(DatatypeIdValue.DT_ITEM, Collections.singletonList("Thing"));
        XSD_FOR_DATATYPE.put(DatatypeIdValue.DT_PROPERTY, Collections.singletonList("Property"));
        XSD_FOR_DATATYPE.put(DatatypeIdValue.DT_STRING, Collections.singletonList("xsd:string"));
        XSD_FOR_DATATYPE.put(DatatypeIdValue.DT_URL, Collections.singletonList("xsd:string"));
        XSD_FOR_DATATYPE.put(DatatypeIdValue.DT_COMMONS_MEDIA, Collections.singletonList("xsd:string"));
        XSD_FOR_DATATYPE.put(DatatypeIdValue.DT_TIME, Collections.singletonList("xsd:dateTime"));
        XSD_FOR_DATATYPE.put(DatatypeIdValue.DT_GLOBE_COORDINATES, Collections.singletonList("GeoCoordinates"));
        XSD_FOR_DATATYPE.put(DatatypeIdValue.DT_QUANTITY, Collections.singletonList("xsd:decimal"));
        XSD_FOR_DATATYPE.put(DatatypeIdValue.DT_MONOLINGUAL_TEXT, Collections.singletonList("rdf:langString"));
        XSD_FOR_DATATYPE.put(DatatypeIdValue.DT_EXTERNAL_ID, Collections.singletonList("xsd:string"));
        XSD_FOR_DATATYPE.put(DatatypeIdValue.DT_MATH, Collections.singletonList("xsd:string"));
    }

    static {
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.ARABIC, "ar");
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.AMHARIC, "am");
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.BULGARIAN, "bg");
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.BENGALI, "bn");
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.CATALAN, "ca");
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.CZECH, "cs");
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.DANISH, "da");
        WIKIMEDIA_LANGUAGE_CODES.put(Locale.GERMAN, "de");
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.GREEK, "el");
        WIKIMEDIA_LANGUAGE_CODES.put(Locale.ENGLISH, "en");
        WIKIMEDIA_LANGUAGE_CODES.put(Locale.CANADA, "en-ca");
        WIKIMEDIA_LANGUAGE_CODES.put(Locale.UK, "en-gb");
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.SPANISH, "es");
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.ESTONIAN, "et");
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.PERSIAN, "fa");
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.FINNISH, "fi");
        WIKIMEDIA_LANGUAGE_CODES.put(Locale.FRENCH, "fr");
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.FILIPINO, "tl");
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.GUJARATI, "gu");
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.HEBREW, "he");
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.HINDI, "hi");
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.CROATIAN, "hr");
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.HUNGARIAN, "hu");
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.INDONESIAN, "id");
        WIKIMEDIA_LANGUAGE_CODES.put(Locale.ITALIAN, "it");
        WIKIMEDIA_LANGUAGE_CODES.put(Locale.JAPANESE, "ja");
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.KANNADA, "kn");
        WIKIMEDIA_LANGUAGE_CODES.put(Locale.KOREAN, "ko");
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.LATIN, "la");
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.LITHUANIAN, "lt");
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.LATVIAN, "lv");
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.MALAYALAM, "ml");
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.MARATHI, "mr");
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.MALAY, "ms");
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.DUTCH, "nl");
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.NORWEGIAN, "no");
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.POLISH, "pl");
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.PORTUGUESE, "pt");
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.BRAZIL, "pt-br");
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.ROMANIAN, "ro");
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.RUSSIAN, "ru");
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.SLOVAK, "sk");
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.SLOVENIAN, "sl");
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.SERBIAN, "sr");
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.SWEDISH, "sv");
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.SWAHILI, "sw");
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.TAMIL, "ta");
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.TELUGU, "te");
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.TAGALOG, "tl");
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.THAI, "th");
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.TURKISH, "tr");
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.UKRAINIAN, "uk");
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.VIETNAMESE, "vi");
        WIKIMEDIA_LANGUAGE_CODES.put(Locale.CHINESE, "zh");
        WIKIMEDIA_LANGUAGE_CODES.put(Locale.CHINA, "zh-hans");
        WIKIMEDIA_LANGUAGE_CODES.put(Locale.TAIWAN, "zh-hant");
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.SIMPLIFIED_CHINESE, "zh-hans");
        WIKIMEDIA_LANGUAGE_CODES.put(Locales.TRADITIONAL_CHINESE, "zh-hant");
    }

    public static List<String> getSchemaOrgTypes(ItemIdValue itemIdValue) {
        List<String> found = SCHEMA_TYPES.get(itemIdValue);
        if (found == null) {
            return Collections.emptyList();
        } else {
            return found;
        }
    }

    static boolean isFilteredType(ItemIdValue itemIdValue) {
        return FILTERED_TYPES.contains(itemIdValue);
    }

    static List<String> getRangeForDatatype(DatatypeIdValue datatypeIdValue) {
        List<String> found = XSD_FOR_DATATYPE.get(datatypeIdValue.getIri());
        if (found == null) {
            LOGGER.warn("Unknown datatype " + datatypeIdValue.toString());
            return Collections.emptyList();
        } else {
            return found;
        }
    }
}
