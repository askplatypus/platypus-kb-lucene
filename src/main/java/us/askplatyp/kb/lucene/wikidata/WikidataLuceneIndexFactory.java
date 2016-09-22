package us.askplatyp.kb.lucene.wikidata;

import org.glassfish.hk2.api.Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.dumpfiles.DumpContentType;
import org.wikidata.wdtk.dumpfiles.DumpProcessingController;
import us.askplatyp.kb.lucene.Configuration;
import us.askplatyp.kb.lucene.http.Main;
import us.askplatyp.kb.lucene.lucene.LuceneIndex;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Thomas Pellissier Tanon
 */
public class WikidataLuceneIndexFactory implements Factory<LuceneIndex> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WikidataLuceneIndexFactory.class);

    private static LuceneIndex index;

    public static void init(String luceneDirectoryPath) throws IOException {
        if (index != null) {
            throw new IOException("Wikidata Lucene index already initialized");
        }
        index = new LuceneIndex(Paths.get(luceneDirectoryPath));
        loadData();
    }

    private static void loadData() throws IOException {
        DumpProcessingController dumpProcessingController = new DumpProcessingController("wikidatawiki");
        dumpProcessingController.setDownloadDirectory(Configuration.getInstance().getWikidataDirectory());
        dumpProcessingController.setLanguageFilter(Main.SUPPORTED_LANGUAGES);
        dumpProcessingController.registerEntityDocumentProcessor(
                new LuceneUpdateProcessor(index, dumpProcessingController.getSitesInformation()),
                null,
                true
        );
        dumpProcessingController.processMostRecentJsonDump();
        index.refreshReaders();
        dumpProcessingController.processAllRecentRevisionDumps();
        //dumpProcessingController.processDump(dumpProcessingController.getMostRecentDump(DumpContentType.DAILY));
        index.refreshReaders();

        registerAtThreePM(() -> {
            try {
                dumpProcessingController.processDump(dumpProcessingController.getMostRecentDump(DumpContentType.DAILY));
                index.refreshReaders();
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        });
    }

    private static void registerAtThreePM(Runnable runnable) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threePM = LocalDate.now().atTime(15, 0);
        if (threePM.isBefore(now)) {
            threePM = threePM.plusDays(1);
        }
        long start = LocalDateTime.now().until(threePM, ChronoUnit.HOURS);
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(runnable, start, Duration.ofDays(1).toHours(), TimeUnit.HOURS);
    }

    @Override
    public LuceneIndex provide() {
        return index;
    }

    @Override
    public void dispose(LuceneIndex luceneIndex) {
    }
}
