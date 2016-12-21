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

package us.askplatyp.kb.lucene.wikidata.mapping;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.wikidata.wdtk.datamodel.interfaces.StringValue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

/**
 * @author Thomas Pellissier Tanon
 */
class URIStatementMapper implements StatementMainStringValueMapper {

    private String targetFieldName;

    URIStatementMapper(String targetFieldName) {
        this.targetFieldName = targetFieldName;
    }

    @Override
    public List<Field> mapMainStringValue(StringValue value) throws InvalidWikibaseValueException {
        try {
            URI parsedURL = new URI(value.getString()).normalize();
            if ((parsedURL.getScheme().equals("http") || parsedURL.getScheme().equals("https")) && parsedURL.getPath().isEmpty()) {
                parsedURL = parsedURL.resolve("/");
            }
            return Collections.singletonList(new StringField(targetFieldName, parsedURL.toString(), Field.Store.YES));
        } catch (URISyntaxException e) {
            throw new InvalidWikibaseValueException(value + " is an invalid URI", e);
        }
    }
}
