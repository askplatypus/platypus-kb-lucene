package us.askplatyp.kb.lucene.http;

import us.askplatyp.kb.lucene.lucene.LuceneIndex;
import us.askplatyp.kb.lucene.lucene.LuceneSearcher;

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
@Path("entity/{IRI}")
public class EntityActions {

    @Inject
    private LuceneIndex index;

    @GET
    public Response get(@PathParam("IRI") String IRI, @Context Request request) {
        return ActionUtils.jsonContentNegotiation(request, (locale) -> {
            try (LuceneIndex.Reader indexReader = index.getReader()) {
                return new LuceneSearcher(indexReader).getEntityForIRI(
                        IRI,
                        locale
                );
            }
        });
    }
}
