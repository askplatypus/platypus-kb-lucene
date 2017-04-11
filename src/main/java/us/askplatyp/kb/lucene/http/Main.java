/*
 * Copyright (c) 2017 Platypus Knowledge Base developers.
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

package us.askplatyp.kb.lucene.http;

import com.sun.net.httpserver.HttpServer;
import io.swagger.annotations.ExternalDocs;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
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
@SwaggerDefinition(
        host = "kb.askplatyp.us",
        info = @Info(
                title = "Platypus knowledge base",
                description = "API for the Platypus knowledge base based on Wikidata",
                version = "dev",
                license = @License(
                        name = "Creative Commons Attribution-ShareAlike 3.0",
                        url = "https://creativecommons.org/licenses/by-sa/3.0/"
                )
        ),
        produces = {"application/json", "application/ld+json"},
        schemes = {SwaggerDefinition.Scheme.HTTP, SwaggerDefinition.Scheme.HTTPS},
        externalDocs = @ExternalDocs(value = "GraphQL editor", url = "graphiql.html")
)
public class Main extends ResourceConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private Main() {
        packages("us.askplatyp.kb.lucene.http");

        register(JacksonFeature.class);
        register(EntityFilteringFeature.class);
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bindFactory(WikidataLuceneIndexFactory.class).to(LuceneIndex.class);
            }
        });
        register(CORSFilter.class);
        register(ApiListingResource.class);
        register(SwaggerSerializers.class);
        EncodingFilter.enableFor(this, GZipEncoder.class);
        EncodingFilter.enableFor(this, DeflateEncoder.class);

        configureSwagger();
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

    private void configureSwagger() {
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setResourcePackage("us.askplatyp.kb.lucene.http");
        beanConfig.setScan(true);
    }
}
