package us.askplatyp.kb.lucene.http;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Test;
import us.askplatyp.kb.lucene.FakeWikidataLuceneIndexFactory;
import us.askplatyp.kb.lucene.lucene.LuceneIndex;
import us.askplatyp.kb.lucene.model.Collection;
import us.askplatyp.kb.lucene.model.Entity;
import us.askplatyp.kb.lucene.model.EntitySearchResult;
import us.askplatyp.kb.lucene.model.JsonLdRoot;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
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
                target("/search/simple").request().get(RESULT_TYPE);
        Assert.assertEquals(Locale.ENGLISH, result.getContext().getLocale());
        assertElementCount(result.getContent(), 3);
        assertEnglishIndividual(result.getContent().getElements().get(0).getResult());
        assertEnglishSmallFoo(result.getContent().getElements().get(1).getResult());
        assertEnglishDummy(result.getContent().getElements().get(2).getResult());

    }

    @Test
    public void testKeywordSearch() {
        JsonLdRoot<Collection<EntitySearchResult<Entity>>> result =
                target("/search/simple").queryParam("q", "Foo Bar").request().get(RESULT_TYPE);
        Assert.assertEquals(Locale.ENGLISH, result.getContext().getLocale());
        assertElementCount(result.getContent(), 2);
        assertEnglishIndividual(result.getContent().getElements().get(0).getResult());
        assertEnglishSmallFoo(result.getContent().getElements().get(1).getResult());
    }

    @Test
    public void testKeywordAndTypeSearch() {
        JsonLdRoot<Collection<EntitySearchResult<Entity>>> result =
                target("/search/simple").queryParam("q", "Foo Bar").queryParam("type", "Person").request().get(RESULT_TYPE);
        Assert.assertEquals(Locale.ENGLISH, result.getContext().getLocale());
        assertElementCount(result.getContent(), 1);
        assertEnglishIndividual(result.getContent().getElements().get(0).getResult());
    }

    @Test
    public void testTypeSearch() {
        JsonLdRoot<Collection<EntitySearchResult<Entity>>> result =
                target("/search/simple").queryParam("type", "Person").request().get(RESULT_TYPE);
        Assert.assertEquals(Locale.ENGLISH, result.getContext().getLocale());
        assertElementCount(result.getContent(), 1);
        assertEnglishIndividual(result.getContent().getElements().get(0).getResult());
    }

    @Test
    public void testKeywordAndLanguageSearch() {
        JsonLdRoot<Collection<EntitySearchResult<Entity>>> result =
                target("/search/simple").queryParam("q", "super de test").queryParam("lang", "fr-FR").request().get(RESULT_TYPE);
        Assert.assertEquals(Locale.ENGLISH, result.getContext().getLocale());
        assertElementCount(result.getContent(), 1);
        assertEnglishIndividual(result.getContent().getElements().get(0).getResult());
    }

    @Test
    public void testKeywordSearchWithContentNegotiation() {
        JsonLdRoot<Collection<EntitySearchResult<Entity>>> result =
                target("/search/simple").queryParam("q", "Foo Bar").request().acceptLanguage(Locale.FRANCE).get(RESULT_TYPE);
        Assert.assertEquals(Locale.FRANCE, result.getContext().getLocale());
        assertElementCount(result.getContent(), 2);
        assertFrenchIndividual(result.getContent().getElements().get(0).getResult());
        assertFrenchSmallFoo(result.getContent().getElements().get(1).getResult());
    }

    @Test
    public void testPropertySearch() {
        JsonLdRoot<Collection<EntitySearchResult<Entity>>> result =
                target("/search/simple").queryParam("type", "Property").request().get(RESULT_TYPE);
        Assert.assertEquals(Locale.ENGLISH, result.getContext().getLocale());
        assertElementCount(result.getContent(), 1);
        assertEnglishProperty(result.getContent().getElements().get(0).getResult());
    }

    @Test
    public void testKeywordPropertySearch() {
        JsonLdRoot<Collection<EntitySearchResult<Entity>>> result =
                target("/search/simple").queryParam("q", "Foo Bar").queryParam("type", "Property").request().get(RESULT_TYPE);
        Assert.assertEquals(Locale.ENGLISH, result.getContext().getLocale());
        assertElementCount(result.getContent(), 1);
        assertEnglishProperty(result.getContent().getElements().get(0).getResult());
    }

    private <T> void assertElementCount(Collection<T> collection, int count) {
        Assert.assertEquals(count, collection.getTotalNumber());
        Assert.assertEquals(count, collection.getElements().size());
    }

    private void assertEnglishIndividual(Entity result) {
        Assert.assertEquals("Foo bar", result.getName());
        Assert.assertNull(result.getDescription());
        assertNotLanguageBaseIndividual(result);
    }

    private void assertFrenchIndividual(Entity result) {
        Assert.assertEquals("super de test", result.getName());
        Assert.assertEquals("Un test", result.getDescription());
        assertNotLanguageBaseIndividual(result);
    }

    private void assertNotLanguageBaseIndividual(Entity result) {
        Assert.assertEquals("wd:Q42", result.getIRI());
        Assert.assertArrayEquals(new String[]{"Thing", "Person"}, result.getTypes());
        Assert.assertEquals("http://foobar.com/", result.getOfficialWebsiteIRI());
        Assert.assertArrayEquals(new String[]{
                "http://fr.wikipedia.org/wiki/Douglas_Adams",
                "http://twitter.com/BarackObama",
                "http://www.instagram.com/barackobama",
                "http://www.facebook.com/barackobama",
                "http://www.youtube.com/channel/UCdn86UYrf54lXfVli9CB6Aw",
                "http://plus.google.com/+BarackObama"
        }, result.getSameAsIRIs());
    }

    private void assertEnglishDummy(Entity result) {
        Assert.assertEquals("dummy", result.getName());
        Assert.assertNull(result.getDescription());
        assertNotLanguageBaseDummy(result);
    }

    private void assertNotLanguageBaseDummy(Entity result) {
        Assert.assertEquals("wd:Q111", result.getIRI());
    }

    private void assertEnglishSmallFoo(Entity result) {
        Assert.assertEquals("Foo bar", result.getName());
        Assert.assertNull(result.getDescription());
        assertNotLanguageBaseSmallFoo(result);
    }

    private void assertFrenchSmallFoo(Entity result) {
        Assert.assertNull(result.getName());
        Assert.assertNull(result.getDescription());
        assertNotLanguageBaseSmallFoo(result);
    }

    private void assertNotLanguageBaseSmallFoo(Entity result) {
        Assert.assertEquals("wd:Q222", result.getIRI());
        Assert.assertArrayEquals(new String[]{"Thing"}, result.getTypes());
        Assert.assertNull(result.getOfficialWebsiteIRI());
        Assert.assertArrayEquals(new String[]{}, result.getSameAsIRIs());
    }

    private void assertEnglishProperty(Entity result) {
        Assert.assertEquals("Foo-Bar", result.getName());
        Assert.assertNull(result.getDescription());
        assertNotLanguageBaseProperty(result);
    }

    private void assertNotLanguageBaseProperty(Entity result) {
        Assert.assertEquals("wd:P42", result.getIRI());
        Assert.assertArrayEquals(new String[]{"Property"}, result.getTypes());
        Assert.assertArrayEquals(new String[]{"xsd:string"}, result.getRangeIncludes());
        Assert.assertNull(result.getOfficialWebsiteIRI());
        Assert.assertArrayEquals(new String[]{}, result.getSameAsIRIs());
    }
}
