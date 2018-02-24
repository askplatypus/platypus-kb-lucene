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

package us.askplatyp.kb.lucene.lucene;

import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.askplatyp.kb.lucene.Configuration;
import us.askplatyp.kb.lucene.model.Claim;
import us.askplatyp.kb.lucene.model.Resource;
import us.askplatyp.kb.lucene.model.Schema;
import us.askplatyp.kb.lucene.model.value.*;

import java.util.Locale;

class LuceneResourceBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(LuceneResourceBuilder.class);
    private static final Schema SCHEMA = Schema.getSchema();

    Resource buildResource(Document document) {
        Resource resource = new Resource(document.get("@id"));
        addTypes(resource, document);
        addClaims(resource, document);
        return resource;
    }

    private void addTypes(Resource resource, Document document) {
        for (String typeIRI : document.getValues("@type")) {
            resource.addType(typeIRI);
        }
    }

    private void addClaims(Resource resource, Document document) {
        SCHEMA.getProperties().forEach(property -> {
            switch (property.getSimpleRange()) {
                case CALENDAR:
                    for (String value : document.getValues(property.getIRI())) {
                        resource.addClaim(new Claim(property.getIRI(), new CalendarValue(value)));
                    }
                    break;
                case CONSTANT:
                    for (String value : document.getValues(property.getIRI())) {
                        resource.addClaim(new Claim(property.getIRI(), new ConstantValue(value)));
                    }
                    break;
                case GEO:
                    for (String value : document.getValues(property.getIRI())) {
                        resource.addClaim(new Claim(property.getIRI(), GeoValue.buildGeoValue(value)));
                    }
                    break;
                case INTEGER:
                    for (String value : document.getValues(property.getIRI())) {
                        resource.addClaim(new Claim(property.getIRI(), new IntegerValue(value)));
                    }
                    break;
                case LOCAL_STRING:
                    for (Locale locale : Configuration.SUPPORTED_LOCALES) { //TODO: bad
                        for (String value : document.getValues(property.getIRI() + "@" + locale.getLanguage())) {
                            resource.addClaim(new Claim(property.getIRI(), value, locale));
                        }
                    }
                    break;
                case RESOURCE:
                    for (String value : document.getValues(property.getIRI())) {
                        resource.addClaim(new Claim(property.getIRI(), new ResourceValue(value)));
                    }
                    break;
                case STRING:
                case IRI:
                    for (String value : document.getValues(property.getIRI())) {
                        resource.addClaim(new Claim(property.getIRI(), value));
                    }
                    break;
                default:
                    LOGGER.warn("Unsupported simple range type: " + property.getSimpleRange().toString());

            }
        });
    }
}
