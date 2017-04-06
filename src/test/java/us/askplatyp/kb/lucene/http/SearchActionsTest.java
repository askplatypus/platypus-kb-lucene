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

package us.askplatyp.kb.lucene.http;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Test;
import us.askplatyp.kb.lucene.lucene.LuceneIndex;
import us.askplatyp.kb.lucene.model.Collection;
import us.askplatyp.kb.lucene.model.Entity;
import us.askplatyp.kb.lucene.model.EntitySearchResult;
import us.askplatyp.kb.lucene.model.JsonLdRoot;
import us.askplatyp.kb.lucene.wikidata.FakeWikidataLuceneIndexFactory;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

public class SearchActionsTest extends JerseyTest {

    private static final GenericType<JsonLdRoot<Collection<EntitySearchResult<Entity>>>> RESULT_TYPE =
            new GenericType<JsonLdRoot<Collection<EntitySearchResult<Entity>>>>() {
            };

    @Override
    protected Application configure() {
        return new ResourceConfig(SearchActions.class)
                .register(new AbstractBinder() {
                    @Override
                    protected void configure() {
                        bindFactory(FakeWikidataLuceneIndexFactory.class).to(LuceneIndex.class);
                    }
                });
    }

    @Test
    public void testDefault() {
        JsonLdRoot<Collection<EntitySearchResult<Entity>>> result =
                target("/api/v1/search/simple").request().get(RESULT_TYPE);
        Assert.assertEquals(Locale.ENGLISH, result.getContext().getLocale());
        assertElementCount(result.getContent(), 3);
        assertEnglishIndividual(result.getContent().getElements().get(0).getResult());
        assertEnglishSmallFoo(result.getContent().getElements().get(1).getResult());
        assertEnglishDummy(result.getContent().getElements().get(2).getResult());

    }

    @Test
    public void testKeywordSearch() {
        JsonLdRoot<Collection<EntitySearchResult<Entity>>> result =
                target("/api/v1/search/simple").queryParam("q", "Foo Bar").request().get(RESULT_TYPE);
        Assert.assertEquals(Locale.ENGLISH, result.getContext().getLocale());
        assertElementCount(result.getContent(), 2);
        assertEnglishIndividual(result.getContent().getElements().get(0).getResult());
        assertEnglishSmallFoo(result.getContent().getElements().get(1).getResult());
    }

    @Test
    public void testKeywordSearchFuzzyAndCaseInsensitive() {
        JsonLdRoot<Collection<EntitySearchResult<Entity>>> result =
                target("/api/v1/search/simple").queryParam("q", "fooo barr").request().get(RESULT_TYPE);
        Assert.assertEquals(Locale.ENGLISH, result.getContext().getLocale());
        assertElementCount(result.getContent(), 2);
        assertEnglishIndividual(result.getContent().getElements().get(0).getResult());
        assertEnglishSmallFoo(result.getContent().getElements().get(1).getResult());
    }

    @Test
    public void testKeywordAndTypeSearch() {
        JsonLdRoot<Collection<EntitySearchResult<Entity>>> result =
                target("/api/v1/search/simple").queryParam("q", "Foo Bar").queryParam("type", "Person").request().get(RESULT_TYPE);
        Assert.assertEquals(Locale.ENGLISH, result.getContext().getLocale());
        assertElementCount(result.getContent(), 1);
        assertEnglishIndividual(result.getContent().getElements().get(0).getResult());
    }

    @Test
    public void testTypeSearch() {
        JsonLdRoot<Collection<EntitySearchResult<Entity>>> result =
                target("/api/v1/search/simple").queryParam("type", "Person").request().get(RESULT_TYPE);
        Assert.assertEquals(Locale.ENGLISH, result.getContext().getLocale());
        assertElementCount(result.getContent(), 1);
        assertEnglishIndividual(result.getContent().getElements().get(0).getResult());
    }

    @Test
    public void testFullTypeIRISearch() {
        JsonLdRoot<Collection<EntitySearchResult<Entity>>> result =
                target("/api/v1/search/simple").queryParam("type", "http://schema.org/Person").request().get(RESULT_TYPE);
        Assert.assertEquals(Locale.ENGLISH, result.getContext().getLocale());
        assertElementCount(result.getContent(), 1);
        assertEnglishIndividual(result.getContent().getElements().get(0).getResult());
    }

    @Test
    public void testKeywordAndLanguageSearch() {
        JsonLdRoot<Collection<EntitySearchResult<Entity>>> result =
                target("/api/v1/search/simple").queryParam("q", "super de test").queryParam("lang", "fr-FR").request().get(RESULT_TYPE);
        Assert.assertEquals(Locale.ENGLISH, result.getContext().getLocale());
        assertElementCount(result.getContent(), 1);
        assertEnglishIndividual(result.getContent().getElements().get(0).getResult());
    }

    @Test
    public void testKeywordSearchWithContentNegotiation() {
        JsonLdRoot<Collection<EntitySearchResult<Entity>>> result =
                target("/api/v1/search/simple").queryParam("q", "Foo Bar").request().acceptLanguage(Locale.FRANCE).get(RESULT_TYPE);
        Assert.assertEquals(Locale.FRANCE, result.getContext().getLocale());
        assertElementCount(result.getContent(), 2);
        assertFrenchIndividual(result.getContent().getElements().get(0).getResult());
        assertFrenchSmallFoo(result.getContent().getElements().get(1).getResult());
    }

    @Test
    public void testPropertySearch() {
        JsonLdRoot<Collection<EntitySearchResult<Entity>>> result =
                target("/api/v1/search/simple").queryParam("type", "Property").request().get(RESULT_TYPE);
        Assert.assertEquals(Locale.ENGLISH, result.getContext().getLocale());
        assertElementCount(result.getContent(), 1);
        assertEnglishProperty(result.getContent().getElements().get(0).getResult());
    }

    @Test
    public void testKeywordPropertySearch() {
        JsonLdRoot<Collection<EntitySearchResult<Entity>>> result =
                target("/api/v1/search/simple").queryParam("q", "Foo Bar").queryParam("type", "rdf:Property").request().get(RESULT_TYPE);
        Assert.assertEquals(Locale.ENGLISH, result.getContext().getLocale());
        assertElementCount(result.getContent(), 1);
        assertEnglishProperty(result.getContent().getElements().get(0).getResult());
    }

    private <T> void assertElementCount(Collection<T> collection, int count) {
        Assert.assertEquals(count, collection.getTotalNumber());
        Assert.assertEquals(count, collection.getElements().size());
    }

    private void assertEnglishIndividual(Entity result) {
        Assert.assertEquals("Foo bar", result.getPropertyValue("name"));
        Assert.assertNull(result.getPropertyValue("description"));
        assertNotLanguageBaseIndividual(result);
    }

    private void assertFrenchIndividual(Entity result) {
        Assert.assertEquals("super de test", result.getPropertyValue("name"));
        Assert.assertEquals("Un test", result.getPropertyValue("description"));
        assertNotLanguageBaseIndividual(result);
    }

    private void assertNotLanguageBaseIndividual(Entity result) {
        Assert.assertEquals("wd:Q42", result.getIRI());
        Assert.assertEquals(Arrays.asList("NamedIndividual", "Person"), result.getTypes());
        Assert.assertEquals("http://foobar.com/", result.getPropertyValue("url"));
        Assert.assertEquals(Arrays.asList(
                "http://fr.wikipedia.org/wiki/Douglas_Adams",
                "http://twitter.com/BarackObama",
                "http://www.instagram.com/barackobama",
                "http://www.facebook.com/barackobama",
                "http://www.youtube.com/channel/UCdn86UYrf54lXfVli9CB6Aw",
                "http://plus.google.com/+BarackObama"
        ), result.getPropertyValue("sameAs"));
    }

    private void assertEnglishDummy(Entity result) {
        Assert.assertEquals("dummy", result.getPropertyValue("name"));
        Assert.assertNull(result.getPropertyValue("description"));
        assertNotLanguageBaseDummy(result);
    }

    private void assertNotLanguageBaseDummy(Entity result) {
        Assert.assertEquals("wd:Q111", result.getIRI());
    }

    private void assertEnglishSmallFoo(Entity result) {
        Assert.assertEquals("Foo bar", result.getPropertyValue("name"));
        Assert.assertNull(result.getPropertyValue("description"));
        assertNotLanguageBaseSmallFoo(result);
    }

    private void assertFrenchSmallFoo(Entity result) {
        Assert.assertNull(result.getPropertyValue("name"));
        Assert.assertNull(result.getPropertyValue("description"));
        assertNotLanguageBaseSmallFoo(result);
    }

    private void assertNotLanguageBaseSmallFoo(Entity result) {
        Assert.assertEquals("wd:Q222", result.getIRI());
        Assert.assertEquals(Collections.singletonList("NamedIndividual"), result.getTypes());
        Assert.assertNull(result.getPropertyValue("url"));
        Assert.assertEquals(Collections.emptyList(), result.getPropertyValue("sameAs"));
    }

    private void assertEnglishProperty(Entity result) {
        Assert.assertEquals("Foo-Bar", result.getPropertyValue("name"));
        Assert.assertNull(result.getPropertyValue("description"));
        assertNotLanguageBaseProperty(result);
    }

    private void assertNotLanguageBaseProperty(Entity result) {
        Assert.assertEquals("wdt:P42", result.getIRI());
        Assert.assertEquals(Arrays.asList("Property", "DatatypeProperty"), result.getTypes());
        Assert.assertEquals("xsd:string", result.getPropertyValue("range"));
        Assert.assertNull(result.getPropertyValue("url"));
        Assert.assertNull(result.getPropertyValue("sameAs"));
    }
}
