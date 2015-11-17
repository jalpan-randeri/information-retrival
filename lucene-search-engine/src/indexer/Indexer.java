package indexer;


import handler.DocumentHandler;
import handler.HtmlHandler;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Queue;

/**
 * Indexer will index the document which are retrieved by crawler
 */
public class Indexer {
    private Queue<Path> queue;
    private IndexWriter writer;


    public Indexer(Queue<Path> queue, String indexPath, Analyzer analyzer) throws IOException {
        this.queue = queue;
        FSDirectory dir = FSDirectory.open(new File(indexPath));
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47, analyzer);
        writer = new IndexWriter(dir, config);
    }


    public void index() throws IOException {
        DocumentHandler docHandler = new HtmlHandler();
        long initialDoc = writer.numDocs();
        while(!queue.isEmpty()){
            Path file = queue.remove();
            Document doc = docHandler.getDocument(file);
            writer.addDocument(doc);
        }
        long totalDocs = writer.numDocs() - initialDoc;
        System.out.printf("Total %d docs added%n", totalDocs);

        writer.close();
    }

}
