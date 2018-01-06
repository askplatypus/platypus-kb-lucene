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

package us.askplatyp.kb.lucene.http;

import com.google.common.collect.Sets;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.resultio.TupleQueryResultParser;
import org.openrdf.query.resultio.helpers.QueryResultCollector;
import org.openrdf.query.resultio.sparqljson.SPARQLResultsJSONParserFactory;
import us.askplatyp.kb.lucene.CompositeIndex;
import us.askplatyp.kb.lucene.wikidata.FakeWikidataLuceneIndexFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SPARQLActionsTest extends JerseyTest {

    private static final ValueFactory VALUE_FACTORY = ValueFactoryImpl.getInstance();

    @Override
    protected Application configure() {
        return new ResourceConfig(SPARQLActions.class)
                .register(new AbstractBinder() {
                    @Override
                    protected void configure() {
                        bindFactory(FakeWikidataLuceneIndexFactory.class).to(CompositeIndex.class);
                    }
                });
    }

    @Test
    public void testGetPeople() throws Exception {
        Assert.assertEquals(
                Collections.singleton(VALUE_FACTORY.createURI("http://www.wikidata.org/entity/Q42")),
                doSparqlQuerySingleSelect("SELECT DISTINCT ?s WHERE { ?s <http://www.wikidata.org/prop/direct/P31>/<http://www.wikidata.org/prop/direct/P279>* <http://www.wikidata.org/entity/Q5> }")
        );
    }

    @Test
    public void testGetByGooglePlus() throws Exception {
        Assert.assertEquals(
                Collections.singleton(VALUE_FACTORY.createURI("http://www.wikidata.org/entity/Q42")),
                doSparqlQuerySingleSelect("SELECT DISTINCT ?s WHERE { ?s <http://www.wikidata.org/prop/direct/P2847> \"+BarackObama\" }")
        );
    }

    @Test
    public void testGetValues() throws Exception {
        Assert.assertEquals(
                Sets.newHashSet(
                        VALUE_FACTORY.createURI("http://www.wikidata.org/entity/Q515"),
                        VALUE_FACTORY.createURI("geo:48.85657777777817,2.3518277777777965")
                ),
                doSparqlQuerySingleSelect("SELECT DISTINCT ?o WHERE { <http://www.wikidata.org/entity/Q90> ?p ?o }")
        );
    }

    /*@Test
    public void testGetSubject() throws Exception {
        Assert.assertEquals(
                Sets.newHashSet(
                        VALUE_FACTORY.createURI("http://www.wikidata.org/entity/Q42"),
                        VALUE_FACTORY.createURI("http://www.wikidata.org/entity/Q222"),
                        VALUE_FACTORY.createURI("http://www.wikidata.org/entity/Q111"),
                        VALUE_FACTORY.createURI("http://www.wikidata.org/entity/Q91"),
                        VALUE_FACTORY.createURI("http://www.wikidata.org/entity/Q90"),
                        VALUE_FACTORY.createURI("http://www.wikidata.org/entity/Q2108")
                ),
                doSparqlQuerySingleSelect("SELECT DISTINCT ?s WHERE { ?s ?p ?o }")
        );
    }

    @Test
    public void testGetPeople() throws IOException {
        Assert.assertEquals(
                Sets.newHashSet(
                        VALUE_FACTORY.createURI("http://www.wikidata.org/entity/Q42")
                ),
                doSparqlQuerySingleSelect("SELECT DISTINCT ?s WHERE { ?s a schema:Person }")
        );
    }

    @Test
    public void testGetByBirthDate() throws IOException {
        Assert.assertEquals(
                Sets.newHashSet(
                        VALUE_FACTORY.createURI("http://www.wikidata.org/entity/Q42")
                ),
                doSparqlQuerySingleSelect("SELECT DISTINCT ?s WHERE { ?s schema:birthDate \"1952-03-11Z\"^^xsd:date }")
        );
    }

    @Test
    public void testGetBySameAs() throws IOException {
        Assert.assertEquals(
                Sets.newHashSet(
                        VALUE_FACTORY.createURI("http://www.wikidata.org/entity/Q42")
                ),
                doSparqlQuerySingleSelect("SELECT DISTINCT ?s WHERE { ?s schema:sameAs \"http://fr.wikipedia.org/wiki/Douglas_Adams\" }")
        );
    }

    @Test
    public void testGetByName() throws IOException {
        Assert.assertEquals(
                Sets.newHashSet(
                        VALUE_FACTORY.createURI("http://www.wikidata.org/entity/Q42")
                ),
                doSparqlQuerySingleSelect("SELECT DISTINCT ?s WHERE { ?s schema:name \"super de test\"@fr }")
        );
    }

    @Test
    public void testGetByNameAndType() throws IOException {
        Assert.assertEquals(
                Sets.newHashSet(
                        VALUE_FACTORY.createURI("http://www.wikidata.org/entity/Q42")
                ),
                doSparqlQuerySingleSelect("SELECT DISTINCT ?s WHERE { ?s schema:name \"Foo bar\"@en ; a schema:Person }")
        );
    }

    @Test
    public void testGetName() throws IOException {
        Assert.assertEquals(
                Sets.newHashSet(
                        VALUE_FACTORY.createLiteral("Foo bar", "en")
                ),
                doSparqlQuerySingleSelect("SELECT DISTINCT ?label WHERE { wd:Q222 schema:name ?label }")
        );
    }

    /* TODO
    @Test
    public void testGetAllDescriptions() throws IOException {
        Assert.assertEquals(
                Sets.newHashSet(
                        VALUE_FACTORY.createLiteral("Un test", "fr")
                ),
                doSparqlQuerySingleSelect("SELECT DISTINCT ?desc WHERE { ?foo schema:description ?desc }")
        );
    }*/

    private Set<Value> doSparqlQuerySingleSelect(String query) throws Exception {
        return doSparqlQuery(query).stream()
                .map(bindings -> bindings.getValue(bindings.getBindingNames().iterator().next()))
                .collect(Collectors.toSet());
    }

    private List<BindingSet> doSparqlQuery(String query) throws Exception {
        InputStream result = target("/api/v1/sparql")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(query, "application/sparql-query"), InputStream.class);
        TupleQueryResultParser parser = (new SPARQLResultsJSONParserFactory()).getParser();
        QueryResultCollector collector = new QueryResultCollector();
        parser.setQueryResultHandler(collector);
        parser.parseQueryResult(result);
        return collector.getBindingSets();
    }

    @Test
    public void testBadRequest() {
        Response response = target("/api/v1/sparql")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity("foo", "application/sparql-query"));
        Assert.assertEquals(400, response.getStatus());
    }
}
