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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.datamodel.interfaces.StringValue;
import us.askplatyp.kb.lucene.model.Claim;

import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * @author Thomas Pellissier Tanon
 */
class StringStatementMapper implements StatementMainStringValueMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(StringStatementMapper.class);

    private String targetFieldName;
    private Pattern pattern;

    StringStatementMapper(String targetFieldName, String pattern) {
        this.targetFieldName = targetFieldName;
        this.pattern = Pattern.compile(pattern);
    }

    StringStatementMapper(String targetFieldName) {
        this.targetFieldName = targetFieldName;
    }

    @Override
    public Stream<Claim> mapMainStringValue(StringValue value) {
        if (pattern != null && !pattern.matcher(value.getString()).matches()) {
            LOGGER.info(value + " is not a valid string value. It does not matches the pattern " + pattern);
            return Stream.empty();
        }
        return Stream.of(new Claim(targetFieldName, value.getString()));
    }
}

