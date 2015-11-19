import analyzer.HtmlAnalyzer;
import crawler.Crawler;
import handler.DocumentHandler;
import indexer.Indexer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import searcher.Searcher;

import javax.print.Doc;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * Driver class
 */
public class Main {
    public static void main(String[] args) throws IOException {

        if(args.length != 3) {
            System.out.println("Usage : Main <dataset-location> <index-location> <queries.txt>");
            System.exit(1);
        }




        String src = "cacm";
        String indexLocation = "index";
        String queryFile = "queries.txt";






        int printLimit = 100;


        // crawl
        Crawler crawler = new Crawler();
        Queue<Path> files = crawler.crawlDirectory(src);

        // index
        Analyzer analyzer = new HtmlAnalyzer();
        Indexer indexer = new Indexer(files, indexLocation, analyzer);
        indexer.index();

        // plot term - frequency
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexLocation)));
        Map<String, Long> termFrequency = getAllTermsAndFrequencies(reader);
        PrintWriter writer = new PrintWriter("term-frequency.txt");
        termFrequency.entrySet()
                .stream()
                .sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue()))
                .forEach(writer::println);
        writer.close();



        // search
        PrintWriter searchWriter = new PrintWriter("Query-results.txt");
        int limit = 1000000;
        Stream<String> queries = Files.lines(Paths.get(queryFile));
        queries.forEach(line -> {
            try {
                Searcher searcher = new Searcher(reader, analyzer, limit);
                ScoreDoc[] hits =  searcher.search(line);
                System.out.println("Total found docs "+hits.length);

                searchWriter.println("========================================");
                searchWriter.println("Query :"+line);
                searchWriter.println("========================================");

                for (int i = 0; i < hits.length && i < printLimit; ++i) {
                    int docId = hits[i].doc;
                    Document d = searcher.doc(docId);
                    String[] doc = d.getValues(DocumentHandler.FIELD_CONTENTS);
                    String contents;
                    if(doc[0].length() >= 200) {
                        contents = doc[0].substring(0, 200);
                    }else{
                        contents = doc[0];
                    }
                    System.out.println((i + 1) + ".\n" + d.get(DocumentHandler.FIELD_FILE_NAME)
                            + "\nscore=" + hits[i].score + "\ncontents "+contents+"\n");

                    searchWriter.println((i + 1) + ".\n" + d.get(DocumentHandler.FIELD_FILE_NAME)
                            + "\nscore=" + hits[i].score + "\ncontents "+contents+"\n");
                }

            } catch (ParseException | IOException e) {
                e.printStackTrace();
            }
        });

        searchWriter.close();


        List<Long> freq = new ArrayList<>();
        freq.addAll(termFrequency.values());
        Collections.sort(freq, Collections.reverseOrder());




        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries series = new XYSeries("Rank-Frequency log plot");
        for(int i = 0; i < freq.size(); i++) {
            series.add(Math.log10(i + 1), Math.log10(freq.get(i)));
        }
        dataset.addSeries(series);
        chartBuilder(dataset,"zipf's-law-log-plot.jpeg");



        XYSeriesCollection normal_dataset = new XYSeriesCollection();
        XYSeries normal_series = new XYSeries("Rank-Frequency log plot");
        for(int i = 0; i < freq.size(); i++) {
            normal_series.add(i + 1, freq.get(i));
        }
        normal_dataset.addSeries(normal_series);
        chartBuilder(normal_dataset, "normal_plot.jpeg");

    }


    /**
     * build chart file
     * @param dataset XYDataset data set
     * @param filename jpeg file name
     * @throws IOException
     */
    private static void chartBuilder(XYDataset dataset, String filename) throws IOException {

        JFreeChart lineChartObject = ChartFactory.createXYLineChart(
                "Rank Vs frequency",
                "Rank",
                "Frequency",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false);
        lineChartObject.setAntiAlias(true);


        int width = 600; /* Width of the image */
        int height = 480; /* Height of the image */
        File lineChart = new File(filename);
        ChartUtilities.saveChartAsJPEG(lineChart ,lineChartObject, width ,height);
    }

    /**
     * retrieve all terms and their frequencies
     * @param reader Index reader
     * @return Map of term and term frequency
     * @throws IOException
     */
    private static Map<String, Long> getAllTermsAndFrequencies(IndexReader reader) throws IOException {
        HashMap<String, Long> termFrequency = new HashMap<>();
        Fields fields = MultiFields.getFields(reader);
        Terms terms = fields.terms(DocumentHandler.FIELD_CONTENTS);
        TermsEnum iterator = terms.iterator(null);
        BytesRef byteRef;

        while ((byteRef = iterator.next()) != null) {
            String termText = byteRef.utf8ToString();
            Term termInstance = new Term(DocumentHandler.FIELD_CONTENTS, termText);
            long termFreq = reader.totalTermFreq(termInstance);
            termFrequency.putIfAbsent(termText, termFreq);
        }

        return termFrequency;
    }
}
