package us.askplatyp.kb.lucene.lucene;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import us.askplatyp.kb.lucene.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Thomas Pellissier Tanon
 */
public class LuceneSearcher {

    private final static TopDocs EMPTY_TOP_DOCS = new TopDocs(0, new ScoreDoc[]{}, 0);

    private LuceneIndex.Reader entitiesReader;

    public LuceneSearcher(LuceneIndex.Reader entitiesReader) {
        this.entitiesReader = entitiesReader;
    }

    public JsonLdRoot<Entity> getEntityForIRI(String IRI, Locale outputLocale) throws ApiException {
        try {
            return new JsonLdRoot<>(
                    new Context(outputLocale),
                    EntityBuilder.getInstance()
                            .buildFullEntityInLanguage(
                                    entitiesReader.getDocumentForIRI(Namespaces.reduce(IRI))
                                            .orElseThrow(() -> new ApiException("Entity with IRI <" + IRI + "> not found.", 404)),
                                    outputLocale
                            )
            );
        } catch (IOException e) {
            throw new ApiException(e);
        }
    }

    public JsonLdRoot<Collection<EntitySearchResult<Entity>>> getEntitiesForLabel(
            String label, String type, Locale inputLocale, Locale outputLocale, String baseURI, String currentContinue, int limit
    ) throws ApiException {
        try {
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
            return buildSearchResults(searchResults, outputLocale, baseURI, startAfter, nextStartAfter, fuziness);
        } catch (IOException e) {
            throw new ApiException(e);
        }
    }

    private Query buildQueryForPhraseAndOrType(Locale locale, String label, String type, int fuziness) throws ApiException {
        if (type == null) {
            if (label == null) {
                return new MatchAllDocsQuery();
            } else {
                return buildFuzzyQueryForTerm(locale, label, fuziness);
            }
        } else {
            if (label == null) {
                return buildTermQuery("@type", type);
            } else {
                return new BooleanQuery.Builder()
                        .add(buildFuzzyQueryForTerm(locale, label, fuziness), BooleanClause.Occur.MUST)
                        .add(buildTermQuery("@type", type), BooleanClause.Occur.FILTER)
                        .build();
            }
        }
    }

    private Query buildFuzzyQueryForTerm(Locale locale, String label, int fuziness) throws ApiException {
        String name = "label@" + locale.getLanguage(); //TODO: variants
        Term term = new Term(name, label);
        if (fuziness == 0) {
            return new TermQuery(term);
        } else {
            return new FuzzyQuery(term, fuziness);
        }
    }

    private Query buildTermQuery(String field, String label) {
        return new TermQuery(new Term(field, label));
    }

    private JsonLdRoot<Collection<EntitySearchResult<Entity>>> buildSearchResults(
            TopDocs topDocs, Locale locale, String baseURI, ScoreDoc currentContinue, ScoreDoc nextContinue, int fuziness
    ) throws IOException {
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        List<EntitySearchResult<Entity>> searchResults = new ArrayList<>();
        for (ScoreDoc scoreDoc : scoreDocs) {
            searchResults.add(buildSearchResult(scoreDoc, locale));
        }
        return new JsonLdRoot<>(new Context(locale), new PartialCollection<>(
                searchResults, topDocs.totalHits, baseURI, serializeContinue(currentContinue, fuziness), serializeContinue(nextContinue, fuziness)
        ));
    }

    private EntitySearchResult<Entity> buildSearchResult(ScoreDoc scoreDoc, Locale locale) throws IOException {
        return new EntitySearchResult<>(
                EntityBuilder.getInstance().buildSimpleEntityInLanguage(entitiesReader.getDocumentForDocId(scoreDoc.doc), locale),
                scoreDoc.score
        );
    }

    private Continue parseContinue(String str) {
        if (str == null) {
            return null;
        }

        String[] parts = str.split("|", 3);
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
