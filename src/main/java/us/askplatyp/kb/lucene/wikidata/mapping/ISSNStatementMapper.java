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

import org.apache.commons.validator.routines.ISSNValidator;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.wikidata.wdtk.datamodel.interfaces.StringValue;

import java.util.Collections;
import java.util.List;

/**
 * @author Thomas Pellissier Tanon
 */
class ISSNStatementMapper implements StatementMainStringValueMapper {

    @Override
    public List<Field> mapMainStringValue(StringValue value) throws InvalidWikibaseValueException {
        String ISSN = (String) ISSNValidator.getInstance().validate(value.getString());
        if (ISSN == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new StringField("issn", ISSN, Field.Store.YES));
    }
}
