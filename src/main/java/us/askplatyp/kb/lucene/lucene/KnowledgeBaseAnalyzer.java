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

package us.askplatyp.kb.lucene.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.DelegatingAnalyzerWrapper;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author Thomas Pellissier Tanon
 */
class KnowledgeBaseAnalyzer extends DelegatingAnalyzerWrapper {

    private static Map<String, Analyzer> LUCENE_ANALYZERS = new TreeMap<>();
    private static Analyzer DEFAULT_ANALYZER = new SimpleAnalyzer();

    static {
        LUCENE_ANALYZERS.put("en", new EnglishAnalyzer());
        LUCENE_ANALYZERS.put("fr", new FrenchAnalyzer());
    }

    KnowledgeBaseAnalyzer() {
        super(PER_FIELD_REUSE_STRATEGY);
    }

    protected Analyzer getWrappedAnalyzer(String fieldName) {
        if (fieldName == null) {
            return DEFAULT_ANALYZER;
        }

        for (Map.Entry<String, Analyzer> entry : LUCENE_ANALYZERS.entrySet()) {
            if (fieldName.endsWith("@" + entry.getKey())) {
                return entry.getValue();
            }
        }
        return DEFAULT_ANALYZER;
    }
}
