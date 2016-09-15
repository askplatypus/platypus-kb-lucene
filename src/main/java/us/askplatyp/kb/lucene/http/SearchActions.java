package us.askplatyp.kb.lucene.http;

import us.askplatyp.kb.lucene.lucene.LuceneIndex;
import us.askplatyp.kb.lucene.lucene.LuceneSearcher;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.*;
import java.util.Locale;

/**
 * @author Thomas Pellissier Tanon
 */
@Path("search")
public class SearchActions {

    private static final int LIMIT_DEFAULT = 100;
    private static final int LIMIT_MAX = 1000;

    @Inject
    private LuceneIndex index;

    @Path("simple")
    @GET
    public Response search(
            @QueryParam("q") String query,
            @QueryParam("type") @DefaultValue("Thing") String type,
            @QueryParam("lang") @DefaultValue("en") String lang,
            @QueryParam("continue") String queryContinue,
            @QueryParam("limit") int limit,
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

    private int cleanLimit(int limit) {
        if (limit <= 0) {
            return LIMIT_DEFAULT;
        } else {
            return Math.min(limit, LIMIT_MAX);
        }
    }
}
