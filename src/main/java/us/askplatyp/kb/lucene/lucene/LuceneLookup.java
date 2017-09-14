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

package us.askplatyp.kb.lucene.lucene;

import org.apache.lucene.index.Term;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.queries.function.FunctionQuery;
import org.apache.lucene.queries.function.valuesource.LongFieldSource;
import org.apache.lucene.search.*;
import us.askplatyp.kb.lucene.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * @author Thomas Pellissier Tanon
 */
public class LuceneLookup implements StorageLookup {

    private final static LuceneResourceBuilder RESOURCE_BUILDER = new LuceneResourceBuilder();
    private final static TopDocs EMPTY_TOP_DOCS = new TopDocs(0, new ScoreDoc[]{}, 0);

    private LuceneIndex.Reader entitiesReader;

    public LuceneLookup(LuceneIndex.Reader entitiesReader) {
        this.entitiesReader = entitiesReader;
    }

    public Optional<Resource> getResourceForIRI(String IRI) throws IOException {
        return entitiesReader
                .getDocumentForTerm(new Term("@id", Namespaces.reduce(IRI)))
                .map(RESOURCE_BUILDER::buildResource);
    }

    public ResourceSearchResult getResourcesForLabel(
            String label, String type, Locale inputLocale, String currentContinue, int limit
    ) throws IOException {
        type = Namespaces.reduce(type);
        Continue startAfter = parseContinue(currentContinue);
        TopDocs searchResults = EMPTY_TOP_DOCS;
        int fuziness;
        if (startAfter == null) {
            for (fuziness = 0; fuziness <= 2; fuziness++) {
                searchResults = entitiesReader.search(buildQueryForPhraseAndOrType(inputLocale, label, type, fuziness), limit);
                if (searchResults.totalHits > 0) {
                    break;
                }
            }
        } else {
            fuziness = startAfter.fuziness;
            searchResults = entitiesReader.searchAfter(startAfter, buildQueryForPhraseAndOrType(inputLocale, label, type, fuziness), limit);
        }
        ScoreDoc nextStartAfter = (searchResults.scoreDocs.length == limit) ? searchResults.scoreDocs[limit - 1] : null;
        return buildSearchResult(searchResults, startAfter, nextStartAfter, fuziness);
    }

    private Query buildQueryForPhraseAndOrType(Locale locale, String label, String type, int fuziness) {
        if (type == null) {
            if (label == null) {
                return addScoreBoostToQuery(new MatchAllDocsQuery());
            } else {
                return addScoreBoostToQuery(buildFuzzyQueryForTerm(locale, label, fuziness));
            }
        } else {
            if (label == null) {
                return addScoreBoostToQuery(buildTermQuery("@type", type));
            } else {
                return addScoreBoostToQuery(new BooleanQuery.Builder()
                        .add(buildFuzzyQueryForTerm(locale, label, fuziness), BooleanClause.Occur.MUST)
                        .add(buildTermQuery("@type", type), BooleanClause.Occur.FILTER)
                        .build());
            }
        }
    }

    private Query buildFuzzyQueryForTerm(Locale locale, String label, int fuziness) {
        String name = "label@" + locale.getLanguage(); //TODO: variants
        Term term = new Term(name, label.toLowerCase(locale));
        if (fuziness == 0) {
            return new TermQuery(term);
        } else {
            return new FuzzyQuery(term, fuziness);
        }
    }

    private Query buildTermQuery(String field, String label) {
        return new TermQuery(new Term(field, label));
    }

    private Query addScoreBoostToQuery(Query query) {
        return new CustomScoreQuery(query, new FunctionQuery(new LongFieldSource("score")));
    }

    private ResourceSearchResult buildSearchResult(TopDocs topDocs, ScoreDoc currentContinue, ScoreDoc nextContinue, int fuziness) throws IOException {
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        List<ScoredResource> searchResults = new ArrayList<>();
        for (ScoreDoc scoreDoc : scoreDocs) {
            searchResults.add(buildScoredResource(scoreDoc));
        }
        return new ResourceSearchResult(
                searchResults, topDocs.totalHits, serializeContinue(currentContinue, fuziness), serializeContinue(nextContinue, fuziness)
        );
    }

    private ScoredResource buildScoredResource(ScoreDoc scoreDoc) throws IOException {
        return new ScoredResource(
                RESOURCE_BUILDER.buildResource(entitiesReader.getDocumentForDocId(scoreDoc.doc)),
                scoreDoc.score
        );
    }

    private Continue parseContinue(String str) {
        if (str == null) {
            return null;
        }

        String[] parts = str.split("\\|", 3);
        return (parts.length == 3) ? new Continue(Integer.parseInt(parts[0]), Float.parseFloat(parts[1]), Integer.parseInt(parts[2])) : null;
    }

    private String serializeContinue(ScoreDoc scoreDoc, int fuziness) {
        return (scoreDoc == null) ? null : scoreDoc.doc + "|" + scoreDoc.score + "|" + fuziness;
    }

    private static class Continue extends ScoreDoc {

        int fuziness;

        Continue(int doc, float score, int fuziness) {
            super(doc, score);

            this.fuziness = fuziness;
        }
    }
}
