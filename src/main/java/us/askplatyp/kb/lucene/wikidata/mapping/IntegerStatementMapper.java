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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.datamodel.interfaces.QuantityValue;
import us.askplatyp.kb.lucene.model.Claim;

import java.math.BigDecimal;
import java.util.stream.Stream;

/**
 * @author Thomas Pellissier Tanon
 */
class IntegerStatementMapper implements StatementMainQuantityValueMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(IntegerStatementMapper.class);


    private String targetFieldName;

    IntegerStatementMapper(String targetFieldName) {
        this.targetFieldName = targetFieldName;
    }

    @Override
    public Stream<Claim> mapMainQuantityValue(QuantityValue value) {
        if (!value.getUnit().isEmpty()) {
            LOGGER.info("Integers should not have a unit: " + value);
            return Stream.empty();
        }
        if (
                (value.getLowerBound() != null && !value.getLowerBound().equals(BigDecimal.ZERO)) ||
                        (value.getUpperBound() != null && !value.getUpperBound().equals(BigDecimal.ZERO))
                ) {
            LOGGER.info("Integers should be exact: " + value);
            return Stream.empty();
        }
        try {
            return Stream.of(new Claim(targetFieldName, value.getNumericValue().toBigIntegerExact()));
        } catch (ArithmeticException e) {
            LOGGER.info(value + " is not an integer.");
            return Stream.empty();
        }
    }
}
