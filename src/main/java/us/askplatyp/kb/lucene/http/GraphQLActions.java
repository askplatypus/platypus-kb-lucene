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

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import graphql.language.SourceLocation;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.askplatyp.kb.lucene.lucene.DataFetcherBuilder;
import us.askplatyp.kb.lucene.lucene.LuceneIndex;
import us.askplatyp.kb.lucene.model.GraphQLSchemaBuilder;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Thomas Pellissier Tanon
 * @see "http://graphql.org/learn/serving-over-http/"
 */
@Path("/api/v1/graphql")
@Api
public class GraphQLActions {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphQLActions.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Inject
    private LuceneIndex index;

    private GraphQL graphQL;

    @GET
    @Produces(MediaType.TEXT_HTML)
    @ApiOperation(value = "GraphQL endpoint")
    public Response executeSimpleGet() {
        return Response.temporaryRedirect(URI.create("/api/v1/swagger/graphiql.html")).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "GraphQL endpoint")
    public Response executeSimpleGet(
            @QueryParam("query") @ApiParam(value = "The GraphQL query", required = true) String query,
            @QueryParam("operationName") @ApiParam(value = "Allows to control the executed operation") String operationName
    ) {
        return internalExecute(new GraphQLRequest(query, operationName, null)); //TODO: variables
    }

    @POST
    @Consumes("application/graphql")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "GraphQL endpoint")
    public Response executeSimplePost(String query) {
        return internalExecute(new GraphQLRequest(query, null, null));
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "GraphQL endpoint")
    public Response executeJsonPost(GraphQLRequest request) {
        return internalExecute(request);
    }

    private Response internalExecute(GraphQLRequest request) {
        try {
            GraphQLResult result = new GraphQLResult(
                    getGraphQL().execute(request.getQuery(), request.getOperationName(), null, request.getVariables())
            );
            if (result.getErrors() != null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(result)
                        .type(MediaType.APPLICATION_JSON).build();
            } else {
                return Response.ok(result, MediaType.APPLICATION_JSON).build();
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return Response.serverError()
                    .entity(new GraphQLResult(e))
                    .type(MediaType.APPLICATION_JSON).build();
        }
    }

    private GraphQL getGraphQL() {
        if (graphQL == null) {
            graphQL = new GraphQL(new GraphQLSchemaBuilder(new DataFetcherBuilder(index)).schema());
        }
        return graphQL;
    }


    @ApiModel(value = "GraphQLRequest", description = "A GraphQL request as done by React")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GraphQLRequest {
        private String query;
        private String operationName;
        private Map<String, Object> variables;

        @JsonCreator
        GraphQLRequest(
                @JsonProperty("query") String query,
                @JsonProperty("operationName") String operationName,
                @JsonProperty("variables") Map<String, Object> variables
        ) {
            this.query = query;
            this.operationName = (operationName != null && operationName.equals("")) ? null : operationName;
            this.variables = (variables == null) ? Collections.emptyMap() : variables;
        }

        @JsonProperty("query")
        @ApiModelProperty(value = "Allows to control the executed operation")
        public String getQuery() {
            return query;
        }

        @JsonProperty("operationName")
        @ApiModelProperty(value = "Allows to control the executed operation")
        public String getOperationName() {
            return operationName;
        }

        @JsonProperty("variables")
        @ApiModelProperty(value = "Definition of variables")
        public Map<String, Object> getVariables() {
            return variables;
        }
    }


    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class GraphQLResult {
        private Object data;
        private List<GraphQLErrorOutput> errors;

        GraphQLResult(ExecutionResult result) {
            data = result.getData();

            if (result.getErrors().size() > 0) {
                errors = result.getErrors().stream().map(GraphQLErrorOutput::new).collect(Collectors.toList());
            }
        }

        GraphQLResult(Exception e) {
            errors = Collections.singletonList(new GraphQLErrorOutput(e));
        }

        @JsonProperty("data")
        @JsonRawValue
        @ApiModelProperty(value = "Result of the GraphQL query")
        public Object getJsonData() throws JsonProcessingException {
            return OBJECT_MAPPER.writeValueAsString(data);
        }

        @JsonProperty("errors")
        @ApiModelProperty(value = "Possible errors")
        public List<GraphQLErrorOutput> getErrors() {
            return errors;
        }
    }

    public static class GraphQLErrorOutput {
        private String message;
        private List<GraphQLErrorLocation> locations;

        GraphQLErrorOutput(GraphQLError graphQLError) {
            message = graphQLError.getMessage();
            locations = graphQLError.getLocations().stream().map(GraphQLErrorLocation::new).collect(Collectors.toList());
        }

        GraphQLErrorOutput(Exception e) {
            message = e.getMessage();
            locations = Collections.emptyList();
        }

        @JsonProperty("message")
        public String getMessage() {
            return message;
        }

        @JsonProperty("locations")
        public List<GraphQLErrorLocation> getLocations() {
            return locations;
        }
    }

    public static class GraphQLErrorLocation {
        private int line;
        private int column;

        GraphQLErrorLocation(SourceLocation sourceLocation) {
            line = sourceLocation.getLine();
            column = sourceLocation.getColumn();
        }

        @JsonProperty("line")
        public int getLine() {
            return line;
        }

        @JsonProperty("column")
        public int getColumn() {
            return column;
        }
    }
}
