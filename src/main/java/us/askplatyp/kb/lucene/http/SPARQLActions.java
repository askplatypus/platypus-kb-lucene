/*
 * Copyright (c) 2018 Platypus Knowledge Base developers.
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

import info.aduna.lang.FileFormat;
import info.aduna.lang.service.FileFormatServiceRegistry;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SD;
import org.openrdf.query.*;
import org.openrdf.query.resultio.BooleanQueryResultWriterFactory;
import org.openrdf.query.resultio.BooleanQueryResultWriterRegistry;
import org.openrdf.query.resultio.TupleQueryResultWriterFactory;
import org.openrdf.query.resultio.TupleQueryResultWriterRegistry;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.rio.RDFWriterRegistry;
import org.openrdf.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.askplatyp.kb.lucene.CompositeIndex;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Thomas Pellissier Tanon
 */
@Path("/api/v1/sparql")
@Api
public class SPARQLActions {

    private static final Logger LOGGER = LoggerFactory.getLogger(SPARQLActions.class);

    @Inject
    private CompositeIndex index;

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
        return Response.ok(
                (StreamingOutput) outputStream -> {
                    try {
                        Rio.write(getServiceDescription(), format.getService().getWriter(outputStream));
                    } catch (RDFHandlerException e) {
                        LOGGER.warn(e.getMessage(), e);
                        throw new InternalServerErrorException(e.getMessage(), e);
                    }
                },
                variantForFormat(format.getFormat())
        ).build();
    }

    private Response executeQuery(String query, String baseIRI, Request request) {
        try {
            RepositoryConnection connection = index.getSesameSailRepository().getReadOnlyConnection();
            try {
                Query preparedQuery = connection.prepareQuery(QueryLanguage.SPARQL, query, baseIRI);
                if (preparedQuery instanceof BooleanQuery) {
                    return evaluateBooleanQuery((BooleanQuery) preparedQuery, request);
                } else if (preparedQuery instanceof GraphQuery) {
                    return evaluateGraphQuery((GraphQuery) preparedQuery, request);
                } else if (preparedQuery instanceof TupleQuery) {
                    return evaluateTupleQuery((TupleQuery) preparedQuery, request);
                } else {
                    throw new BadRequestException("Unsupported kind of query: " + preparedQuery.toString());
                }
            } catch (MalformedQueryException e) {
                throw new BadRequestException(e.getMessage(), e);
            } finally {
                connection.close();
            }
        } catch (RepositoryException e) {
            LOGGER.warn(e.getMessage(), e);
            throw new InternalServerErrorException(e.getMessage(), e);
        }
    }

    private Response evaluateBooleanQuery(BooleanQuery query, Request request) {
        FormatService<BooleanQueryResultWriterFactory> format = getServiceForFormat(BooleanQueryResultWriterRegistry.getInstance(), request);
        return Response.ok(
                (StreamingOutput) outputStream -> {
                    try {
                        format.getService().getWriter(outputStream).handleBoolean(query.evaluate());
                    } catch (QueryResultHandlerException | QueryEvaluationException e) {
                        LOGGER.warn(e.getMessage(), e);
                        throw new InternalServerErrorException(e.getMessage(), e);
                    }
                },
                variantForFormat(format.getFormat())
        ).build();
    }

    private Response evaluateGraphQuery(GraphQuery query, Request request) {
        FormatService<RDFWriterFactory> format = getServiceForFormat(RDFWriterRegistry.getInstance(), request);
        return Response.ok(
                (StreamingOutput) outputStream -> {
                    try {
                        query.evaluate(format.getService().getWriter(outputStream));
                    } catch (QueryEvaluationException | RDFHandlerException e) {
                        LOGGER.warn(e.getMessage(), e);
                        throw new InternalServerErrorException(e.getMessage(), e);
                    }
                },
                variantForFormat(format.getFormat())
        ).build();
    }

    private Response evaluateTupleQuery(TupleQuery query, Request request) {
        FormatService<TupleQueryResultWriterFactory> format = getServiceForFormat(TupleQueryResultWriterRegistry.getInstance(), request);
        return Response.ok(
                (StreamingOutput) outputStream -> {
                    try {
                        query.evaluate(format.getService().getWriter(outputStream));
                    } catch (QueryEvaluationException | TupleQueryResultHandlerException e) {
                        LOGGER.warn(e.getMessage(), e);
                        throw new InternalServerErrorException(e.getMessage(), e);
                    }
                },
                variantForFormat(format.getFormat())
        ).build();
    }

    private <FF extends FileFormat, S> FormatService<S> getServiceForFormat(FileFormatServiceRegistry<FF, S> writerRegistry, Request request) {
        List<Variant> aceptedVariants = buildVariants(writerRegistry.getKeys());
        Variant bestResponseVariant = request.selectVariant(aceptedVariants);
        if (bestResponseVariant == null) {
            throw new NotAcceptableException("No acceptable result format found. Accepted format are: " +
                    aceptedVariants.stream().map(variant -> variant.getMediaType().toString()).collect(Collectors.joining(", ")));
        }
        FF fileFormat = Optional.ofNullable(writerRegistry.getFileFormatForMIMEType(bestResponseVariant.getMediaType().toString())).orElseThrow(() -> {
            LOGGER.error("Not able to retrieve writer for " + bestResponseVariant.getMediaType());
            return new InternalServerErrorException("Not able to retrieve writer for " + bestResponseVariant.getMediaType());
        });
        return new FormatService<>(fileFormat, Optional.ofNullable(writerRegistry.get(fileFormat)).orElseThrow(() -> {
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

    private Model getServiceDescription() {
        ValueFactory valueFactory = ValueFactoryImpl.getInstance();
        Model model = new TreeModel();

        Resource service = valueFactory.createBNode();
        model.add(service, RDF.TYPE, SD.SERVICE);
        //TODO model.add(service, SD.ENDPOINT, )
        model.add(service, SD.FEATURE_PROPERTY, SD.UNION_DEFAULT_GRAPH);
        model.add(service, SD.FEATURE_PROPERTY, SD.BASIC_FEDERATED_QUERY);
        model.add(service, SD.SUPPORTED_LANGUAGE, SD.SPARQL_10_QUERY);
        model.add(service, SD.SUPPORTED_LANGUAGE, SD.SPARQL_11_QUERY);

        /*TODO for (TupleQueryResultWriterFactory queryResultWriterFactory : TupleQueryResultWriterRegistry.getInstance().getAll()) {
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
        }*/

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
