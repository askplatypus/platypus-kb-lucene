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

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.interfaces.*;

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
    private ConcurrentMap<String, String> typeHierarchy;

    WikidataTypeHierarchy(Path file) {
        datatabase = DBMaker.fileDB(file.toFile()).fileMmapEnableIfSupported().make();
        typeHierarchy = datatabase
                .hashMap("wd-type-hierachy", Serializer.STRING, Serializer.STRING)
                .createOrOpen();
    }

    public List<ItemIdValue> getSuperClasses(ItemIdValue item) {
        String parents = typeHierarchy.get(item.getId());
        if (parents == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(parents.split(" "))
                .map(Datamodel::makeWikidataItemIdValue)
                .collect(Collectors.toList());
    }

    EntityDocumentProcessor getUpdateProcessor() {
        return new EntityDocumentProcessor() {
            @Override
            public void processItemDocument(ItemDocument itemDocument) {
                StatementGroup statementGroup = itemDocument.findStatementGroup("P279");
                if (statementGroup != null) {
                    String parents = statementGroup.getStatements().stream()
                            .map(Statement::getValue)
                            .flatMap(value -> {
                                if (value instanceof ItemIdValue) {
                                    return Stream.of(((ItemIdValue) value).getId());
                                } else {
                                    return Stream.empty();
                                }
                            }).collect(Collectors.joining(" "));
                    if (!parents.equals("")) {
                        typeHierarchy.put(itemDocument.getItemId().getId(), parents);
                    }
                }
            }

            @Override
            public void processPropertyDocument(PropertyDocument propertyDocument) {
            }
        };
    }

    @Override
    public void close() {
        datatabase.close();
    }
}
