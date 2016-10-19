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
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;
import us.askplatyp.kb.lucene.wikidata.WikidataTypes;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Thomas Pellissier Tanon
 */
class P31StatementMapper implements StatementMainItemIdValueMapper {

    @Override
    public List<Field> mapMainItemIdValue(ItemIdValue value) throws InvalidWikibaseValueException {
        return WikidataTypes.getSchemaOrgTypes(value).stream()
                .map(type -> new StringField("@type", type, Field.Store.YES))
                .collect(Collectors.toList());
    }
}
