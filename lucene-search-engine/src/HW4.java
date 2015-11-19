import analyzer.HtmlAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;
import org.jsoup.Jsoup;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * To create Apache Lucene index in a folder and add files into this index based
 * on the input of the user.
 */
public class HW4 {
    private static Analyzer sAnalyzer = new HtmlAnalyzer();

    private IndexWriter writer;
    private ArrayList<File> queue = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        System.out
                .println("Enter the FULL path where the index will be created: (e.g. /Usr/index or c:\\temp\\index)");

        String indexLocation = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String s = br.readLine();

        HW4 indexer = null;
        try {
            indexLocation = s;
            indexer = new HW4(s);
        } catch (Exception ex) {
            System.out.println("Cannot create index..." + ex.getMessage());
            System.exit(-1);
        }

        // ===================================================
        // read input from user until he enters q for quit
        // ===================================================
        while (!s.equalsIgnoreCase("q")) {
            try {
                System.out
                        .println("Enter the FULL path to add into the index (q=quit): (e.g. /home/mydir/docs or c:\\Users\\mydir\\docs)");
                System.out
                        .println("[Acceptable file types: .xml, .html, .html, .txt]");
                s = br.readLine();
                if (s.equalsIgnoreCase("q")) {
                    break;
                }

                // try to add file into the index
                indexer.indexFileOrDirectory(s);
            } catch (Exception e) {
                System.out.println("Error indexing " + s + " : "
                        + e.getMessage());
            }
        }

        // ===================================================
        // after adding, we always have to call the
        // closeIndex, otherwise the index is not created
        // ===================================================
        indexer.closeIndex();


        // =========================================================
        // Now search
        // =========================================================
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexLocation)));
        IndexSearcher searcher = new IndexSearcher(reader);
        //TODO: changed here
        TopScoreDocCollector collector = TopScoreDocCollector.create(100, true);


        HashMap<String, Long> termFrequency = new HashMap<>();

        Fields fields = MultiFields.getFields(reader);
        Terms terms = fields.terms("contents");
        TermsEnum iterator = terms.iterator(null);
        BytesRef byteRef = null;
        while ((byteRef = iterator.next()) != null) {
            String termText = byteRef.utf8ToString();
            Term termInstance = new Term("contents", termText);
            long termFreq = reader.totalTermFreq(termInstance);

            if (termFrequency.containsKey(termText)) {
                termFrequency.put(termText, termFrequency.get(termText) + termFreq);
            } else {
                termFrequency.put(termText, termFreq);
            }

        }

        termFrequency.entrySet()
                .stream()
                .sorted((o1, o2) -> o1.getValue().compareTo(o2.getValue()))
                .forEach(System.out::println);


        s = "";
        while (!s.equalsIgnoreCase("q")) {
            try {
                System.out.println("Enter the search query (q=quit):");
                s = br.readLine();
                if (s.equalsIgnoreCase("q")) {
                    break;
                }

                Query q = new QueryParser(Version.LUCENE_47, "contents", sAnalyzer).parse(s);
                searcher.search(q, collector);
                ScoreDoc[] hits = collector.topDocs().scoreDocs;

                // 4. display results
                System.out.println("Found " + hits.length + " hits.");
                for (int i = 0; i < hits.length; ++i) {
                    int docId = hits[i].doc;
                    Document d = searcher.doc(docId);
                    System.out.println((i + 1) + ". " + d.get("path")
                            + " score=" + hits[i].score);
                }
                // 5. term stats --> watch out for which "version" of the term
                // must be checked here instead!
                Term termInstance = new Term("contents", s);
                long termFreq = reader.totalTermFreq(termInstance);
                long docCount = reader.docFreq(termInstance);
                System.out.println(s + " Term Frequency " + termFreq
                        + " - Document Frequency " + docCount);


            } catch (ParseException e) {
                System.out.println("Error searching " + s + " : " + e.getMessage());
                e.printStackTrace();
            }

        }
    }

    /**
     * Constructor
     *
     * @param indexDir the name of the folder in which the index should be created
     * @throws IOException when exception creating index.
     */
    public HW4(String indexDir) throws IOException {

        FSDirectory dir = FSDirectory.open(new File(indexDir));
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47, sAnalyzer);

        writer = new IndexWriter(dir, config);
    }

    /**
     * Indexes a file or directory
     *
     * @param fileName the name of a text file or a folder we wish to add to the
     *                 index
     * @throws IOException when exception
     */

    public void indexFileOrDirectory(String fileName) throws IOException {
        // ===================================================
        // gets the list of files in a folder (if user has submitted
        // the name of a folder) or gets a single file name (is user
        // has submitted only the file name)
        // ===================================================
        addFiles(new File(fileName));

        int originalNumDocs = writer.numDocs();
        for (File f : queue) {


                Document doc = new Document();
                // ===================================================
                // add contents of file
                // ===================================================
                String contents  = Jsoup.parse(f, "UTF-8").body().text();
                doc.add(new TextField("contents", contents, Field.Store.YES));
                doc.add(new StringField("path", f.getPath(), Field.Store.YES));
                doc.add(new StringField("filename", f.getName(), Field.Store.YES));
                writer.addDocument(doc);
                System.out.println("Added: " + f);

        }

        int newNumDocs = writer.numDocs();
        System.out.println("");
        System.out.println("************************");
        System.out.println((newNumDocs - originalNumDocs) + " documents added.");
        System.out.println("************************");

        queue.clear();
    }

    private String getValidTermsFromDoc(String text) {
        StringBuilder builder = new StringBuilder(text.length());
        Arrays.stream(text.split(" ")).filter(HW4::isNotDigit).forEach(word -> {
            builder.append(word);
            builder.append(" ");
        });

        return builder.toString();
    }


    private static boolean isNotDigit(String word) {
        return !word.chars().allMatch(c -> (char) c >= '0' && (char) c <= '9');
    }

    private void addFiles(File file) {

        if (!file.exists()) {
            System.out.println(file + " does not exist.");
        }
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                addFiles(f);
            }
        } else {
            String filename = file.getName().toLowerCase();
            // ===================================================
            // Only index text files
            // ===================================================
            if (filename.endsWith(".htm") || filename.endsWith(".html")
                    || filename.endsWith(".xml") || filename.endsWith(".txt")) {
                queue.add(file);
            } else {
                System.out.println("Skipped " + filename);
            }
        }
    }

    /**
     * Close the index.
     *
     * @throws IOException when exception closing
     */
    public void closeIndex() throws IOException {
        writer.close();
    }
}