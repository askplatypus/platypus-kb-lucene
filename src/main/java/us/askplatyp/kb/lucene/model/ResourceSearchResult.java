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

package us.askplatyp.kb.lucene.model;

import java.util.List;

public class ResourceSearchResult {

    private List<ScoredResource> resources;
    private long totalHits;
    private String currentContinue;
    private String nextContinue;

    public ResourceSearchResult(List<ScoredResource> resources, long totalHits, String currentContinue, String nextContinue) {
        this.resources = resources;
        this.totalHits = totalHits;
        this.currentContinue = currentContinue;
        this.nextContinue = nextContinue;
    }

    public List<ScoredResource> getResources() {
        return resources;
    }

    public long getTotalHits() {
        return totalHits;
    }

    public String getCurrentContinue() {
        return currentContinue;
    }

    public String getNextContinue() {
        return nextContinue;
    }
}
