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

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Thomas Pellissier Tanon
 */
class ExternalIdentifierStatementMapper implements StatementMainStringValueMapper {
    private String URITemplate;
    private Pattern pattern;

    ExternalIdentifierStatementMapper(String URITemplate, String pattern) {
        this.URITemplate = URITemplate;
        this.pattern = Pattern.compile(pattern);
    }

    @Override
    public List<Field> mapMainStringValue(StringValue value) throws InvalidWikibaseValueException {
        if (!pattern.matcher(value.getString()).matches()) {
            throw new InvalidWikibaseValueException(value + " is not a valid identifier. It does not matches the pattern " + pattern);
        }
        return Collections.singletonList(new StringField("sameAs", URITemplate.replace("$1", value.getString()), Field.Store.YES));
    }
}

