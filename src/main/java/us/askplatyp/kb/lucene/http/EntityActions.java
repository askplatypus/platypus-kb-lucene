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

package us.askplatyp.kb.lucene.http;

import io.swagger.annotations.*;
import us.askplatyp.kb.lucene.lucene.LuceneIndex;
import us.askplatyp.kb.lucene.lucene.LuceneSearcher;
import us.askplatyp.kb.lucene.model.Entity;
import us.askplatyp.kb.lucene.model.JsonLdRoot;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

/**
 * @author Thomas Pellissier Tanon
 */
@Path("/api/v1/entity/{IRI}")
@Api
public class EntityActions {

    @Inject
    private LuceneIndex index;

    @GET
    @ApiOperation(
            value = "Retrieve an entity using its IRI"
            //TODO: enable when Swagger 1.511 will be out (support of @JsonUnwrapped) response = JsonLdRoot.class
    )
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Accept-Language", value = "The language to use for the output", defaultValue = "en", dataType = "string", paramType = "header")
    })
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Entity not found")
    })
    public Response get(
            @PathParam("IRI") @ApiParam(value = "The entity full IRI or with one of the standard prefixes used", example = "wd:Q42") String IRI,
            @Context Request request
    ) {
        return ActionUtils.jsonContentNegotiation(request, (locale) -> {
            try (LuceneIndex.Reader indexReader = index.getReader()) {
                return new LuceneSearcher(indexReader).getEntityForIRI(
                        IRI,
                        locale
                );
            }
        });
    }

    private static class GetResult extends JsonLdRoot<Entity> {
        public GetResult(us.askplatyp.kb.lucene.model.Context context, Entity content) {
            super(context, content);
        }
    }
}
