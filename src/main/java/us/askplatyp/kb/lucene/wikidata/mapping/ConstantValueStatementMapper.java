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

import io.swagger.annotations.SwaggerDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.datamodel.interfaces.Value;
import us.askplatyp.kb.lucene.model.Claim;
import us.askplatyp.kb.lucene.model.value.ConstantValue;

import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Thomas Pellissier Tanon
 */
class ConstantValueStatementMapper implements StatementMainValueMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConstantValueStatementMapper.class);

    private String targetFieldName;
    private Map<Value, String> conversion;

    ConstantValueStatementMapper(String targetFieldName, Map<Value, String> conversion) {
        this.targetFieldName = targetFieldName;
        this.conversion = conversion;
    }

    @Override
    public Stream<Claim> mapMainValue(Value value) {
        if (!conversion.containsKey(value)) {
            LOGGER.info("Not expected constant for property " + targetFieldName + ": " + value);
            return Stream.empty();
        }
        return Stream.of(new Claim(targetFieldName, new ConstantValue(conversion.get(value))));
    }
}
