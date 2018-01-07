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

package us.askplatyp.kb.lucene.wikidata;

import com.bigdata.rdf.sail.BigdataSailRepository;
import org.apache.commons.compress.utils.IOUtils;
import org.glassfish.hk2.api.Factory;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.dumpfiles.DumpProcessingController;
import org.wikidata.wdtk.dumpfiles.MwLocalDumpFile;
import us.askplatyp.kb.lucene.CompositeIndex;
import us.askplatyp.kb.lucene.blazegraph.RepositoryBatchLoader;
import us.askplatyp.kb.lucene.blazegraph.SailFactory;
import us.askplatyp.kb.lucene.lucene.LuceneIndex;
import us.askplatyp.kb.lucene.lucene.LuceneLoader;

import java.io.*;
import java.util.zip.GZIPOutputStream;

/**
 * @author Thomas Pellissier Tanon
 */
public class FakeWikidataLuceneIndexFactory implements Factory<CompositeIndex> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FakeWikidataLuceneIndexFactory.class);

    private LuceneIndex index;
    private BigdataSailRepository repository;

    public FakeWikidataLuceneIndexFactory() {
        try {
            TemporaryFolder temporaryFolder = new TemporaryFolder();
            temporaryFolder.create();

            File fakeDumpFile = temporaryFolder.newFile("wikidata-20160829-all.json.gz");
            compressFileToGzip(new File(FakeWikidataLuceneIndexFactory.class.getResource("/wikidata-20160829-all.json").getPath()), fakeDumpFile);
            MwLocalDumpFile fakeDump = new MwLocalDumpFile(fakeDumpFile.getPath());

            File dbFile = temporaryFolder.newFile();
            dbFile.delete(); //THe file should not exist

            repository = SailFactory.openNoInferenceTripleRepository(temporaryFolder.newFile().getAbsolutePath());
            index = new LuceneIndex(temporaryFolder.newFolder().toPath());

            try (
                    WikidataTypeHierarchy typeHierarchy = new WikidataTypeHierarchy(dbFile.toPath());
                    RepositoryBatchLoader batchLoader = new RepositoryBatchLoader(repository.getConnection(), 100)
            ) {
                DumpProcessingController dumpProcessingController = new DumpProcessingController("wikidatawiki");
                dumpProcessingController.setDownloadDirectory(temporaryFolder.newFolder().toString());
                dumpProcessingController.registerEntityDocumentProcessor(typeHierarchy.getUpdateProcessor(), null, true);
                dumpProcessingController.processDump(fakeDump);

                dumpProcessingController.registerEntityDocumentProcessor(
                        new WikidataResourceProcessor(new LuceneLoader(index), dumpProcessingController.getSitesInformation(), typeHierarchy, batchLoader),
                        null,
                        true
                );
                dumpProcessingController.processDump(fakeDump);
                index.refreshReaders();
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private static void compressFileToGzip(File input, File output) throws IOException {
        try (
                InputStream inputStream = new FileInputStream(input);
                OutputStream outputStream = new GZIPOutputStream(new FileOutputStream(output))
        ) {
            IOUtils.copy(inputStream, outputStream);
        }
    }

    @Override
    public CompositeIndex provide() {
        return new CompositeIndex(index, repository);
    }

    @Override
    public void dispose(CompositeIndex luceneIndex) {
    }
}
