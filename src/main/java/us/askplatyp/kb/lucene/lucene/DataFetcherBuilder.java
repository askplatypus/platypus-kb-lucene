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

import graphql.schema.DataFetcher;
import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.datamodel.interfaces.WikimediaLanguageCodes;
import us.askplatyp.kb.lucene.model.Namespaces;
import us.askplatyp.kb.lucene.model.value.Article;
import us.askplatyp.kb.lucene.model.value.Image;
import us.askplatyp.kb.lucene.wikimedia.rest.WikimediaREST;
import us.askplatyp.kb.lucene.wikimedia.rest.model.Summary;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Thomas Pellissier Tanon
 */
public class DataFetcherBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataFetcherBuilder.class);

    private LuceneIndex index;

    public DataFetcherBuilder(LuceneIndex index) {
        this.index = index;
    }

    public DataFetcher entityForIRIFetcher() {
        return entityForIRIFetcher(null);
    }

    public DataFetcher entityForIRIFetcher(String requiredType) {
        return environment -> {
            String IRI = environment.getArgument("id");
            try (LuceneIndex.Reader reader = index.getReader()) {
                return reader.getDocumentForIRI(Namespaces.reduce(IRI)).filter(document ->
                        requiredType == null || retrieveTypes(document).contains(requiredType)
                ).orElse(null);
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
                return null;
            }
        };
    }

    public List<String> retrieveTypes(Object object) {
        if (object == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(((Document) object).getValues("@type"));
    }

    public DataFetcher stringPropertyFetcher(String property) {
        return environment -> ((Document) environment.getSource()).get(property);
    }

    public DataFetcher stringsPropertyFetcher(String property) {
        return environment -> Arrays.asList(((Document) environment.getSource()).getValues(property));
    }

    public DataFetcher entityPropertyFetcher(String property) {
        return environment -> {
            String IRI = ((Document) environment.getSource()).get(property);
            if (IRI == null) {
                return null;
            }
            try (LuceneIndex.Reader reader = index.getReader()) {
                return reader.getDocumentForIRI(IRI).orElse(null);
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
                return null;
            }
        };
    }

    public DataFetcher entitiesPropertyFetcher(String property) {
        return environment -> {
            try (LuceneIndex.Reader reader = index.getReader()) {
                return Arrays.stream(((Document) environment.getSource()).getValues(property))
                        .flatMap(IRI -> {
                            try {
                                return reader.getDocumentForIRI(IRI).map(Stream::of).orElseGet(Stream::empty);
                            } catch (IOException e) {
                                LOGGER.error(e.getMessage(), e);
                                return Stream.empty();
                            }
                        })
                        .collect(Collectors.toList());
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
                return Collections.emptyList();
            }
        };
    }

    public DataFetcher languageStringPropertyFetcher(String property) {
        return environment -> {
            Locale locale = environment.getArgument("language");
            String value = ((Document) environment.getSource()).get(property + "@" + locale.getLanguage());
            return value;
        };
    }

    public DataFetcher languageStringsPropertyFetcher(String property) {
        return environment -> {
            Locale locale = environment.getArgument("language");
            return Arrays.asList(((Document) environment.getSource()).getValues(property + "@" + locale.getLanguage()));
        };
    }

    public DataFetcher wikipediaArticleFetcher() {
        return environment -> {
            Locale locale = environment.getArgument("language");
            for (String IRI : ((Document) environment.getSource()).getValues("sameAs")) {
                if (IRI.contains(locale.getLanguage() + ".wikipedia.org/wiki/")) { //TODO: support complex language codes
                    try {
                        Summary summary = WikimediaREST.getInstance().getSummary(IRI);
                        return new Article(
                                IRI,
                                summary.getTitle(),
                                summary.getExtract(),
                                "http://creativecommons.org/licenses/by-sa/3.0/",
                                WikimediaLanguageCodes.getLanguageCode(summary.getLanguageCode())
                        );
                    } catch (IOException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            }
            return null;
        };
    }

    public DataFetcher wikipediaImageFetcher() {
        return environment -> {
            for (String IRI : ((Document) environment.getSource()).getValues("sameAs")) {
                if (IRI.contains(".wikipedia.org/wiki/")) {
                    try {
                        Optional<Image> imageOption = WikimediaREST.getInstance().getSummary(IRI).getThumbnail()
                                .map(thumbnail -> new Image(thumbnail.getSource()));
                        if (imageOption.isPresent()) {
                            return imageOption.get();
                        }
                    } catch (IOException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            }
            return null;
        };
    }
}
