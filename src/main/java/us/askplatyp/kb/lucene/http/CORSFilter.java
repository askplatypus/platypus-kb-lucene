package us.askplatyp.kb.lucene.http;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import java.io.IOException;

/**
 * @author Thomas Pellissier Tanon
 */
class CORSFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) throws IOException {
        response.getHeaders().add("Access-Control-Allow-Origin", "*");
        response.getHeaders().add("Access-Control-Allow-Headers", "Origin, User-Agent, Content-Type, Content-Language, Accept, Accept-Language, Accept-Encoding, Accept-Charset");
        response.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS, HEAD");
    }
}
