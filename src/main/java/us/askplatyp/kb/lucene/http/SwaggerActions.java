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
        return Response.temporaryRedirect(URI.create("/swagger/index.html")).build();
    }

    @Path("/swagger/{path:.*}")
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
