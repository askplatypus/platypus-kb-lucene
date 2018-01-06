/*
 * Copyright (c) 2018 Platypus Knowledge Base developers.
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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

/**
 * @author Thomas Pellissier Tanon
 */
public class JsonLdRoot<T> {

    private Context context;

    private T content;

    /*JsonLdRoot(Context context, T content) {
        this.context = context;
        this.content = content;
    }

    @JsonCreator
    JsonLdRoot(@JsonProperty("@context") Context context) {
        this.context = context;
    }*/

    @JsonCreator
    JsonLdRoot(@JsonProperty("@context") Context context, @JsonUnwrapped T content) {
        this.context = context;
        this.content = content;
    }

    @JsonProperty("@context")
    public Context getContext() {
        return context;
    }

    @JsonUnwrapped
    public T getContent() {
        return content;
    }
}
