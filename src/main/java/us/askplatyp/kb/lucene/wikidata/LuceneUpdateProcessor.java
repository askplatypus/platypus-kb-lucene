package us.askplatyp.kb.lucene.wikidata;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.interfaces.*;
import us.askplatyp.kb.lucene.lucene.LuceneIndex;
import us.askplatyp.kb.lucene.wikidata.mapping.InvalidWikibaseValueException;
import us.askplatyp.kb.lucene.wikidata.mapping.MapperRegistry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author Thomas Pellissier Tanon
 */
public class LuceneUpdateProcessor implements EntityDocumentProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LuceneUpdateProcessor.class);

    private static final PropertyIdValue P31 = Datamodel.makeWikidataPropertyIdValue("P31");

    private LuceneIndex index;
    private Sites sites;

    public LuceneUpdateProcessor(LuceneIndex index, Sites sites) {
        this.index = index;
        this.sites = sites;
    }

    public void processItemDocument(ItemDocument itemDocument) {
        if (!isGoodItem(itemDocument)) {
            return;
        }

        Document document = createDocumentForEntity(itemDocument);
        document.add(new StringField("@type", "Thing", Field.Store.YES));
        addTermsToDocument(itemDocument, document);
        addSiteLinksToDocument(itemDocument, document);
        addStatementsToDocument(itemDocument, document);
        writeDocument(document);
    }

    private boolean isGoodItem(ItemDocument itemDocument) {
        //TODO: filter elements without statements?
        return !getBestStatements(itemDocument, P31).stream()
                .anyMatch(statement -> {
                    Value value = statement.getValue();
                    return value instanceof ItemIdValue && WikidataTypes.isFilteredType((ItemIdValue) statement.getValue());
                });
    }

    public void processPropertyDocument(PropertyDocument propertyDocument) {
        Document document = createDocumentForEntity(propertyDocument);
        addTermsToDocument(propertyDocument, document);
        document.add(new StringField("@type", "Property", Field.Store.YES));
        for (String range : WikidataTypes.getRangeForDatatype(propertyDocument.getDatatype())) {
            document.add(new StoredField("rangeIncludes", range));
        }
        writeDocument(document);
    }

    private Document createDocumentForEntity(EntityDocument entityDocument) {
        Document document = new Document();
        document.add(new StringField("@id", DataValueEncoder.encode(entityDocument.getEntityId()), Field.Store.YES));
        return document;
    }

    private void addTermsToDocument(TermedDocument termedDocument, Document document) {
        termedDocument.getLabels().values().forEach(label -> {
            document.add(toLabelField(label));
            document.add(toStoredField("name", label));
        });
        termedDocument.getDescriptions().values().forEach(description ->
                document.add(toStoredField("description", description))
        );
        termedDocument.getAliases().values().forEach(aliases ->
                aliases.forEach(alias -> {
                    document.add(toLabelField(alias));
                    document.add(toStoredField("alternateName", alias));
                })
        );
    }

    private void addSiteLinksToDocument(ItemDocument itemDocument, Document document) {
        for (SiteLink siteLink : itemDocument.getSiteLinks().values()) {
            document.add(new StoredField("sameAs", sites.getSiteLinkUrl(siteLink).replace("https://", "http://")));
        }
    }

    private void addStatementsToDocument(StatementDocument statementDocument, Document document) {
        statementDocument.getStatementGroups().forEach(group ->
                getBestStatements(group).forEach(statement ->
                        MapperRegistry.getMapperForProperty(statement.getClaim().getMainSnak().getPropertyId()).ifPresent(mapper -> {
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

    private StringField toLabelField(MonolingualTextValue value) {
        return new StringField(
                "label@" + WikimediaLanguageCodes.getLanguageCode(value.getLanguageCode()),
                value.getText(),
                Field.Store.NO
        );
    }

    private StoredField toStoredField(String name, MonolingualTextValue value) {
        return new StoredField(
                name + "@" + WikimediaLanguageCodes.getLanguageCode(value.getLanguageCode()),
                value.getText()
        );
    }

    private void writeDocument(Document document) {
        try {
            this.index.putDocument(document);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
