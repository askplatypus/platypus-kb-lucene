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

import org.apache.lucene.document.*;
import org.apache.lucene.index.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.askplatyp.kb.lucene.model.IndexableResource;
import us.askplatyp.kb.lucene.model.StorageLoader;
import us.askplatyp.kb.lucene.model.value.LocaleStringValue;
import us.askplatyp.kb.lucene.model.value.Value;

import java.io.IOException;

public class LuceneLoader implements StorageLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(LuceneLoader.class);

    private LuceneIndex index;

    public LuceneLoader(LuceneIndex index) {
        this.index = index;
    }

    @Override
    public void addResource(IndexableResource resource) {
        Document document = new Document();

        document.add(new StringField("@id", resource.getIRI(), Field.Store.YES));

        resource.getTypes().forEach(typeIRI -> document.add(new StringField("@type", typeIRI, Field.Store.YES)));

        resource.getLabels().forEach(label -> document.add(new StringField(
                "label@" + label.getLocale().getLanguage(), //TODO: variants
                label.toString().toLowerCase(label.getLocale()),
                Field.Store.NO
        )));

        resource.getClaims().forEach(claim -> {
            Value value = claim.getValue();
            if (value instanceof LocaleStringValue) {
                document.add(new StoredField(
                        claim.getProperty() + "@" + ((LocaleStringValue) value).getLocale().getLanguage(),
                        value.toString()
                ));
            } else {
                document.add(new StoredField(claim.getProperty(), value.toString()));
            }
        });

        document.add(new NumericDocValuesField("score", resource.getRank()));

        writeDocument(document);
    }

    private void writeDocument(Document document) {
        try {
            this.index.putDocument(document, new Term("@id", document.get("@id")));
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
