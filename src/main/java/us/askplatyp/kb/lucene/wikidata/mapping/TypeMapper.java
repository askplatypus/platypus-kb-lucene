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

import com.google.common.collect.Sets;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;
import us.askplatyp.kb.lucene.wikidata.WikidataTypeHierarchy;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(TypeMapper.class);
    private static final Map<ItemIdValue, List<String>> SCHEMA_TYPES = new HashMap<>();

    static {
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q5"), Collections.singletonList("Person")); //human
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q165"), Arrays.asList("Place", "Landform", "BodyOfWater", "SeaBodyOfWater"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q515"), Arrays.asList("Place", "AdministrativeArea", "City"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q532"), Collections.singletonList("Place")); //village
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q571"), Arrays.asList("CreativeWork", "Book"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q1004"), Arrays.asList("CreativeWork", "ComicStory")); //in bib: extension
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q1420"), Arrays.asList("Product", "Vehicle", "Car"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q3914"), Arrays.asList("Organization", "EducationalOrganization", "School"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q3918"), Arrays.asList("Organization", "EducationalOrganization", "CollegeOrUniversity"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q3947"), Arrays.asList("Place", "Accommodation", "House"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q4006"), Arrays.asList("CreativeWork", "Map"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q4022"), Arrays.asList("Place", "Landform", "BodyOfWater", "RiverBodyOfWater")); //river
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q5107"), Arrays.asList("Place", "Landform", "Continent"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q5638"), Arrays.asList("Product", "Vehicle", "BusOrCoach")); //Bus, in auto:
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q6256"), Arrays.asList("Place", "AdministrativeArea", "Country"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q7889"), Arrays.asList("CreativeWork", "Game", "SoftwareApplication", "VideoGame"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q8502"), Arrays.asList("Place", "Landform", "Mountain")); //mountain
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q3918"), Arrays.asList("Organization", "EducationalOrganization", "HighSchool")); //TODO: Us only
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q9842"), Arrays.asList("Organization", "EducationalOrganization", "ElementarySchool"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q11410"), Arrays.asList("CreativeWork", "Periodical", "Newspaper"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q11410"), Arrays.asList("CreativeWork", "Game"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q11424"), Arrays.asList("CreativeWork", "Movie")); //film
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q11707"), Arrays.asList("Place", "Organization", "LocalBusiness", "FoodEstablishment", "Restaurant"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q12280"), Arrays.asList("Place", "CivicStructure", "Bridge"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q16917"), Arrays.asList("Place", "CivicStructure", "Hospital"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q16970"), Arrays.asList("Place", "CivicStructure", "PlaceOfWorship", "Church"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q22698"), Arrays.asList("Place", "CivicStructure", "Park"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q23397"), Arrays.asList("Place", "LakeBodyOfWater")); //lake
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q24354"), Arrays.asList("Place", "CivicStructure", "PerformingArtsTheater"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q30022"), Arrays.asList("Place", "Organization", "LocalBusiness", "FoodEstablishment", "CafeOrCoffeeShop"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q30849"), Arrays.asList("CreativeWork", "Blog"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q33506"), Arrays.asList("Place", "CivicStructure", "Museum"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q34770"), Collections.singletonList("Language"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q35127"), Arrays.asList("CreativeWork", "WebSite"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q39614"), Arrays.asList("Place", "CivicStructure", "Cemetery"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q40080"), Arrays.asList("Place", "CivicStructure", "Beach"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q41253"), Arrays.asList("Place", "CivicStructure", "Organization", "LocalBusiness", "EntertainmentBusiness", "MovieTheater"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q41298"), Arrays.asList("CreativeWork", "CreativeWorkSeries", "Periodical"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q43229"), Collections.singletonList("Organization"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q55488"), Arrays.asList("Place", "CivicStructure", "Zoo"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q46970"), Arrays.asList("Organization", "Airline"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q55488"), Arrays.asList("Place", "CivicStructure", "TrainStation"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q56061"), Arrays.asList("Place", "AdministrativeArea"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q79007"), Collections.singletonList("Place")); //street
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q79913"), Arrays.asList("Organization", "NGO"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q95074"), Collections.singletonList("Person")); //fictional character
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q107390"), Arrays.asList("Place", "AdministrativeArea", "State"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q125191"), Arrays.asList("CreativeWork", "VisualArtwork", "Photograph"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q132241"), Arrays.asList("Event", "Festival"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q149566"), Arrays.asList("Organization", "EducationalOrganization", "MiddleSchool")); //TODO: US only
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q157570"), Arrays.asList("Place", "CivicStructure", "Crematorium"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q166142"), Arrays.asList("CreativeWork", "SoftwareApplication"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q191067"), Arrays.asList("CreativeWork", "Article"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q207628"), Arrays.asList("CreativeWork", "MusicComposition"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q215380"), Arrays.asList("Organization", "PerformingGroup", "MusicGroup"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q215627"), Collections.singletonList("Person")); //person
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q219239"), Arrays.asList("CreativeWork", "Recipe"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q277759"), Arrays.asList("CreativeWork", "CreativeWorkSeries", "BookSeries"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q431289"), Collections.singletonList("Brand"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q482994"), Arrays.asList("CreativeWork", "MusicPlaylist", "MusicAlbum")); //album
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q483110"), Arrays.asList("Place", "CivicStructure", "StadiumOrArena"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q486972"), Collections.singletonList("Place")); //human settlement
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q494829"), Arrays.asList("Place", "CivicStructure", "BusStation"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q543654"), Arrays.asList("Place", "CivicStructure", "GovernmentBuilding", "CityHall"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q629206"), Collections.singletonList("ComputerLanguage"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q860861"), Arrays.asList("CreativeWork", "VisualArtwork", "Sculpture"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q861951"), Arrays.asList("Place", "CivicStructure", "PoliceStation"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q928830"), Arrays.asList("Place", "CivicStructure", "SubwayStation"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q953806"), Arrays.asList("Place", "CivicStructure", "BusStop"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q132241"), Collections.singletonList("Event"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q149566"), Arrays.asList("Organization", "GovernmentOrganization"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q1137809"), Arrays.asList("Place", "CivicStructure", "GovernmentBuilding", "Courthouse"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q1195942"), Arrays.asList("Place", "CivicStructure", "FireStation"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q1248784"), Arrays.asList("Place", "CivicStructure", "Airport"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q1370598"), Arrays.asList("Place", "CivicStructure", "PlaceOfWorship"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q1980247"), Arrays.asList("CreativeWork", "Chapter"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q1983062"), Arrays.asList("CreativeWork", "Episode"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q2281788"), Arrays.asList("Place", "CivicStructure", "Aquarium"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q2393314"), Arrays.asList("Organization", "PerformingGroup", "DanceGroup"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q2416217"), Arrays.asList("Organization", "PerformingGroup", "TheaterGroup"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q3305213"), Arrays.asList("CreativeWork", "VisualArtwork", "Painting"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q3464665"), Arrays.asList("CreativeWork", "CreativeWorkSeason", "TVSeason")); //TV season
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q1137809"), Arrays.asList("Place", "CivicStructure", "GovernmentBuilding", "Embassy"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q4438121"), Arrays.asList("Organization", "SportsOrganization"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q4502142"), Arrays.asList("CreativeWork", "VisualArtwork"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q4830453"), Arrays.asList("Organization", "Corporation"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q5398426"), Arrays.asList("CreativeWork", "CreativeWorkSeries", "TVSeries"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q5707594"), Arrays.asList("CreativeWork", "Article", "NewsArticle"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q7058673"), Arrays.asList("CreativeWork", "CreativeWorkSeries", "VideoGameSeries"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q7138926"), Arrays.asList("Place", "CivicStructure", "GovernmentBuilding", "LegislativeBuilding"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q8719053"), Arrays.asList("Place", "CivicStructure", "MusicVenue"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q12973014"), Arrays.asList("Organization", "SportsOrganization", "SportsTeam"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q13100073"), Collections.singletonList("Place")); //Chinese village TODO
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q13442814"), Arrays.asList("CreativeWork", "Article", "ScholarlyArticle")); //scientific article
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q14406742"), Arrays.asList("CreativeWork", "CreativeWorkSeries", " Periodical", "ComicSeries"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q14623351"), Arrays.asList("CreativeWork", "CreativeWorkSeries", "RadioSeries"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q16831714"), Arrays.asList("Place", "CivicStructure", "GovernmentBuilding"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q17537576"), Collections.singletonList("CreativeWork")); //creative work
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q18674739"), Arrays.asList("Place", "CivicStructure", "EventVenue"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q1137809"), Arrays.asList("Place", "CivicStructure", "GovernmentBuilding", "DefenceEstablishment"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q19816504"), Arrays.asList("CreativeWork", "PublicationVolume"));
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q20950067"), Arrays.asList("Organization", "EducationalOrganization", "ElementarySchool")); //TODO: US only
        SCHEMA_TYPES.put(Datamodel.makeWikidataItemIdValue("Q27108230"), Arrays.asList("Place", "CivicStructure", "Organization", "LocalBusiness", "LodgingBusiness", "Campground"));
    }

    private WikidataTypeHierarchy typeHierarchy;

    public TypeMapper(WikidataTypeHierarchy typeHierarchy) {
        this.typeHierarchy = typeHierarchy;
    }

    @Override
    public Stream<Field> mapMainItemIdValue(ItemIdValue value) throws InvalidWikibaseValueException {
        return mapClass(value).stream()
                .map(type -> new StringField("@type", type, Field.Store.YES));
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
            for (ItemIdValue superClass : typeHierarchy.getSuperClasses(toGet.pop())) {
                if (!superClasses.contains(superClass)) {
                    superClasses.add(superClass);
                    toGet.add(superClass);
                }
            }
        }

        return superClasses;
    }
}
