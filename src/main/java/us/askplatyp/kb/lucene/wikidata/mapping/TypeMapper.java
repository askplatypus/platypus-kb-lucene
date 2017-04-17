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

import jersey.repackaged.com.google.common.collect.Sets;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocument;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.wikibaseapi.WikibaseDataFetcher;
import org.wikidata.wdtk.wikibaseapi.apierrors.MediaWikiApiErrorException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Thomas Pellissier Tanon
 */
public class TypeMapper implements StatementMainItemIdValueMapper {

    private static final Set<ItemIdValue> FILTERED_TYPES = Sets.newHashSet(
            Datamodel.makeWikidataItemIdValue("Q17379835"),  //Wikimedia page outside the main knowledge tree
            Datamodel.makeWikidataItemIdValue("Q17442446"), //Wikimedia internal stuff
            Datamodel.makeWikidataItemIdValue("Q4167410"), //disambiguation page
            Datamodel.makeWikidataItemIdValue("Q13406463"), //list article
            Datamodel.makeWikidataItemIdValue("Q17524420"), //aspect of history
            Datamodel.makeWikidataItemIdValue("Q18340514")  //article about events in a specific year or time period
    );

    private static final Map<ItemIdValue, List<String>> SCHEMA_TYPES = new HashMap<>();
    private static final TypeMapper INSTANCE = new TypeMapper();

    static {
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q5"), Collections.singletonList("Person")); //human
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q532"), Collections.singletonList("Place")); //village
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q13100073"), Collections.singletonList("Place")); //Chinese village TODO
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q79007"), Collections.singletonList("Place")); //street
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q486972"), Collections.singletonList("Place")); //human settlement
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q23397"), Arrays.asList("Place", "LakeBodyOfWater")); //lake
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q8502"), Arrays.asList("Place", "Mountain")); //mountain
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q4022"), Arrays.asList("Place", "RiverBodyOfWater")); //river
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q11424"), Arrays.asList("CreativeWork", "Movie")); //film
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q482994"), Arrays.asList("CreativeWork", "MusicPlaylist", "MusicAlbum")); //album
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q13442814"), Arrays.asList("CreativeWork", "Article", "ScholarlyArticle")); //scientific article
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q16970"), Arrays.asList("Place", "CivicStructure", "PlaceOfWorship", "Church")); //
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q571"), Arrays.asList("CreativeWork", "Book")); //
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q215380"), Arrays.asList("Organization", "MusicGroup")); //
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q7889"), Arrays.asList("CreativeWork", "Game", "SoftwareApplication", "VideoGame")); //
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q43229"), Collections.singletonList("Organization")); //
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q515"), Arrays.asList("Place", "AdministrativeArea", "City")); //
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q191067"), Arrays.asList("CreativeWork", "Article")); //
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q41298"), Arrays.asList("CreativeWork", "CreativeWorkSeries", "Periodical")); //
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q207628"), Arrays.asList("CreativeWork", "MusicComposition")); //
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q125191"), Arrays.asList("CreativeWork", "Photograph")); //
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q11707"), Arrays.asList("Place", "Organization", "LocalBusiness", "FoodEstablishment", "Restaurant")); //
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q1420"), Arrays.asList("Product", "Vehicle", "Car")); //
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q431289"), Collections.singletonList("Brand")); //
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q11410"), Arrays.asList("CreativeWork", "Game")); //
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q166142"), Arrays.asList("CreativeWork", "SoftwareApplication")); //
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q132241"), Arrays.asList("Event", "Festival")); //
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q277759"), Arrays.asList("CreativeWork", "CreativeWorkSeries", "BookSeries")); //
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q56061"), Arrays.asList("Place", "AdministrativeArea")); //
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q30022"), Arrays.asList("Place", "Organization", "LocalBusiness", "FoodEstablishment", "CafeOrCoffeeShop")); //
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q3305213"), Arrays.asList("CreativeWork", "Painting")); //
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q1248784"), Arrays.asList("Place", "CivicStructure", "Airport")); //
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q1983062"), Arrays.asList("CreativeWork", "Episode")); //
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q1983062"), Arrays.asList("CreativeWork", "CreativeWorkSeries", "VideoGameSeries")); //
    }

    private Map<ItemIdValue, List<ItemIdValue>> subclassOfCache = new HashMap<>();

    private TypeMapper() {
    }

    public static TypeMapper getInstance() {
        return INSTANCE;
    }

    @Override
    public List<Field> mapMainItemIdValue(ItemIdValue value) throws InvalidWikibaseValueException {
        return mapClass(value).stream()
                .map(type -> new StringField("@type", type, Field.Store.YES))
                .collect(Collectors.toList());
    }

    private Set<String> mapClass(ItemIdValue itemId) {
        return getAllSuperClasses(itemId).stream()
                .flatMap(itemIdV -> SCHEMA_TYPES.getOrDefault(itemIdV, Collections.emptyList()).stream())
                .collect(Collectors.toSet());
    }

    public boolean isFilteredClass(ItemIdValue itemId) {
        return getAllSuperClasses(itemId).stream()
                .anyMatch(FILTERED_TYPES::contains);
    }

    private Set<ItemIdValue> getAllSuperClasses(ItemIdValue itemId) {
        Set<ItemIdValue> superClasses = new HashSet<>();
        superClasses.add(itemId);

        Stack<ItemIdValue> toGet = new Stack<>();
        toGet.add(itemId);
        while (!toGet.empty()) {
            for (ItemIdValue superClass : getDirectSuperClasses(toGet.pop())) {
                if (!superClasses.contains(superClass)) {
                    superClasses.add(superClass);
                    toGet.add(superClass);
                }
            }
        }

        return superClasses;
    }

    private List<ItemIdValue> getDirectSuperClasses(ItemIdValue itemId) {
        if (!subclassOfCache.containsKey(itemId)) {
            subclassOfCache.put(itemId, retrieveDirectSuperClasses(itemId));
        }
        return subclassOfCache.get(itemId);
    }

    private List<ItemIdValue> retrieveDirectSuperClasses(ItemIdValue itemId) {
        try {
            EntityDocument document = WikibaseDataFetcher.getWikidataDataFetcher().getEntityDocument(itemId.toString());
            if (!(document instanceof ItemDocument)) {
                return Collections.emptyList();
            }
            return ((ItemDocument) document).findStatementGroup("P279").getStatements().stream()
                    .map(Statement::getValue)
                    .flatMap(value -> {
                        if (value instanceof ItemIdValue) {
                            return Stream.of((ItemIdValue) value);
                        } else {
                            return Stream.empty();
                        }
                    }).collect(Collectors.toList());
        } catch (MediaWikiApiErrorException e) {
            return Collections.emptyList();
        }
    }
}
