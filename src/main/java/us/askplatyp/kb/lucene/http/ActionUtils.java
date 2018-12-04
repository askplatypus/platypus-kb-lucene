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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.askplatyp.kb.lucene.Configuration;

import javax.ws.rs.InternalServerErrorException;
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

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final MediaType APPLICATION_JSON_LD_TYPE = MediaType.valueOf("application/ld+json");

    static Response jsonContentNegotiation(Request request, JsonResultBuilder<Object> resultBuilder) {
        List<Variant> variants = Variant
                .mediaTypes(MediaType.APPLICATION_JSON_TYPE, APPLICATION_JSON_LD_TYPE)
                .languages(Configuration.SUPPORTED_LOCALES)
                .add().build(); //TODO lang parameter
        Variant bestResponseVariant = request.selectVariant(variants);
        if (bestResponseVariant == null) {
            return Response.notAcceptable(variants).build();
        }
        return Response.ok(serialize(resultBuilder.buildResult(bestResponseVariant.getLanguage())), bestResponseVariant).build();
    }

    private static String serialize(Object model) {
        try {
            return OBJECT_MAPPER.writeValueAsString(model);
        } catch (JsonProcessingException e) {
            throw new InternalServerErrorException("Result serialization failed", e);
        }
    }

    @FunctionalInterface
    interface JsonResultBuilder<R> {
        R buildResult(Locale locale);
    }
}
