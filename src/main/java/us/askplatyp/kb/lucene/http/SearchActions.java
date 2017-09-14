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

import io.swagger.annotations.*;
import us.askplatyp.kb.lucene.jsonld.JsonLdBuilder;
import us.askplatyp.kb.lucene.lucene.LuceneIndex;
import us.askplatyp.kb.lucene.lucene.LuceneLookup;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
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
            @ApiImplicitParam(name = "Accept-Language", value = "The language to use for the output", defaultValue = "en", dataType = "string", paramType = "header")
    })
    public Response simple(
            @QueryParam("q") @ApiParam(value = "The query itself. If empty, all entities are returned", example = "Barack Obama") String query,
            @QueryParam("type") @DefaultValue("NamedIndividual") @ApiParam(value = "A type filter (it uses http://schema.org/ as the default namespace)", example = "Person") String type,
            @QueryParam("lang") @DefaultValue("en") @ApiParam(value = "The query language") String lang,
            @QueryParam("continue") @ApiParam(value = "Allows to retrieve more results using a pagination system") String queryContinue,
            @QueryParam("limit") @DefaultValue(LIMIT_DEFAULT) @ApiParam(value = "The number of query results to return") int limit,
            @Context Request request,
            @Context UriInfo uriInfo
    ) {
        return ActionUtils.jsonContentNegotiation(request, (locale) -> {
            Locale inputLocale = Locale.forLanguageTag(lang);
            try (LuceneIndex.Reader indexReader = index.getReader()) {
                LuceneLookup luceneLookup = new LuceneLookup(indexReader);
                return (new JsonLdBuilder(luceneLookup)).buildEntitySearchResultInLanguage(luceneLookup.getResourcesForLabel(
                        query,
                        type,
                        inputLocale,
                        queryContinue,
                        cleanLimit(limit)
                ), getRequestBaseURI(uriInfo, query, type, inputLocale), locale);
            } catch (IOException e) {
                throw new InternalServerErrorException("Database error.", e);
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

    private int cleanLimit(int limit) {
        if (limit <= 0) {
            throw new BadRequestException("The limit parameter should have a value greater than 0");
        }
        return Math.min(limit, LIMIT_MAX);
    }
}
