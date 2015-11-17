package searcher;

import handler.DocumentHandler;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.util.Version;

import java.io.IOException;

/**
 *
 */
public class Searcher {

    private IndexReader reader;
    private Analyzer analyzer;
    private IndexSearcher searcher;
    private TopScoreDocCollector collector;
    private int limit;

    public Searcher(IndexReader reader, Analyzer analyzer, int limit) {
        this.reader = reader;
        this.analyzer = analyzer;
        this.limit = limit;
        searcher = new IndexSearcher(reader);
        collector = TopScoreDocCollector.create(limit, true);
    }

    public ScoreDoc[] search(String query) throws ParseException, IOException {
        Query  q = new QueryParser(Version.LUCENE_47, DocumentHandler.FIELD_CONTENTS, analyzer).parse(query);
        searcher.search(q, collector);
        return collector.topDocs().scoreDocs;
    }
}
