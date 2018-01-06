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

import com.google.common.collect.Sets;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.interfaces.*;
import us.askplatyp.kb.lucene.model.Claim;
import us.askplatyp.kb.lucene.model.IndexableResource;
import us.askplatyp.kb.lucene.model.StorageLoader;
import us.askplatyp.kb.lucene.model.value.LocaleStringValue;
import us.askplatyp.kb.lucene.wikidata.mapping.InvalidWikibaseValueException;
import us.askplatyp.kb.lucene.wikidata.mapping.MapperRegistry;
import us.askplatyp.kb.lucene.wikidata.mapping.TypeMapper;

import java.util.*;

/**
 * @author Thomas Pellissier Tanon
 */
public class WikidataResourceProcessor implements EntityDocumentProcessor {

    public static final Set<String> SUPPORTED_LANGUAGES = Sets.newHashSet(
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
    private static final Logger LOGGER = LoggerFactory.getLogger(WikidataResourceProcessor.class);
    private static final PropertyIdValue P31 = Datamodel.makeWikidataPropertyIdValue("P31");

    private StorageLoader loader;
    private Sites sites;
    private TypeMapper typeMapper;
    private MapperRegistry mapperRegistry;
    private RepositoryConnection repositoryConnection;

    public WikidataResourceProcessor(StorageLoader loader, Sites sites, WikidataTypeHierarchy typeHierarchy, RepositoryConnection repositoryConnection) {
        this.loader = loader;
        this.sites = sites;
        this.typeMapper = new TypeMapper(typeHierarchy);
        this.mapperRegistry = new MapperRegistry(typeHierarchy);
        this.repositoryConnection = repositoryConnection;
    }

    @Override
    public void processItemDocument(ItemDocument itemDocument) {
        if (!isGoodItem(itemDocument)) {
            return;
        }

        IndexableResource resource = new IndexableResource(itemDocument.getEntityId().getIri());
        addTermsToResource(itemDocument, resource);
        addSiteLinksToResource(itemDocument, resource);
        addStatementsToResource(itemDocument, resource);
        addScoreToResource(itemDocument, resource);
        loader.addResource(resource);
        addToRepository(itemDocument);
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

    private void addTermsToResource(TermedDocument termedDocument, IndexableResource resource) {
        termedDocument.getLabels().values().forEach(label -> {
            LocaleStringValue value = convert(label);
            resource.addClaim("name", value);
            resource.addLabel(value);

        });
        termedDocument.getDescriptions().values().forEach(description ->
                resource.addClaim("description", convert(description))
        );
        termedDocument.getAliases().values().forEach(aliases ->
                aliases.forEach(alias -> {
                    LocaleStringValue value = convert(alias);
                    resource.addClaim("alternateName", value);
                    resource.addLabel(value);
                })
        );
    }

    private void addSiteLinksToResource(ItemDocument itemDocument, IndexableResource resource) {
        itemDocument.getSiteLinks().values().stream()
                .filter(siteLink ->
                        sites.getGroup(siteLink.getSiteKey()).equals("wikipedia") &&
                                SUPPORTED_LANGUAGES.contains(sites.getLanguageCode(siteLink.getSiteKey()))
                )
                .forEach(siteLink ->
                        resource.addClaim(new Claim("sameAs", sites.getSiteLinkUrl(siteLink).replace("https://", "http://")))
                );
    }

    private void addStatementsToResource(StatementDocument statementDocument, IndexableResource resource) {
        statementDocument.getStatementGroups().forEach(group ->
                getBestStatements(group).forEach(statement ->
                        mapperRegistry.getMapperForProperty(statement.getClaim().getMainSnak().getPropertyId()).ifPresent(mapper -> {
                            try {
                                mapper.mapStatement(statement).forEach(resource::addClaim);
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

    private LocaleStringValue convert(MonolingualTextValue value) {
        return new LocaleStringValue(value.getText(), WikimediaLanguageCodes.getLanguageCode(value.getLanguageCode()));
    }

    private void addScoreToResource(ItemDocument itemDocument, IndexableResource resource) {
        resource.addToRank(itemDocument.getSiteLinks().size());
    }

    private void addToRepository(StatementDocument document) {
        Model newStatements = new TreeModel();

        ValueFactory valueFactory = repositoryConnection.getValueFactory();
        URI entityURI = valueFactory.createURI(document.getEntityId().getIri());
        document.getStatementGroups().forEach(statementGroup -> {
            URI propertyURI = valueFactory.createURI("http://www.wikidata.org/prop/direct/", statementGroup.getProperty().getId());
            getBestStatements(statementGroup).forEach(statement -> {
                Object value = statement.getValue();
                if (value instanceof IriIdentifiedValue) {
                    newStatements.add(
                            entityURI,
                            propertyURI,
                            valueFactory.createURI(((IriIdentifiedValue) value).getIri())
                    );
                } else if (value instanceof StringValue) {
                    newStatements.add(
                            entityURI,
                            propertyURI,
                            valueFactory.createLiteral(((StringValue) value).getString())
                    );
                } else if (value instanceof MonolingualTextValue) {
                    newStatements.add(
                            entityURI,
                            propertyURI,
                            valueFactory.createLiteral(((MonolingualTextValue) value).getText(), ((MonolingualTextValue) value).getLanguageCode())
                    );
                } else if (value instanceof GlobeCoordinatesValue) {
                    WikibaseValueUtils.toGeoURI((GlobeCoordinatesValue) value).ifPresent(uri ->
                            newStatements.add(entityURI, propertyURI, valueFactory.createURI(uri))
                    );
                } else if (value instanceof TimeValue) {
                    WikibaseValueUtils.toXmlGregorianCalendar((TimeValue) value).ifPresent(literal ->
                            newStatements.add(entityURI, propertyURI, valueFactory.createLiteral(literal))
                    );
                } else if (value instanceof QuantityValue) {
                    QuantityValue quantity = (QuantityValue) value;
                    if (quantity.getUnit().isEmpty() && Objects.equals(quantity.getLowerBound(), quantity.getUpperBound())) {
                        newStatements.add(entityURI, propertyURI, valueFactory.createLiteral(quantity.getNumericValue().toPlainString()));
                    }
                    //TODO: support more quantities
                } else if (value != null) {
                    LOGGER.warn("Unsupported value type: " + value);
                }
            });
        });

        //TODO: do batches and use named graphs
        if (!newStatements.isEmpty()) {
            try {
                repositoryConnection.begin();
                repositoryConnection.remove(entityURI, null, null);
                repositoryConnection.add(newStatements);
                repositoryConnection.commit();
            } catch (RepositoryException e) {
                LOGGER.warn(e.getMessage(), e);
            }
        }

    }
}
