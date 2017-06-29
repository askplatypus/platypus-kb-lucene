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

package us.askplatyp.kb.lucene.wikidata;

import org.mapdb.*;
import org.mapdb.serializer.GroupSerializerObjectArray;
import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.interfaces.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Thomas Pellissier Tanon
 */
public class WikidataTypeHierarchy implements AutoCloseable {
    private DB datatabase;
    private ConcurrentMap<ItemIdValue, List<ItemIdValue>> typeHierarchy;

    WikidataTypeHierarchy(Path file) {
        datatabase = DBMaker.fileDB(file.toFile()).fileMmapEnableIfSupported().make();
        typeHierarchy = datatabase
                .hashMap("wd-type-hierachy", new WikidataItemIdValueSerializer(), new WikidataItemIdValuesSerializer())
                .createOrOpen();
    }

    public List<ItemIdValue> getSuperClasses(ItemIdValue item) {
        return typeHierarchy.getOrDefault(item, Collections.emptyList());
    }

    EntityDocumentProcessor getUpdateProcessor() {
        return new EntityDocumentProcessor() {
            @Override
            public void processItemDocument(ItemDocument itemDocument) {
                StatementGroup statementGroup = itemDocument.findStatementGroup("P279");
                if (statementGroup != null) {
                    List<ItemIdValue> parents = statementGroup.getStatements().stream()
                            .map(Statement::getValue)
                            .flatMap(value -> {
                                if (value instanceof ItemIdValue) {
                                    return Stream.of((ItemIdValue) value);
                                } else {
                                    return Stream.empty();
                                }
                            }).collect(Collectors.toList());
                    typeHierarchy.put(itemDocument.getItemId(), parents);
                }
            }

            @Override
            public void processPropertyDocument(PropertyDocument propertyDocument) {
            }
        };
    }

    @Override
    public void close() throws Exception {
        datatabase.close();
    }

    private static class WikidataItemIdValueSerializer extends GroupSerializerObjectArray<ItemIdValue> {
        public void serialize(DataOutput2 out, ItemIdValue value) throws IOException {
            Serializer.STRING_ASCII.serialize(out, value.getId());
        }

        public ItemIdValue deserialize(DataInput2 in, int available) throws IOException {
            return Datamodel.makeWikidataItemIdValue(Serializer.STRING_ASCII.deserialize(in, available));
        }

        public boolean isTrusted() {
            return true;
        }

        public int hashCode(ItemIdValue value, int seed) {
            return Serializer.STRING_ASCII.hashCode(value.getId(), seed);
        }
    }

    private static class WikidataItemIdValuesSerializer extends GroupSerializerObjectArray<List<ItemIdValue>> {
        public void serialize(DataOutput2 out, List<ItemIdValue> values) throws IOException {
            String value = values.stream().map(ItemIdValue::getId).collect(Collectors.joining(" "));
            Serializer.STRING_ASCII.serialize(out, value);
        }

        public List<ItemIdValue> deserialize(DataInput2 in, int available) throws IOException {
            String value = Serializer.STRING_ASCII.deserialize(in, available);
            return Arrays.stream(value.split(" ")).map(Datamodel::makeWikidataItemIdValue).collect(Collectors.toList());
        }

        public boolean isTrusted() {
            return true;
        }

        public int hashCode(List<ItemIdValue> values, int seed) {
            String value = values.stream().map(ItemIdValue::getId).collect(Collectors.joining(" "));
            return Serializer.STRING_ASCII.hashCode(value, seed);
        }
    }
}
