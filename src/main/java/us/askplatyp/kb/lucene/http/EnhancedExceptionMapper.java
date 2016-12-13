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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.Locale;

/**
 * @author Thomas Pellissier Tanon
 */
@Provider
public class EnhancedExceptionMapper implements ExceptionMapper<WebApplicationException> {

    @Override
    public Response toResponse(WebApplicationException exception) {
        Response response = exception.getResponse();
        if (response.hasEntity()) {
            return response;
        }
        return Response.fromResponse(response)
                .entity(exception.getMessage())
                .variant(new Variant(MediaType.TEXT_PLAIN_TYPE, Locale.ENGLISH, null))
                .build();
    }
}
