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

package us.askplatyp.kb.lucene.wikidata;

import org.apache.commons.compress.utils.IOUtils;
import org.glassfish.hk2.api.Factory;
import org.junit.rules.TemporaryFolder;
import org.wikidata.wdtk.dumpfiles.DumpProcessingController;
import org.wikidata.wdtk.dumpfiles.MwLocalDumpFile;
import us.askplatyp.kb.lucene.lucene.LuceneIndex;

import java.io.*;
import java.util.zip.GZIPOutputStream;

/**
 * @author Thomas Pellissier Tanon
 */
public class FakeWikidataLuceneIndexFactory implements Factory<LuceneIndex> {

    private LuceneIndex index;

    public FakeWikidataLuceneIndexFactory() throws IOException {
        TemporaryFolder temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();

        File fakeDump = temporaryFolder.newFile("wikidata-20160829-all.json.gz");
        compressFileToGzip(new File(FakeWikidataLuceneIndexFactory.class.getResource("/wikidata-20160829-all.json").getPath()), fakeDump);

        File dbFile = temporaryFolder.newFile();
        dbFile.delete();
        WikidataTypeHierarchy typeHierarchy = new WikidataTypeHierarchy(dbFile.toPath());
        index = new LuceneIndex(temporaryFolder.newFolder().toPath());
        DumpProcessingController dumpProcessingController = new DumpProcessingController("wikidatawiki");
        dumpProcessingController.setDownloadDirectory(temporaryFolder.newFolder().toString());
        dumpProcessingController.registerEntityDocumentProcessor(typeHierarchy.getUpdateProcessor(), null, true);
        dumpProcessingController.processDump(new MwLocalDumpFile(fakeDump.getPath()));

        dumpProcessingController.registerEntityDocumentProcessor(
                new LuceneUpdateProcessor(index, dumpProcessingController.getSitesInformation(), typeHierarchy),
                null,
                true
        );
        dumpProcessingController.processDump(new MwLocalDumpFile(fakeDump.getPath()));
        index.refreshReaders();

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
    public LuceneIndex provide() {
        return index;
    }

    @Override
    public void dispose(LuceneIndex luceneIndex) {
    }
}
