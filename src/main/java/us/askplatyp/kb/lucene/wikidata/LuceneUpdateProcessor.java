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

import com.google.common.collect.Sets;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StringField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.interfaces.*;
import us.askplatyp.kb.lucene.lucene.LuceneIndex;
import us.askplatyp.kb.lucene.model.Namespaces;
import us.askplatyp.kb.lucene.wikidata.mapping.InvalidWikibaseValueException;
import us.askplatyp.kb.lucene.wikidata.mapping.MapperRegistry;
import us.askplatyp.kb.lucene.wikidata.mapping.TypeMapper;

import java.io.IOException;
import java.util.*;

/**
 * @author Thomas Pellissier Tanon
 */
class LuceneUpdateProcessor implements EntityDocumentProcessor {

    static final Set<String> SUPPORTED_LANGUAGES = Sets.newHashSet(
            "ar", "am",
            "bg", "bn",
            "ca", "cs",
            "da", "de",
            "el", "en", "en-ca", "en-gb", "es", "et",
            "fa", "fi", "fr",
            "gu",
            "he", "hi", "hr", "hu",
            "id", "it",
            "ja",
            "kn", "ko",
            "la", "lt", "lv",
            "ml", "mr", "ms",
            "nl", "no",
            "pl", "pt", "pt-br",
            "ro", "ru",
            "sk", "sl", "sr", "sv", "sw",
            "ta", "te", "tl", "th", "tr",
            "uk",
            "vi",
            "zh", "zh-hans", "zh-hant"
    );
    private static final Logger LOGGER = LoggerFactory.getLogger(LuceneUpdateProcessor.class);
    private static final PropertyIdValue P31 = Datamodel.makeWikidataPropertyIdValue("P31");

    private LuceneIndex index;
    private Sites sites;
    private TypeMapper typeMapper;
    private MapperRegistry mapperRegistry;

    LuceneUpdateProcessor(LuceneIndex index, Sites sites, WikidataTypeHierarchy typeHierarchy) {
        this.index = index;
        this.sites = sites;
        this.typeMapper = new TypeMapper(typeHierarchy);
        this.mapperRegistry = new MapperRegistry(typeHierarchy);
    }

    @Override
    public void processItemDocument(ItemDocument itemDocument) {
        if (!isGoodItem(itemDocument)) {
            return;
        }

        Document document = new Document();
        document.add(new StringField("@id", Namespaces.reduce(itemDocument.getEntityId().getIri()), Field.Store.YES));
        document.add(new StringField("@type", "NamedIndividual", Field.Store.YES));
        addTermsToDocument(itemDocument, document);
        addSiteLinksToDocument(itemDocument, document);
        addStatementsToDocument(itemDocument, document);
        addScoreToDocument(itemDocument, document);
        writeDocument(document);
    }

    @Override
    public void processPropertyDocument(PropertyDocument propertyDocument) {
    }

    private boolean isGoodItem(ItemDocument itemDocument) {
        //TODO: filter elements without statements?
        return getBestStatements(itemDocument, P31).stream()
                .map(Statement::getValue)
                .noneMatch(value -> value instanceof ItemIdValue && typeMapper.isFilteredClass((ItemIdValue) value));
    }

    private void addTermsToDocument(TermedDocument termedDocument, Document document) {
        termedDocument.getLabels().values().forEach(label -> {
            document.add(toLabelField(label));
            document.add(toField("name", label));
        });
        termedDocument.getDescriptions().values().forEach(description ->
                document.add(toField("description", description))
        );
        termedDocument.getAliases().values().forEach(aliases ->
                aliases.forEach(alias -> {
                    document.add(toLabelField(alias));
                    document.add(toField("alternateName", alias));
                })
        );
    }

    private void addSiteLinksToDocument(ItemDocument itemDocument, Document document) {
        itemDocument.getSiteLinks().values().stream()
                .filter(siteLink ->
                        sites.getGroup(siteLink.getSiteKey()).equals("wikipedia") &&
                                SUPPORTED_LANGUAGES.contains(sites.getLanguageCode(siteLink.getSiteKey()))
                )
                .forEach(siteLink -> document.add(new StringField("sameAs", sites.getSiteLinkUrl(siteLink).replace("https://", "http://"), Field.Store.YES)));
    }

    private void addStatementsToDocument(StatementDocument statementDocument, Document document) {
        statementDocument.getStatementGroups().forEach(group ->
                getBestStatements(group).forEach(statement ->
                        mapperRegistry.getMapperForProperty(statement.getClaim().getMainSnak().getPropertyId()).ifPresent(mapper -> {
                            try {
                                mapper.mapStatement(statement).forEach(document::add);
                            } catch (InvalidWikibaseValueException e) {
                                LOGGER.warn(e.getMessage(), e);
                            }
                        })
                )
        );
    }

    private List<Statement> getBestStatements(StatementDocument statementDocument, PropertyIdValue property) {
        return Optional.ofNullable(statementDocument.findStatementGroup(property))
                .map(this::getBestStatements)
                .orElse(Collections.emptyList());
    }

    private List<Statement> getBestStatements(StatementGroup statementGroup) {
        List<Statement> preferred = new ArrayList<>();
        List<Statement> normals = new ArrayList<>();
        for (Statement statement : statementGroup.getStatements()) {
            if (statement.getRank().equals(StatementRank.PREFERRED)) {
                preferred.add(statement);
            } else if (statement.getRank().equals(StatementRank.NORMAL)) {
                normals.add(statement);
            }
        }
        if (preferred.isEmpty()) {
            return normals;
        } else {
            return preferred;
        }
    }

    private StringField toField(String name, MonolingualTextValue value) {
        return new StringField(
                name + "@" + WikimediaLanguageCodes.getLanguageCode(value.getLanguageCode()),
                value.getText(),
                Field.Store.YES
        );
    }

    private StringField toLabelField(MonolingualTextValue value) {
        Locale locale = Locale.forLanguageTag(WikimediaLanguageCodes.getLanguageCode(value.getLanguageCode()));
        return new StringField(
                "label@" + locale.getLanguage(), //TODO: variants
                value.getText().toLowerCase(locale),
                Field.Store.NO
        );
    }

    private void addScoreToDocument(ItemDocument itemDocument, Document document) {
        document.add(new NumericDocValuesField("score", itemDocument.getSiteLinks().size()));
    }

    private void writeDocument(Document document) {
        try {
            this.index.putDocument(document);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
