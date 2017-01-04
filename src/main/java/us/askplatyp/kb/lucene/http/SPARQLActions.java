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

import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;
import com.google.common.util.concurrent.UncheckedTimeoutException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.eclipse.rdf4j.common.lang.FileFormat;
import org.eclipse.rdf4j.common.lang.service.FileFormatServiceRegistry;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SD;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.query.algebra.evaluation.QueryPreparer;
import org.eclipse.rdf4j.query.parser.*;
import org.eclipse.rdf4j.query.parser.sparql.SPARQLParserFactory;
import org.eclipse.rdf4j.query.resultio.BooleanQueryResultWriterFactory;
import org.eclipse.rdf4j.query.resultio.BooleanQueryResultWriterRegistry;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultWriterFactory;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultWriterRegistry;
import org.eclipse.rdf4j.rio.RDFWriterFactory;
import org.eclipse.rdf4j.rio.RDFWriterRegistry;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.askplatyp.kb.lucene.lucene.LuceneIndex;
import us.askplatyp.kb.lucene.lucene.sparql.LuceneTripleSource;
import us.askplatyp.kb.lucene.lucene.sparql.SimpleQueryPreparer;
import us.askplatyp.kb.lucene.model.Namespaces;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Thomas Pellissier Tanon
 */
@Path("/api/v1/sparql")
@Api
public class SPARQLActions {

    private static final long QUERY_TIMOUT_IN_S = 30;
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphQLActions.class);
    private static final TimeLimiter TIME_LIMITER = new SimpleTimeLimiter();

    private static final String PREFIX = Namespaces.NAMESPACES.entrySet().stream()
            .map(namespace -> "PREFIX " + namespace.getKey() + ": <" + namespace.getValue() + ">")
            .collect(Collectors.joining("\n"));
    @Inject
    private LuceneIndex index;

    private QueryParser queryParser = new SPARQLParserFactory().getParser();
    private QueryPreparer queryPreparer;
    @GET
    @ApiOperation(value = "Executes SPARQL query. If the 'query' query parameter is not set, returns a description of the endpoint.")
    public Response get(
            @QueryParam("query") @ApiParam(value = "The SPARQL query to execute") String query,
            @Context Request request
    ) {
        if (query == null) {
            return executeDescription(request);
        } else {
            return executeQuery(query, null, request);
        }
    }

    @POST
    @Consumes("application/sparql-query")
    @ApiOperation(value = "Executes SPARQL query.")
    public Response postSPARQL(String query, @Context Request request) {
        return executeQuery(query, null, request);
    }

    private Response executeDescription(Request request) {
        FormatService<RDFWriterFactory> format = getServiceForFormat(RDFWriterRegistry.getInstance(), request);
        try (OutputStream outputStream = new ByteArrayOutputStream()) {
            Rio.write(getServiceDescription(), format.getService().getWriter(outputStream));
            return Response.ok(outputStream.toString(), variantForFormat(format.getFormat())).build();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new InternalServerErrorException(e.getMessage(), e);
        }
    }

    private Response executeQuery(String query, String baseIRI, Request request) {
        try {
            return TIME_LIMITER.callWithTimeout(() -> {
                ParsedQuery parsedQuery = parseQuery(query, baseIRI);
                try {
                    if (parsedQuery instanceof ParsedBooleanQuery) {
                        return evaluateBooleanQuery((ParsedBooleanQuery) parsedQuery, request);
                    } else if (parsedQuery instanceof ParsedGraphQuery) {
                        return evaluateGraphQuery((ParsedGraphQuery) parsedQuery, request);
                    } else if (parsedQuery instanceof ParsedTupleQuery) {
                        return evaluateTupleQuery((ParsedTupleQuery) parsedQuery, request);
                    } else {
                        throw new BadRequestException("Unsupported kind of query: " + parsedQuery.toString());
                    }
                } catch (QueryEvaluationException e) {
                    LOGGER.info(e.getMessage(), e);
                    throw new BadRequestException(e.getMessage(), e);
                }
            }, QUERY_TIMOUT_IN_S, TimeUnit.SECONDS, true);
        } catch (UncheckedTimeoutException e) {
            throw new InternalServerErrorException("Query timeout limit reached");
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            LOGGER.error(e.getMessage(), e);
            throw new InternalServerErrorException(e.getMessage(), e);
        }
    }

    private Response evaluateBooleanQuery(ParsedBooleanQuery parsedQuery, Request request) {
        BooleanQuery query = getQueryPreparer().prepare(parsedQuery);
        FormatService<BooleanQueryResultWriterFactory> format = getServiceForFormat(BooleanQueryResultWriterRegistry.getInstance(), request);
        try (OutputStream outputStream = new ByteArrayOutputStream()) {
            format.getService().getWriter(outputStream).handleBoolean(query.evaluate());
            return Response.ok(outputStream.toString(), variantForFormat(format.getFormat())).build();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new InternalServerErrorException(e.getMessage(), e);
        }
    }

    private Response evaluateGraphQuery(ParsedGraphQuery parsedQuery, Request request) {
        GraphQuery query = getQueryPreparer().prepare(parsedQuery);
        FormatService<RDFWriterFactory> format = getServiceForFormat(RDFWriterRegistry.getInstance(), request);
        try (OutputStream outputStream = new ByteArrayOutputStream()) {
            query.evaluate(format.getService().getWriter(outputStream));
            return Response.ok(outputStream.toString(), variantForFormat(format.getFormat())).build();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new InternalServerErrorException(e.getMessage(), e);
        }
    }

    private Response evaluateTupleQuery(ParsedTupleQuery parsedQuery, Request request) {
        TupleQuery query = getQueryPreparer().prepare(parsedQuery);
        FormatService<TupleQueryResultWriterFactory> format = getServiceForFormat(TupleQueryResultWriterRegistry.getInstance(), request);
        try (OutputStream outputStream = new ByteArrayOutputStream()) {
            query.evaluate(format.getService().getWriter(outputStream));
            return Response.ok(outputStream.toString(), variantForFormat(format.getFormat())).build();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new InternalServerErrorException(e.getMessage(), e);
        }
    }

    private ParsedQuery parseQuery(String query, String baseIRI) {
        try {
            return queryParser.parseQuery(PREFIX + "\n" + query, baseIRI);
        } catch (MalformedQueryException e) {
            throw new BadRequestException(e.getMessage(), e);
        }
    }

    private <FF extends FileFormat, S> FormatService<S> getServiceForFormat(FileFormatServiceRegistry<FF, S> writerRegistry, Request request) {
        List<Variant> aceptedVariants = buildVariants(writerRegistry.getKeys());
        Variant bestResponseVariant = request.selectVariant(aceptedVariants);
        if (bestResponseVariant == null) {
            throw new NotAcceptableException("No acceptable result format found. Accepted format are: " +
                    aceptedVariants.stream().map(variant -> variant.getMediaType().toString()).collect(Collectors.joining(", ")));
        }
        FF fileFormat = writerRegistry.getFileFormatForMIMEType(bestResponseVariant.getMediaType().toString()).orElseThrow(() -> {
            LOGGER.error("Not able to retrieve writer for " + bestResponseVariant.getMediaType());
            return new InternalServerErrorException("Not able to retrieve writer for " + bestResponseVariant.getMediaType());
        });
        return new FormatService<>(fileFormat, writerRegistry.get(fileFormat).orElseThrow(() -> {
            LOGGER.error("Unable to write " + fileFormat);
            return new InternalServerErrorException("Unable to write " + fileFormat);
        }));
    }

    private <FF extends FileFormat> List<Variant> buildVariants(Set<FF> acceptedFormats) {
        return Variant.mediaTypes(
                acceptedFormats.stream()
                        .flatMap(fileFormat -> fileFormat.getMIMETypes().stream())
                        .map(MediaType::valueOf)
                        .toArray(MediaType[]::new)
        ).add().build();
    }

    private <FF extends FileFormat> Variant variantForFormat(FF format) {
        return new Variant(MediaType.valueOf(format.getDefaultMIMEType()), (Locale) null, null);
    }

    private QueryPreparer getQueryPreparer() {
        if (queryPreparer == null) {
            queryPreparer = new SimpleQueryPreparer(new LuceneTripleSource(index));
        }
        return queryPreparer;
    }

    private Model getServiceDescription() {
        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        Model model = new TreeModel();

        Resource service = valueFactory.createBNode();
        model.add(service, RDF.TYPE, SD.SERVICE);
        //TODO model.add(service, SD.ENDPOINT, )
        model.add(service, SD.FEATURE_PROPERTY, SD.UNION_DEFAULT_GRAPH);
        model.add(service, SD.FEATURE_PROPERTY, SD.BASIC_FEDERATED_QUERY);
        model.add(service, SD.SUPPORTED_LANGUAGE, SD.SPARQL_10_QUERY);
        model.add(service, SD.SUPPORTED_LANGUAGE, SD.SPARQL_11_QUERY);

        for (TupleQueryResultWriterFactory queryResultWriterFactory : TupleQueryResultWriterRegistry.getInstance().getAll()) {
            Resource formatIRI = queryResultWriterFactory.getTupleQueryResultFormat().getStandardURI();
            if (formatIRI != null) {
                model.add(service, SD.RESULT_FORMAT, formatIRI);
            }
        }
        for (BooleanQueryResultWriterFactory queryResultWriterFactory : BooleanQueryResultWriterRegistry.getInstance().getAll()) {
            Resource formatIRI = queryResultWriterFactory.getBooleanQueryResultFormat().getStandardURI();
            if (formatIRI != null) {
                model.add(service, SD.RESULT_FORMAT, formatIRI);
            }
        }
        for (RDFWriterFactory formatWriterFactory : RDFWriterRegistry.getInstance().getAll()) {
            Resource formatIRI = formatWriterFactory.getRDFFormat().getStandardURI();
            if (formatIRI != null) {
                model.add(service, SD.RESULT_FORMAT, formatIRI);
            }
        }

        return model;
    }

    private static class FormatService<S> {

        private FileFormat format;
        private S service;

        FormatService(FileFormat format, S service) {
            this.format = format;
            this.service = service;
        }

        FileFormat getFormat() {
            return format;
        }

        S getService() {
            return service;
        }
    }
}
