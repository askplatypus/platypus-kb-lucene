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

import jersey.repackaged.com.google.common.collect.Sets;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Test;
import us.askplatyp.kb.lucene.jsonld.Entity;
import us.askplatyp.kb.lucene.jsonld.JsonLdRoot;
import us.askplatyp.kb.lucene.lucene.LuceneIndex;
import us.askplatyp.kb.lucene.wikidata.FakeWikidataLuceneIndexFactory;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

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
                target("/api/v1/entity/wd:Q42").request().get(RESULT_TYPE);
        assertEnglishIndividual(result.getContent());
        Assert.assertEquals(Optional.of("xsd:anyURI"), result.getContext().getRange("url"));
        Assert.assertEquals(Optional.of("xsd:anyURI"), result.getContext().getRange("sameAs"));
        Assert.assertEquals(Optional.of("@id"), result.getContext().getRange("gender"));
    }

    @Test
    public void testDefaultFullIRI() {
        JsonLdRoot<Entity> result =
                target("/api/v1/entity/http%3A%2F%2Fwww.wikidata.org%2Fentity%2FQ42").request().get(RESULT_TYPE);
        assertEnglishIndividual(result.getContent());
    }

    @Test
    public void testFrenchWithContentNegotiation() {
        JsonLdRoot<Entity> result =
                target("/api/v1/entity/wd:Q42").request().acceptLanguage(Locale.FRANCE).get(RESULT_TYPE);
        assertFrenchIndividual(result.getContent());
        Assert.assertEquals(Optional.of("xsd:anyURI"), result.getContext().getRange("url"));
        Assert.assertEquals(Optional.of("xsd:anyURI"), result.getContext().getRange("sameAs"));
        Assert.assertEquals(Optional.of("@id"), result.getContext().getRange("gender"));
    }

    @Test
    public void testDummy() {
        JsonLdRoot<Entity> result =
                target("/api/v1/entity/wd:Q111").request().get(RESULT_TYPE);
        assertEnglishDummy(result.getContent());
    }

    @Test
    public void testPlaceGeoShape() {
        JsonLdRoot<Entity> result =
                target("/api/v1/entity/wd:Q90").request().get(RESULT_TYPE);
        Assert.assertNotNull(result.getContent().getPropertyValue("geo"));
        Map geoValue = (Map) result.getContent().getPropertyValue("geo");
        Assert.assertEquals("GeoShape", geoValue.get("@type"));
        Assert.assertEquals(Optional.of("geo:wktLiteral"), result.getContext().getRange("geo:asWKT"));
    }

    @Test
    public void testPlaceGeoLine() {
        JsonLdRoot<Entity> result =
                target("/api/v1/entity/wd:Q2108").request().get(RESULT_TYPE);
        Assert.assertNotNull(result.getContent().getPropertyValue("geo"));
        Map geoValue = (Map) result.getContent().getPropertyValue("geo");
        Assert.assertEquals("GeoShape", geoValue.get("@type"));
    }

    @Test
    public void testPlaceCoordinates() {
        JsonLdRoot<Entity> result =
                target("/api/v1/entity/wd:Q91").request().get(RESULT_TYPE);
        Map geoValue = (Map) result.getContent().getPropertyValue("geo");
        Assert.assertEquals("GeoCoordinates", geoValue.get("@type"));
    }

    @Test
    public void test404() {
        Response response = target("/api/v1/entity/wd:Q01").request().get();
        Assert.assertEquals(404, response.getStatus());
    }

    @Test
    public void testUnsupportedContentType() {
        Response response = target("/api/v1/entity/wdt:Q01").request(MediaType.APPLICATION_XML_TYPE).get();
        Assert.assertEquals(406, response.getStatus());
    }

    private void assertEnglishIndividual(Entity result) {
        Assert.assertEquals(buildLanguageTaggedLiteral("Foo bar", "en"), result.getPropertyValue("name"));
        Assert.assertNull(result.getPropertyValue("description"));
        Assert.assertNull(result.getPropertyValue("detailedDescription"));
        assertNotLanguageBaseIndividual(result);
    }

    private void assertFrenchIndividual(Entity result) {
        Assert.assertEquals(buildLanguageTaggedLiteral("super de test", "fr-FR"), result.getPropertyValue("name"));
        Assert.assertEquals(buildLanguageTaggedLiteral("Un test", "fr-FR"), result.getPropertyValue("description"));
        Map detailedDescription = (Map) result.getPropertyValue("detailedDescription");
        Assert.assertEquals("http://fr.wikipedia.org/wiki/Douglas_Adams", detailedDescription.get("@id"));
        Assert.assertEquals("fr", detailedDescription.get("inLanguage"));
        Assert.assertEquals("Douglas Adams", detailedDescription.get("name"));
        Assert.assertEquals("http://creativecommons.org/licenses/by-sa/3.0/", detailedDescription.get("license"));
        Assert.assertNotNull(result.getPropertyValue("image"));
        assertNotLanguageBaseIndividual(result);
    }

    private void assertNotLanguageBaseIndividual(Entity result) {
        Assert.assertEquals("wd:Q42", result.getIRI());
        Assert.assertEquals(Collections.singletonList("Person"), result.getTypes());
        Assert.assertEquals("http://foobar.com/", result.getPropertyValue("url"));
        Assert.assertEquals(Sets.newHashSet(
                "http://fr.wikipedia.org/wiki/Douglas_Adams",
                "http://twitter.com/BarackObama",
                "http://www.instagram.com/barackobama",
                "http://www.facebook.com/barackobama",
                "http://www.youtube.com/channel/UCdn86UYrf54lXfVli9CB6Aw",
                "http://plus.google.com/+BarackObama"
        ), Sets.newHashSet((List<String>) result.getPropertyValue("sameAs")));
        Assert.assertEquals(builTypedLiteral("1952-03-11Z", "xsd:date"), result.getPropertyValue("birthDate"));
        Assert.assertEquals("Male", result.getPropertyValue("gender"));
    }

    private void assertEnglishDummy(Entity result) {
        Assert.assertEquals(buildLanguageTaggedLiteral("dummy", "en"), result.getPropertyValue("name"));
        Assert.assertNull(result.getPropertyValue("description"));
        assertNotLanguageBaseDummy(result);
    }

    private void assertNotLanguageBaseDummy(Entity result) {
        Assert.assertEquals("wd:Q111", result.getIRI());
        Assert.assertEquals(Collections.emptyList(), result.getTypes());
        Assert.assertNull(result.getPropertyValue("url"));
        Assert.assertNull(result.getPropertyValue("sameAs"));
    }

    private Map<String, Object> buildLanguageTaggedLiteral(String value, String language) {
        Map<String, Object> map = new TreeMap<>();
        map.put("@value", value);
        map.put("@language", language);
        return map;
    }

    private Map<String, Object> builTypedLiteral(String value, String type) {
        Map<String, Object> map = new TreeMap<>();
        map.put("@value", value);
        map.put("@type", type);
        return map;
    }
}
