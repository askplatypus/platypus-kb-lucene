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

import org.apache.commons.compress.utils.IOUtils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * @author Thomas Pellissier Tanon
 */
@Path("/")
public class SwaggerActions {

    @GET
    public Response main() throws IOException {
        return Response.temporaryRedirect(URI.create("/api/v1/swagger/index.html")).build();
    }

    @Path("/api")
    @GET
    public Response api() throws IOException {
        return Response.temporaryRedirect(URI.create("/api/v1/swagger/index.html")).build();
    }

    @Path("/api/v1")
    @GET
    public Response apiV1() throws IOException {
        return Response.temporaryRedirect(URI.create("/api/v1/swagger/index.html")).build();
    }

    @Path("/api/v1/swagger/{path:.*}")
    @GET
    public Response dependencies(@PathParam("path") String path) throws IOException {
        return outputFile("/swagger/" + path);
    }

    private Response outputFile(String filePath) throws IOException {
        InputStream resourceStream = this.getClass().getResourceAsStream(filePath);
        if (resourceStream == null) {
            return Response.status(404).build();
        }
        return Response.status(200).entity(IOUtils.toByteArray(resourceStream)).build();
    }
}
