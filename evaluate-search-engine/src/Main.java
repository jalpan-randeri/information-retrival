import utils.Metrics;
import utils.RelevantDocRetriever;
import utils.RetrievedDocsRetriver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 *
 */
public class Main {
    public static void main(String[] args) throws IOException {

        String relevantPath = "cacm.rel";
        String retrievedPath = "query-results.txt";
        String inputPath = "query.txt";

        System.out.printf("%2s %-50s %4s   %4s     %4s  %5s%n","NO","Query","R@K", "P@K",
                "AvgPrec", "NDCG");
        Files.lines(Paths.get(inputPath))
            .forEach(line -> {
                String query = getQueryString(line).trim();
                String queryId = getQueryId(line);

                List<String> relevantDocs = RelevantDocRetriever.getDocumentsForQuery(queryId, relevantPath);
                List<String> retrievedDocs = RetrievedDocsRetriver.getDocumentForQuery(query, retrievedPath);

                for(int rank = 1; rank <= 20; rank++) {
                    double recallAtRank = Metrics.recallAtRank(relevantDocs, retrievedDocs, rank);
                    double precisionAtRank = Metrics.precisionAtRank(relevantDocs, retrievedDocs, rank);
                    double meanAvgPrecisionForRank = Metrics.averagePrecisionForRank(relevantDocs, retrievedDocs, rank);
                    double normalizedDiscountedCumulativeGain = Metrics.normalizedDiscountedCumulativeGain(relevantDocs,
                            retrievedDocs, rank);
                    System.out.printf("%2d %-50s %.4f  %.4f    %.4f    %.4f%n", rank, line, recallAtRank,
                            precisionAtRank, meanAvgPrecisionForRank, normalizedDiscountedCumulativeGain);
                }
                System.out.println("============================================================");

            });
    }


    private static String getQueryString(String line){
        return line.substring(0, line.indexOf('('));
    }

    private static String getQueryId(String line){
        return line.substring(line.indexOf('(') + 1, line.length() - 1);
    }

}
