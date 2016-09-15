package us.askplatyp.kb.lucene.wikimedia.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import jersey.repackaged.com.google.common.cache.CacheBuilder;
import jersey.repackaged.com.google.common.cache.CacheLoader;
import jersey.repackaged.com.google.common.cache.LoadingCache;
import us.askplatyp.kb.lucene.wikimedia.rest.model.Summary;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author Thomas Pellissier Tanon
 */
public class WikimediaREST {

    private static final WikimediaREST INSTANCE = new WikimediaREST();
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private LoadingCache<String, Summary> summaryCache = CacheBuilder.newBuilder()
            .maximumSize(16384) //TODO: configure?
            .expireAfterWrite(1, TimeUnit.DAYS)
            .build(new CacheLoader<String, Summary>() {
                @Override
                public Summary load(String pageIRI) throws IOException {
                    return buildSummary(pageIRI);
                }
            });

    private WikimediaREST() {
    }

    public static WikimediaREST getInstance() {
        return INSTANCE;
    }

    public Summary getSummary(String pageIRI) throws IOException {
        try {
            return summaryCache.get(pageIRI);
        } catch (ExecutionException e) {
            throw new IOException(e);
        }
    }

    private Summary buildSummary(String pageIRI) throws IOException {
        return MAPPER.readValue(getURLForPageAction("summary", pageIRI).openStream(), Summary.class);
    }

    private URL getURLForPageAction(String action, String pageIRI) throws MalformedURLException {
        URL pageURL = new URL(pageIRI);
        return new URL("https", pageURL.getHost(), "/api/rest_v1/page/" + action + "/" + pageURL.getPath().replaceFirst("/wiki/", ""));
    }
}
