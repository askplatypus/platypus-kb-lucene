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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Thomas Pellissier Tanon
 */
abstract class SearchResult<T> {

    private T result;
    private float score;

    SearchResult(T result, float score) {
        this.result = result;
        this.score = score;
    }

    @JsonProperty("@type")
    public abstract String getType();

    @JsonProperty("result")
    public T getResult() {
        return this.result;
    }

    @JsonProperty("resultScore")
    public float getScore() {
        return this.score;
    }
}
