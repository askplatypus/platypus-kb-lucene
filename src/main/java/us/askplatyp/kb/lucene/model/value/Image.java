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

package us.askplatyp.kb.lucene.model.value;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Thomas Pellissier Tanon
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Image {

    private String contentURL;
    private int width;
    private int height;

    @JsonCreator
    public Image(
            @JsonProperty("contentUrl") String contentURL,
            @JsonProperty("width") int width,
            @JsonProperty("height") int height
    ) {
        this.contentURL = contentURL;
        this.width = width;
        this.height = height;
    }

    @JsonProperty("@type")
    public String getType() {
        return "ImageObject";
    }

    @JsonProperty("contentUrl")
    public String getContentURL() {
        return contentURL;
    }

    @JsonProperty("width")
    public int getWidth() {
        return width;
    }

    @JsonProperty("height")
    public int getHeight() {
        return height;
    }
}
