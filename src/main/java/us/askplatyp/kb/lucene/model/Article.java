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
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Thomas Pellissier Tanon
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Article {

    private String IRI;
    private String title;
    private String articleHeadlines;
    private String licenseIRI;
    private String languageCode;

    @JsonCreator
    public Article(
            @JsonProperty("@id") String IRI,
            @JsonProperty("name") String title,
            @JsonProperty("articleBody") String articleHeadlines,
            @JsonProperty("license") String licenseIRI,
            @JsonProperty("inLanguage") String languageCode
    ) {
        this.IRI = IRI;
        this.title = title;
        this.articleHeadlines = articleHeadlines;
        this.licenseIRI = licenseIRI;
        this.languageCode = languageCode;
    }

    @JsonProperty("@id")
    public String getIRI() {
        return IRI;
    }

    @JsonProperty("@type")
    public String getType() {
        return "Article";
    }

    @JsonProperty("name")
    public String getTitle() {
        return title;
    }

    @JsonProperty("articleBody")
    public String getArticleHeadlines() {
        return articleHeadlines;
    }

    @JsonProperty("license")
    public String getLicenseIRI() {
        return licenseIRI;
    }

    @JsonProperty("inLanguage")
    public String getLanguageCode() {
        return languageCode;
    }
}
