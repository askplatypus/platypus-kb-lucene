package us.askplatyp.kb.lucene.http;

import io.swagger.annotations.*;
import us.askplatyp.kb.lucene.lucene.LuceneIndex;
import us.askplatyp.kb.lucene.lucene.LuceneSearcher;
import us.askplatyp.kb.lucene.model.*;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.*;
import java.util.Locale;

/**
 * @author Thomas Pellissier Tanon
 */
@Path("/api/v1/search")
@Api
public class SearchActions {

    private static final String LIMIT_DEFAULT = "100";
    private static final int LIMIT_MAX = 1000;

    @Inject
    private LuceneIndex index;

    @Path("simple")
    @GET
    @ApiOperation(
            value = "Allows to do simple queries inside of the knowledge base"
            //TODO: enable when Swagger 1.511 will be out (support of @JsonUnwrapped) response = SimpleResult.class
    )
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Content-Language", value = "The language to use for the output", defaultValue = "en", dataType = "string", paramType = "header")
    })
    public Response simple(
            @QueryParam("q") @ApiParam(value = "The query itself. If empty, all entities are returned", example = "Barack Obama") String query,
            @QueryParam("type") @DefaultValue("Thing") @ApiParam(value = "A type filter (it uses http://schema.org/ as the default namespace)", example = "Person") String type,
            @QueryParam("lang") @DefaultValue("en") @ApiParam(value = "The query language") String lang,
            @QueryParam("continue") @ApiParam(value = "Allows to retrieve more results using a pagination system") String queryContinue,
            @QueryParam("limit") @DefaultValue(LIMIT_DEFAULT) @ApiParam(value = "The number of query results to return") int limit,
            @Context Request request,
            @Context UriInfo uriInfo
    ) {
        return ActionUtils.jsonContentNegotiation(request, (locale) -> {
            Locale inputLocale = Locale.forLanguageTag(lang);
            try (LuceneIndex.Reader indexReader = index.getReader()) {
                return new LuceneSearcher(indexReader).getEntitiesForLabel(
                        query,
                        type,
                        inputLocale,
                        locale,
                        getRequestBaseURI(uriInfo, query, type, inputLocale),
                        queryContinue,
                        cleanLimit(limit)
                );
            }
        });
    }

    private String getRequestBaseURI(UriInfo uriInfo, String query, String type, Locale lang) {
        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
        if (query != null) {
            builder.queryParam("q", query);
        }
        if (type != null) {
            builder.queryParam("type", type);
        }
        builder.queryParam("lang", lang.getLanguage());
        return builder.toString();
    }

    private int cleanLimit(int limit) throws ApiException {
        if (limit <= 0) {
            throw new ApiException("The limit parameter should have a value greater than 0", 400);
        }
        return Math.min(limit, LIMIT_MAX);
    }

    private static class SimpleResult extends JsonLdRoot<Collection<EntitySearchResult<Entity>>> {
        public SimpleResult(us.askplatyp.kb.lucene.model.Context context, Collection<EntitySearchResult<Entity>> content) {
            super(context, content);
        }
    }
}
