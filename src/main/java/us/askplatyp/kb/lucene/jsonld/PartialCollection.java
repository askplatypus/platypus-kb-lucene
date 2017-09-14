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

package us.askplatyp.kb.lucene.jsonld;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * @author Thomas Pellissier Tanon
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PartialCollection<T> extends Collection<T> {

    private PartialCollectionView view;

    @JsonCreator
    PartialCollection(
            @JsonProperty("element") List<T> elements,
            @JsonProperty("totalItems") int totalNumber,
            @JsonProperty("hydra:first") String baseIRI,
            String currentContinue, //TODO
            String nextContinue
    ) {
        super(elements, totalNumber);

        this.view = new PartialCollectionView(baseIRI, currentContinue, nextContinue);
    }

    private static String encode(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    @JsonProperty("hydra:view")
    public PartialCollectionView getView() {
        return view;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private static class PartialCollectionView {

        private String baseIRI;
        private String currentContinue;
        private String nextContinue;

        PartialCollectionView(String baseIRI, String currentContinue, String nextContinue) {
            this.baseIRI = baseIRI;
            this.currentContinue = currentContinue;
            this.nextContinue = nextContinue;
        }

        @JsonProperty("@id")
        public String getId() {
            return (currentContinue == null) ? baseIRI : baseIRI + "&continue=" + encode(currentContinue);
        }

        @JsonProperty("@type")
        public String getType() {
            return "hydra:PartialCollectionView";
        }

        @JsonProperty("hydra:first")
        public String getFirst() {
            return baseIRI;
        }

        @JsonProperty("hydra:next")
        public String getNext() {
            return (nextContinue == null) ? null : baseIRI + "&continue=" + encode(nextContinue);
        }
    }
}
