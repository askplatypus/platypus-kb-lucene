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

import org.apache.lucene.document.Field;
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;
import org.wikidata.wdtk.datamodel.interfaces.Value;

import java.util.stream.Stream;

/**
 * @author Thomas Pellissier Tanon
 */
interface StatementMainItemIdValueMapper extends StatementMainValueMapper {

    @Override
    default Stream<Field> mapMainValue(Value value) throws InvalidWikibaseValueException {
        if (!(value instanceof ItemIdValue)) {
            throw new InvalidWikibaseValueException(value + " should be a ItemIdValue");
        }
        return mapMainItemIdValue((ItemIdValue) value);
    }

    Stream<Field> mapMainItemIdValue(ItemIdValue value) throws InvalidWikibaseValueException;
}
