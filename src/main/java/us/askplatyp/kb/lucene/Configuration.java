package us.askplatyp.kb.lucene;

import jersey.repackaged.com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Thomas Pellissier Tanon
 */
public class Configuration {

    public static final Set<String> SUPPORTED_SITELINKS = Sets.newHashSet("enwiki", "frwiki");
    public static final Locale[] SUPPORTED_LOCALES = {
            Locale.ENGLISH, Locale.FRENCH,
            Locale.CANADA, Locale.CANADA_FRENCH, Locale.FRANCE, Locale.UK, Locale.US //TODO: bad
    };
    public static final Set<String> SUPPORTED_LANGUAGES = Arrays.stream(SUPPORTED_LOCALES)
            .map(Locale::getLanguage)
            .collect(Collectors.toSet());
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
