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

package us.askplatyp.kb.lucene.model;

import us.askplatyp.kb.lucene.model.value.LocaleStringValue;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class IndexableResource extends Resource {
    private Set<LocaleStringValue> labels = new HashSet<>();
    private int rank = 0;

    public IndexableResource(String IRI) {
        super(IRI);
    }

    public Stream<LocaleStringValue> getLabels() {
        return labels.stream();
    }

    public void addLabel(LocaleStringValue value) {
        labels.add(value);
    }

    public int getRank() {
        return rank;
    }

    public void addToRank(int val) {
        rank += val;
    }
}
