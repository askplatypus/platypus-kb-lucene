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

package us.askplatyp.kb.lucene.wikimedia.rest.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

/**
 * @author Thomas Pellissier Tanon
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Summary {

    private String title;
    private String extract;
    private Thumbnail thumbnail;
    private String lang;
    private String dir;

    @JsonCreator
    public Summary(
            @JsonProperty("title") String title,
            @JsonProperty("extract") String extract,
            @JsonProperty("thumbnail") Thumbnail thumbnail,
            @JsonProperty("lang") String lang,
            @JsonProperty("dir") String dir
    ) {
        this.title = title;
        this.extract = extract;
        this.thumbnail = thumbnail;
        this.lang = lang;
        this.dir = dir;
    }

    public String getTitle() {
        return title;
    }

    public String getExtract() {
        return extract;
    }

    public Optional<Thumbnail> getThumbnail() {
        return Optional.ofNullable(thumbnail);
    }

    public String getLanguageCode() {
        return lang;
    }

    public String getLanguageDirection() {
        return dir;
    }
}
