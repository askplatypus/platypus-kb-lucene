package us.askplatyp.kb.lucene.wikidata;

import org.glassfish.hk2.api.Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.dumpfiles.DumpContentType;
import org.wikidata.wdtk.dumpfiles.DumpProcessingController;
import org.wikidata.wdtk.dumpfiles.MwDumpFile;
import org.wikidata.wdtk.dumpfiles.wmf.WmfDumpFileManager;
import us.askplatyp.kb.lucene.Configuration;
import us.askplatyp.kb.lucene.lucene.LuceneIndex;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * @author Thomas Pellissier Tanon
 */
public class WikidataLuceneIndexFactory implements Factory<LuceneIndex> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WikidataLuceneIndexFactory.class);
    private static final LastProcessedDumpInfo LAST_PROCESSED_DUMP_INFO = new LastProcessedDumpInfo();

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
        dumpProcessingController.setLanguageFilter(Configuration.SUPPORTED_LANGUAGES);
        dumpProcessingController.registerEntityDocumentProcessor(
                new LuceneUpdateProcessor(index, dumpProcessingController.getSitesInformation()),
                null,
                true
        );
        for (MwDumpFile dump : getNewDumpsToProcess(dumpProcessingController.getWmfDumpFileManager()).toArray(MwDumpFile[]::new)) {
            dumpProcessingController.processDump(dump);
            LAST_PROCESSED_DUMP_INFO.setDateStamp(dump.getDateStamp());
            index.refreshReaders();
        }

        registerAtThreePM(() -> {
            try {
                MwDumpFile dump = dumpProcessingController.getMostRecentDump(DumpContentType.DAILY);
                dumpProcessingController.processDump(dump);
                LAST_PROCESSED_DUMP_INFO.setDateStamp(dump.getDateStamp());
                index.refreshReaders();
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        });
    }

    private static Stream<MwDumpFile> getNewDumpsToProcess(WmfDumpFileManager dumpFileManager) {
        return getPossibleDumpsToProcess(dumpFileManager).filter(dump ->
                dump.getDateStamp().compareTo(LAST_PROCESSED_DUMP_INFO.getDateStamp()) > 0
        );
    }

    private static Stream<MwDumpFile> getPossibleDumpsToProcess(WmfDumpFileManager dumpFileManager) {
        MwDumpFile jsonDump = dumpFileManager.findMostRecentDump(DumpContentType.JSON);
        return Stream.concat(
                Stream.of(jsonDump),
                dumpFileManager.findAllRelevantRevisionDumps(true).stream()
                        .filter(dump -> Integer.parseInt(dump.getDateStamp()) > Integer.parseInt(jsonDump.getDateStamp()))
        ).sorted(new MwDumpFile.DateComparator());
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

    private static class LastProcessedDumpInfo {
        private static final Path STORAGE_FILE = Paths.get(Configuration.getInstance().getLuceneDirectory(), "last-wd-dump-info");
        private String dateStamp = "";

        String getDateStamp() {
            if (dateStamp.isEmpty()) {
                try {
                    dateStamp = Files.readAllLines(STORAGE_FILE).get(0);
                } catch (IOException e) {
                    //TODO: do something
                }
            }
            return dateStamp;
        }

        void setDateStamp(String dateStamp) {
            this.dateStamp = dateStamp;
            try {
                Files.write(STORAGE_FILE, Collections.singletonList(dateStamp));
            } catch (IOException e) {
                //TODO: do something?
            }
        }
    }
}
