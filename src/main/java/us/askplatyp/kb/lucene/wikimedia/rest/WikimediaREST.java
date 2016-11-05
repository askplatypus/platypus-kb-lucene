/*
 * Copyright (c) 2016 Platypus Knowledge Base developers.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package us.askplatyp.kb.lucene.wikimedia.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import jersey.repackaged.com.google.common.cache.CacheBuilder;
import jersey.repackaged.com.google.common.cache.CacheLoader;
import jersey.repackaged.com.google.common.cache.LoadingCache;
import us.askplatyp.kb.lucene.wikimedia.rest.model.Summary;

import javax.ws.rs.core.UriBuilder;
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
        return UriBuilder.fromUri("https://host/api/rest_v1/page/")
                .host(pageURL.getHost())
                .segment(action, pageURL.getPath().replaceFirst("/wiki/", ""))
                .build().toURL();
    }
}
