package us.askplatyp.kb.lucene;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.askplatyp.kb.lucene.model.Locales;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Locale;
import java.util.Properties;

/**
 * @author Thomas Pellissier Tanon
 */
public class Configuration {

    public static final Locale[] SUPPORTED_LOCALES = {
            Locale.ENGLISH, //Default locale
            Locales.ARABIC,
            Locales.AMHARIC,
            Locales.BULGARIAN,
            Locales.BENGALI,
            Locales.CATALAN,
            Locales.CZECH,
            Locales.DANISH,
            Locale.GERMAN, Locale.GERMANY,
            Locales.GREEK,
            Locale.ENGLISH, Locale.CANADA, Locale.UK, Locale.US,
            Locales.SPANISH, Locales.SPAIN, Locales.LATIN_AMERICA_SPANISH,
            Locales.ESTONIAN,
            Locales.PERSIAN,
            Locales.FINNISH,
            Locales.FILIPINO,
            Locale.FRENCH, Locale.CANADA_FRENCH, Locale.FRANCE,
            Locales.GUJARATI,
            Locales.HEBREW,
            Locales.HINDI,
            Locales.CROATIAN,
            Locales.HUNGARIAN,
            Locales.INDONESIAN,
            Locale.ITALIAN, Locale.ITALY,
            Locale.JAPANESE, Locale.JAPAN,
            Locales.KANNADA,
            Locale.KOREAN, Locale.KOREA,
            Locales.LATIN,
            Locales.LITHUANIAN,
            Locales.LATVIAN,
            Locales.MALAYALAM,
            Locales.MARATHI,
            Locales.MALAY,
            Locales.DUTCH,
            Locales.NORWEGIAN,
            Locales.POLISH,
            Locales.PORTUGUESE, Locales.BRAZIL, Locales.PORTUGAL,
            Locales.ROMANIAN,
            Locales.RUSSIAN,
            Locales.SLOVAK,
            Locales.SLOVENIAN,
            Locales.SERBIAN,
            Locales.SWEDISH,
            Locales.SWAHILI,
            Locales.TAMIL,
            Locales.TELUGU,
            Locales.TAGALOG,
            Locales.THAI,
            Locales.TURKISH,
            Locales.UKRAINIAN,
            Locales.VIETNAMESE,
            Locale.CHINESE, Locale.CHINA, Locale.TAIWAN, Locales.SIMPLIFIED_CHINESE, Locales.TRADITIONAL_CHINESE
    };

    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);
    private static final Configuration INSTANCE = new Configuration();
    private Properties properties;

    private Configuration() {
        properties = new Properties();
        try (InputStream inputStream = this.getClass().getResourceAsStream("/config.properties")) {
            properties.load(inputStream);
        } catch (IOException e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    public static Configuration getInstance() {
        return INSTANCE;
    }

    public String getLuceneDirectory() {
        return properties.getProperty("us.askplatyp.kb.lucene.lucene.directory", "data");
    }

    public URI getHttpBaseURI() {
        return URI.create(properties.getProperty("us.askplatyp.kb.lucene.http.uri", "http://localhost:4567/"));
    }

    public String getWikidataDirectory() {
        return properties.getProperty("us.askplatyp.kb.lucene.wikidata.directory", System.getProperty("user.dir"));
    }
}
