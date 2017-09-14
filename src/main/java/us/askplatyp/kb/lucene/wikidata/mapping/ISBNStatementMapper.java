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

package us.askplatyp.kb.lucene.wikidata.mapping;

import org.apache.commons.validator.routines.ISBNValidator;
import org.wikidata.wdtk.datamodel.interfaces.StringValue;
import us.askplatyp.kb.lucene.model.Claim;

import java.util.stream.Stream;

/**
 * @author Thomas Pellissier Tanon
 */
class ISBNStatementMapper implements StatementMainStringValueMapper {

    @Override
    public Stream<Claim> mapMainStringValue(StringValue value) throws InvalidWikibaseValueException {
        String ISBN = ISBNValidator.getInstance().validate(value.getString());
        if (ISBN == null) {
            return Stream.empty();
        }
        return Stream.of(new Claim("isbn", ISBN));
    }
}
