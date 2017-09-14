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

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.QueryBuilder;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

/**
 * @author Thomas Pellissier Tanon
 */
public class LuceneIndex implements Closeable {

    private KnowledgeBaseAnalyzer analyzer = new KnowledgeBaseAnalyzer();
    private QueryBuilder queryBuilder = new QueryBuilder(analyzer);
    private IndexWriter indexWriter;
    private SearcherManager searcherManager;

    public LuceneIndex(Path luceneDirectoryPath) throws IOException {
        Directory luceneDirectory = FSDirectory.open(luceneDirectoryPath);
        this.indexWriter = new IndexWriter(luceneDirectory, new IndexWriterConfig(this.analyzer));
        indexWriter.commit(); //Makes sure that the index is created
        this.searcherManager = new SearcherManager(luceneDirectory, new SearcherFactory());
    }

    public Reader getReader() throws IOException {
        return new Reader();
    }

    public void putDocument(Document document, Term identifier) throws IOException {
        indexWriter.updateDocument(identifier, document); //TODO use revision
    }

    public KnowledgeBaseAnalyzer getAnalyzer() {
        return analyzer;
    }

    /**
     * WARNING: Expansive operation
     */
    public void refreshReaders() throws IOException {
        indexWriter.commit();
        searcherManager.maybeRefresh();
    }

    @Override
    public void close() throws IOException {
        indexWriter.close();
        searcherManager.close();
    }

    public class Reader implements Closeable {

        private IndexSearcher indexSearcher;

        private Reader() throws IOException {
            indexSearcher = searcherManager.acquire();
        }

        public QueryBuilder getQueryBuilder() {
            return queryBuilder;
        }

        KnowledgeBaseAnalyzer getAnalyzer() {
            return analyzer;
        }

        /**
         * @param IRI The entity IRI (IRIs should have been reduced)
         */
        public Optional<Document> getDocumentForIRI(String IRI) throws IOException {
            OptionalInt docID = getDocIdForIRI(IRI);
            if (docID.isPresent()) {
                return Optional.of(indexSearcher.doc(docID.getAsInt()));
            } else {
                return Optional.empty();
            }
        }

        /**
         * @param IRI The entity IRI (IRIs should have been reduced)
         */
        OptionalInt getDocIdForIRI(String IRI) throws IOException {
            ScoreDoc[] scoreDocs = indexSearcher.search(new TermQuery(new Term("@id", IRI)), 1).scoreDocs;
            switch (scoreDocs.length) {
                case 0:
                    return OptionalInt.empty();
                case 1:
                    return OptionalInt.of(scoreDocs[0].doc);
                default:
                    throw new RuntimeException("More than one document found for IRI " + IRI);
            }
        }

        Document getDocumentForDocId(int docID) throws IOException {
            return indexSearcher.doc(docID);
        }

        public Document getDocumentForDocId(int docID, Set<String> fieldsToLoad) throws IOException {
            return indexSearcher.doc(docID, fieldsToLoad);
        }

        TopDocs search(Query query, int limit) throws IOException {
            return indexSearcher.search(query, limit);
        }

        public TopDocs searchAfter(ScoreDoc after, Query query, int limit) throws IOException {
            return indexSearcher.searchAfter(after, query, limit);
        }

        @Override
        public void close() throws IOException {
            searcherManager.release(indexSearcher);
            indexSearcher = null;
        }
    }
}
