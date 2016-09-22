package us.askplatyp.kb.lucene.http;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Test;
import us.askplatyp.kb.lucene.FakeWikidataLuceneIndexFactory;
import us.askplatyp.kb.lucene.lucene.LuceneIndex;
import us.askplatyp.kb.lucene.model.Entity;
import us.askplatyp.kb.lucene.model.JsonLdRoot;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import java.util.Locale;

public class EntityActionsTest extends JerseyTest {

    private static final GenericType<JsonLdRoot<Entity>> RESULT_TYPE = new GenericType<JsonLdRoot<Entity>>() {
    };

    @Override
    protected Application configure() {
        return new ResourceConfig(EntityActions.class)
                .register(new AbstractBinder() {
                    @Override
                    protected void configure() {
                        bindFactory(FakeWikidataLuceneIndexFactory.class).to(LuceneIndex.class);
                    }
                });
    }

    @Test
    public void testDefaultShortIRI() {
        JsonLdRoot<Entity> result =
                target("/entity/wd:Q42").request().get(RESULT_TYPE);
        Assert.assertEquals(Locale.ENGLISH, result.getContext().getLocale());
        assertEnglishIndividual(result.getContent());
    }

    @Test
    public void testDefaultFullIRI() {
        JsonLdRoot<Entity> result =
                target("/entity/http%3A%2F%2Fwww.wikidata.org%2Fentity%2FQ42").request().get(RESULT_TYPE);
        Assert.assertEquals(Locale.ENGLISH, result.getContext().getLocale());
        assertEnglishIndividual(result.getContent());
    }

    @Test
    public void testFrenchWithContentNegotiation() {
        JsonLdRoot<Entity> result =
                target("/entity/wd:Q42").request().acceptLanguage(Locale.FRANCE).get(RESULT_TYPE);
        Assert.assertEquals(Locale.FRANCE, result.getContext().getLocale());
        assertFrenchIndividual(result.getContent());
    }

    @Test
    public void testDummy() {
        JsonLdRoot<Entity> result =
                target("/entity/wd:Q111").request().get(RESULT_TYPE);
        Assert.assertEquals(Locale.ENGLISH, result.getContext().getLocale());
        assertEnglishDummy(result.getContent());
    }

    @Test
    public void testProperty() {
        JsonLdRoot<Entity> result =
                target("/entity/wd:P42").request().get(RESULT_TYPE);
        Assert.assertEquals(Locale.ENGLISH, result.getContext().getLocale());
        assertEnglishProperty(result.getContent());
    }

    private void assertEnglishIndividual(Entity result) {
        Assert.assertEquals(result.getName(), "Foo bar");
        Assert.assertNull(result.getDescription());
        Assert.assertNull(result.getDetailedDescription());
        assertNotLanguageBaseIndividual(result);
    }

    private void assertFrenchIndividual(Entity result) {
        Assert.assertEquals("super de test", result.getName());
        Assert.assertEquals("Un test", result.getDescription());
        Assert.assertEquals("http://fr.wikipedia.org/wiki/Douglas_Adams", result.getDetailedDescription().getIRI());
        Assert.assertEquals("fr", result.getDetailedDescription().getLanguageCode());
        Assert.assertEquals("Douglas Adams", result.getDetailedDescription().getTitle());
        Assert.assertEquals("http://creativecommons.org/licenses/by-sa/3.0/", result.getDetailedDescription().getLicenseIRI());
        Assert.assertNotNull(result.getImage());
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
