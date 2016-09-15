package us.askplatyp.kb.lucene.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.askplatyp.kb.lucene.model.ApiException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;
import java.util.List;
import java.util.Locale;

/**
 * @author Thomas Pellissier Tanon
 */
class ActionUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionUtils.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final MediaType APPLICATION_JSON_LD_TYPE = MediaType.valueOf("application/ld+json");

    static Response jsonContentNegotiation(Request request, JsonResultBuilder<Object> resultBuilder) {
        List<Variant> variants = Variant
                .mediaTypes(MediaType.APPLICATION_JSON_TYPE, APPLICATION_JSON_LD_TYPE)
                .languages(Main.SUPPORTED_LOCALES)
                .add().build(); //TODO lang parameter
        Variant bestResponseVariant = request.selectVariant(variants);
        if (bestResponseVariant == null) {
            return Response.notAcceptable(variants).build();
        }
        try {
            return Response.ok(serialize(resultBuilder.buildResult(bestResponseVariant.getLanguage())), bestResponseVariant)
                    .build();
        } catch (ApiException e) {
            return resultForApiException(e, bestResponseVariant);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return resultForApiException(new ApiException(e), bestResponseVariant);
        }
    }

    private static Response resultForApiException(ApiException e, Variant variant) {
        return Response
                .status(e.getStatus())
                .variant(variant)
                .entity(e)
                .build();
    }

    private static String serialize(Object model) {
        try {
            return OBJECT_MAPPER.writeValueAsString(model);
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
            return "Result serialization failed";
        }
    }

    @FunctionalInterface
    interface JsonResultBuilder<R> {
        R buildResult(Locale locale) throws Exception;
    }
}
