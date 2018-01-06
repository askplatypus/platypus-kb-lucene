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

package us.askplatyp.kb.lucene.blazegraph;

import com.bigdata.journal.BufferMode;
import com.bigdata.rdf.axioms.NoAxioms;
import com.bigdata.rdf.internal.InlineURIFactory;
import com.bigdata.rdf.internal.InlineUnsignedIntegerURIHandler;
import com.bigdata.rdf.sail.BigdataSail;
import com.bigdata.rdf.sail.BigdataSailRepository;
import com.bigdata.rdf.vocab.BaseVocabularyDecl;
import com.bigdata.rdf.vocab.core.BigdataCoreVocabulary_v20160317;
import org.openrdf.repository.RepositoryException;

import java.util.Properties;

public class SailFactory {

    public static BigdataSailRepository openNoInferenceTripleRepository(String file) throws RepositoryException {
        Properties properties = new Properties();
        properties.put(BigdataSail.Options.INITIAL_EXTENT, "209715200");
        properties.put(BigdataSail.Options.MAXIMUM_EXTENT, "209715200");
        properties.put(BigdataSail.Options.VOCABULARY_CLASS, PlatypusVocabulary_v1.class);
        properties.put(BigdataSail.Options.INLINE_URI_FACTORY_CLASS, PlatypusInlineURIFactory.class);
        properties.put(BigdataSail.Options.FILE, file);
        properties.put(BigdataSail.Options.QUADS, "false");
        properties.put(BigdataSail.Options.TRUTH_MAINTENANCE, "false");
        properties.put(BigdataSail.Options.JUSTIFY, "false");
        properties.put(BigdataSail.Options.TEXT_INDEX, "false");
        properties.put(BigdataSail.Options.AXIOMS_CLASS, NoAxioms.class);
        properties.put(BigdataSail.Options.STATEMENT_IDENTIFIERS, "false");
        properties.put(BigdataSail.Options.BUFFER_MODE, BufferMode.DiskRW);


        BigdataSail sail = new BigdataSail(properties);
        BigdataSailRepository repository = new BigdataSailRepository(sail);
        repository.initialize();
        return repository;
    }

    static class PlatypusVocabulary_v1 extends BigdataCoreVocabulary_v20160317 {
        public PlatypusVocabulary_v1() {
        }

        public PlatypusVocabulary_v1(String namespace) {
            super(namespace);
        }

        protected void addValues() {
            addDecl(new WikibaseVocabularyDecl());
            super.addValues();
        }
    }

    static class WikibaseVocabularyDecl extends BaseVocabularyDecl {
        WikibaseVocabularyDecl() {
            super(
                    "http://www.wikidata.org/entity/Q",
                    "http://www.wikidata.org/entity/P",
                    "http://www.wikidata.org/prop/direct/P"
            );
        }
    }

    static class PlatypusInlineURIFactory extends InlineURIFactory {
        public PlatypusInlineURIFactory() {
            addHandler(new InlineUnsignedIntegerURIHandler("http://www.wikidata.org/entity/Q"));
            addHandler(new InlineUnsignedIntegerURIHandler("http://www.wikidata.org/entity/P"));
            addHandler(new InlineUnsignedIntegerURIHandler("http://www.wikidata.org/prop/direct/P"));
        }
    }
}
