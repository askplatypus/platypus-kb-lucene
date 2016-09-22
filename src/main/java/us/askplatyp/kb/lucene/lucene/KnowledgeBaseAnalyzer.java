package us.askplatyp.kb.lucene.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.DelegatingAnalyzerWrapper;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author Thomas Pellissier Tanon
 */
class KnowledgeBaseAnalyzer extends DelegatingAnalyzerWrapper {

    private static Map<String, Analyzer> LUCENE_ANALYZERS = new TreeMap<>();
    private static Analyzer DEFAULT_ANALYZER = new SimpleAnalyzer();

    static {
        LUCENE_ANALYZERS.put("en", new EnglishAnalyzer());
        LUCENE_ANALYZERS.put("fr", new FrenchAnalyzer());
    }

    KnowledgeBaseAnalyzer() {
        super(PER_FIELD_REUSE_STRATEGY);
    }

    protected Analyzer getWrappedAnalyzer(String fieldName) {
        if (fieldName == null) {
            return DEFAULT_ANALYZER;
        }

        for (Map.Entry<String, Analyzer> entry : LUCENE_ANALYZERS.entrySet()) {
            if (fieldName.endsWith("@" + entry.getKey())) {
                return entry.getValue();
            }
        }
        return DEFAULT_ANALYZER;
    }
}
