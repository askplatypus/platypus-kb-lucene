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

package us.askplatyp.kb.lucene;

import com.bigdata.rdf.sail.BigdataSailRepository;
import org.glassfish.hk2.api.Factory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.dumpfiles.DumpContentType;
import org.wikidata.wdtk.dumpfiles.DumpProcessingController;
import org.wikidata.wdtk.dumpfiles.MwDumpFile;
import org.wikidata.wdtk.dumpfiles.wmf.WmfDumpFileManager;
import us.askplatyp.kb.lucene.blazegraph.SailFactory;
import us.askplatyp.kb.lucene.lucene.LuceneIndex;
import us.askplatyp.kb.lucene.lucene.LuceneLoader;
import us.askplatyp.kb.lucene.wikidata.WikidataResourceProcessor;
import us.askplatyp.kb.lucene.wikidata.WikidataTypeHierarchy;

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
public class WikidataLuceneIndexFactory implements Factory<CompositeIndex> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WikidataLuceneIndexFactory.class);
    private static final LastProcessedDumpInfo LAST_PROCESSED_DUMP_INFO = new LastProcessedDumpInfo();

    private static LuceneIndex index;
    private static BigdataSailRepository repository;
    private static WikidataTypeHierarchy typeHierarchy;

    public static void init(String luceneDirectoryPath) throws IOException, RepositoryException {
        if (index != null || repository != null) {
            throw new IOException("Wikidata indexes already initialized");
        }
        Path path = Paths.get(luceneDirectoryPath);
        index = new LuceneIndex(path);
        repository = SailFactory.openNoInferenceTripleRepository(path.resolve("blazegraph.jnl").getFileName().toString());
        typeHierarchy = new WikidataTypeHierarchy(Paths.get(luceneDirectoryPath, "wd-builder"));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                index.close();
                repository.shutDown();
                typeHierarchy.close();
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }));
        loadData();
    }

    private static void loadData() throws IOException, RepositoryException {
        loadTypeHierarchy();
        doInitialLoading();

        registerAtThreePM(() -> {
            try {
                RepositoryConnection connection = repository.getConnection();
                try {
                    DumpProcessingController dumpProcessingController = buildRegularProcessingControler(connection);
                    MwDumpFile dump = dumpProcessingController.getMostRecentDump(DumpContentType.DAILY);
                    dumpProcessingController.processDump(dump);
                    LAST_PROCESSED_DUMP_INFO.setDateStamp(dump.getDateStamp());
                    index.refreshReaders();
                } finally {
                    connection.close();
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        });
    }

    private static void loadTypeHierarchy() throws IOException {
        DumpProcessingController dumpProcessingController = new DumpProcessingController("wikidatawiki");
        dumpProcessingController.setDownloadDirectory(Configuration.getInstance().getWikidataDirectory());
        dumpProcessingController.setLanguageFilter(Collections.emptySet());
        dumpProcessingController.setSiteLinkFilter(Collections.emptySet());
        dumpProcessingController.registerEntityDocumentProcessor(typeHierarchy.getUpdateProcessor(), null, true);
        dumpProcessingController.processMostRecentJsonDump();
    }

    private static void doInitialLoading() throws IOException, RepositoryException {
        RepositoryConnection connection = repository.getConnection();
        try {
            DumpProcessingController dumpProcessingController = buildRegularProcessingControler(connection);
            for (MwDumpFile dump : getNewDumpsToProcess(dumpProcessingController.getWmfDumpFileManager()).toArray(MwDumpFile[]::new)) {
                LOGGER.info("Processing " + dump.getProjectName() + " " + dump.getDumpContentType() + " of the " + dump.getDateStamp());
                try {
                    dumpProcessingController.processDump(dump);
                    LAST_PROCESSED_DUMP_INFO.setDateStamp(dump.getDateStamp());
                    index.refreshReaders();
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        } finally {
            connection.close();
        }
    }

    private static DumpProcessingController buildRegularProcessingControler(RepositoryConnection connection) throws IOException {
        DumpProcessingController dumpProcessingController = new DumpProcessingController("wikidatawiki");
        dumpProcessingController.setDownloadDirectory(Configuration.getInstance().getWikidataDirectory());
        dumpProcessingController.setLanguageFilter(WikidataResourceProcessor.SUPPORTED_LANGUAGES);
        dumpProcessingController.registerEntityDocumentProcessor(
                new WikidataResourceProcessor(new LuceneLoader(index), dumpProcessingController.getSitesInformation(), typeHierarchy, connection),
                null, true
        );
        return dumpProcessingController;
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
    public CompositeIndex provide() {
        return new CompositeIndex(index, repository);
    }

    @Override
    public void dispose(CompositeIndex index) {
    }

    private static class LastProcessedDumpInfo {
        private static final Path STORAGE_FILE = Paths.get(Configuration.getInstance().getLuceneDirectory(), "last-wd-dump-info");
        private String dateStamp = "";

        String getDateStamp() {
            if (dateStamp.isEmpty()) {
                try {
                    dateStamp = Files.readAllLines(STORAGE_FILE).get(0);
                } catch (IOException e) {
                    dateStamp = "20120101";
                }
            }
            return dateStamp;
        }

        void setDateStamp(String dateStamp) {
            this.dateStamp = dateStamp;
            try {
                Files.write(STORAGE_FILE, Collections.singletonList(dateStamp));
            } catch (IOException e) {
                LOGGER.warn("Saving of last parsed dump date stamp failed", e);
            }
        }
    }
}
