package us.askplatyp.kb.lucene.http;

import com.sun.net.httpserver.HttpServer;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.message.DeflateEncoder;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.message.filtering.EntityFilteringFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.EncodingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.askplatyp.kb.lucene.Configuration;
import us.askplatyp.kb.lucene.lucene.LuceneIndex;
import us.askplatyp.kb.lucene.wikidata.WikidataLuceneIndexFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * @author Thomas Pellissier Tanon
 */
public class Main extends ResourceConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private Main() {
        register(EntityFilteringFeature.class);
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bindFactory(WikidataLuceneIndexFactory.class).to(LuceneIndex.class);
            }
        });
        packages("us.askplatyp.kb.lucene.http");

        EncodingFilter.enableFor(this, GZipEncoder.class);
        EncodingFilter.enableFor(this, DeflateEncoder.class);
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = startServer();

        new Thread() {
            @Override
            public void run() {
                try {
                    WikidataLuceneIndexFactory.init(Configuration.getInstance().getLuceneDirectory());
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }.run();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stop(0)));
    }

    private static HttpServer startServer() {
        return JdkHttpServerFactory.createHttpServer(Configuration.getInstance().getHttpBaseURI(), new Main(), getDefaultSSLContext());
    }

    private static SSLContext getDefaultSSLContext() {
        try {
            return SSLContext.getDefault();
        } catch (NoSuchAlgorithmException e) {
            LOGGER.warn("No SSL context found, fallback to no SSL", e.getMessage());
            return null;
        }
    }
}
