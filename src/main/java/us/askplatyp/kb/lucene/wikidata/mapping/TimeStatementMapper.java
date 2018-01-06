/*
 * Copyright (c) 2018 Platypus Knowledge Base developers.
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

import org.wikidata.wdtk.datamodel.interfaces.TimeValue;
import us.askplatyp.kb.lucene.model.Claim;
import us.askplatyp.kb.lucene.wikidata.WikibaseValueUtils;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.stream.Stream;

/**
 * @author Thomas Pellissier Tanon
 */
class TimeStatementMapper implements StatementMainTimeValueMapper {

    private String targetFieldName;

    TimeStatementMapper(String targetFieldName) {
        this.targetFieldName = targetFieldName;
    }

    @Override
    public Stream<Claim> mapMainTimeValue(TimeValue value) throws InvalidWikibaseValueException {
        return convertTimeValue(value).map(calendarValue ->
                new Claim(targetFieldName, calendarValue)
        );
    }

    private Stream<XMLGregorianCalendar> convertTimeValue(TimeValue value) throws InvalidWikibaseValueException {
        return WikibaseValueUtils.toXmlGregorianCalendar(value).map(Stream::of).orElseGet(Stream::empty);
    }
}
