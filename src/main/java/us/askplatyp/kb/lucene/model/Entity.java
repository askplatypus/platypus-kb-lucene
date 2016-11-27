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

package us.askplatyp.kb.lucene.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Thomas Pellissier Tanon
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Entity {

    private String IRI;
    private String[] types;
    private String name;
    private String description;
    private String[] alternateNames;
    private String officialWebsiteIRI;
    private String[] sameAsIRIs;
    private Image image;
    private Article detailedDescription;
    private String[] rangeIncludes;
    private CalendarValue birthDate;
    private String birthPlace;
    private CalendarValue deathDate;
    private String deathPlace;
    private String nationality;

    @JsonCreator
    public Entity(
            @JsonProperty("@id") String IRI,
            @JsonProperty("@type") String[] types,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("alternateName") String[] alternateNames,
            @JsonProperty("url") String officialWebsiteIRI,
            @JsonProperty("sameAs") String[] sameAsIRIs,
            @JsonProperty("image") Image image,
            @JsonProperty("detailedDescription") Article detailedDescription,
            @JsonProperty("rangeIncludes") String[] rangeIncludes,
            @JsonProperty("birthDate") CalendarValue birthDate,
            @JsonProperty("birthPlace") String birthPlace,
            @JsonProperty("deathDate") CalendarValue deathDate,
            @JsonProperty("deathPlace") String deathPlace,
            @JsonProperty("nationality") String nationality
    ) {
        this.IRI = IRI;
        this.types = types;
        this.name = name;
        this.description = description;
        this.alternateNames = alternateNames;
        this.officialWebsiteIRI = officialWebsiteIRI;
        this.sameAsIRIs = sameAsIRIs;
        this.image = image;
        this.detailedDescription = detailedDescription;
        this.rangeIncludes = rangeIncludes;
        this.birthDate = birthDate;
        this.birthPlace = birthPlace;
        this.deathDate = deathDate;
        this.deathPlace = deathPlace;
        this.nationality = nationality;
    }

    @JsonProperty("@id")
    public String getIRI() {
        return IRI;
    }

    @JsonProperty("@type")
    public String[] getTypes() {
        return types;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("alternateName")
    public String[] getAlternateNames() {
        return alternateNames;
    }

    @JsonProperty("url")
    public String getOfficialWebsiteIRI() {
        return officialWebsiteIRI;
    }

    @JsonProperty("sameAs")
    public String[] getSameAsIRIs() {
        return sameAsIRIs;
    }

    @JsonProperty("image")
    public Image getImage() {
        return image;
    }

    @JsonProperty("detailedDescription")
    public Article getDetailedDescription() {
        return detailedDescription;
    }

    @JsonProperty("rangeIncludes")
    public String[] getRangeIncludes() {
        return rangeIncludes;
    }

    @JsonProperty("birthDate")
    public CalendarValue getBirthDate() {
        return birthDate;
    }

    @JsonProperty("birthPlace")
    public String getBirthPlace() {
        return birthPlace;
    }

    @JsonProperty("deathDate")
    public CalendarValue getDeathDate() {
        return deathDate;
    }

    @JsonProperty("deathPlace")
    public String getDeathPlace() {
        return deathPlace;
    }

    @JsonProperty("nationality")
    public String getNationality() {
        return nationality;
    }
}
