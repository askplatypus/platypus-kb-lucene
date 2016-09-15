package us.askplatyp.kb.lucene;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Properties;

/**
 * @author Thomas Pellissier Tanon
 */
public class Configuration {

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
