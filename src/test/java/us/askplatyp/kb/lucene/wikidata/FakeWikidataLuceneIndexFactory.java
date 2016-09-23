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

        index = new LuceneIndex(temporaryFolder.newFolder().toPath());
        DumpProcessingController dumpProcessingController = new DumpProcessingController("wikidatawiki");
        dumpProcessingController.setDownloadDirectory(temporaryFolder.newFolder().toString());
        dumpProcessingController.registerEntityDocumentProcessor(
                new LuceneUpdateProcessor(index, dumpProcessingController.getSitesInformation()),
                null,
                true
        );
        File fakeDump = temporaryFolder.newFile("wikidata-20160829-all.json.gz");
        compressFileToGzip(new File(FakeWikidataLuceneIndexFactory.class.getResource("/wikidata-20160829-all.json").getPath()), fakeDump);
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
