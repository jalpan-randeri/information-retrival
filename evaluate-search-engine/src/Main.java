import model.Document;
import utils.Metrics;
import utils.RelevantDocRetriever;
import utils.RetrievedDocsRetriver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class Main {
    public static void main(String[] args) throws IOException {

        if(args.length != 3){
            System.out.println("Usage: Main  [judgement-file] [hw-4-result] [query.txt]");
            System.out.println("e.g   cacm.rel    query-results.txt   query.txt");
            System.exit(1);
        }

        String relevantPath = args[0];
        String retrievedPath = args[1];
        String inputPath = args[2];

//        String relevantPath = "cacm.rel";
//        String retrievedPath = "query-results.txt";
//        String inputPath = "query.txt";


        List<String> queries = Files.lines(Paths.get(inputPath)).collect(Collectors.toList());
        List<Double> avgPrecision = new ArrayList<>();
        processQueries(queries,relevantPath, retrievedPath, avgPrecision);
        System.out.println("Mean Avg Precision : "+avgPrecision.stream().mapToDouble(Double::doubleValue).average().getAsDouble());


    }



    private static void processQueries(List<String> quries, String relevantPath, String retrievedPath, List<Double> avgPrecision){

        String HEADER = "rank   doc_id     doc_score    precision    recall  relevance     NDCG";
        System.out.println(HEADER);
        for(String queryLine : quries){
            List<String> result = getMetrics(queryLine, relevantPath, retrievedPath, avgPrecision);
            result.forEach(System.out::println);
        }

    }


    private static List<String> getMetrics(String queryLine, String relevantPath, String retrievedPath, List<Double> avgPrecision){
        String query = getQueryString(queryLine);
        String queryId = getQueryId(queryLine);

        List<String> relevantDocs = RelevantDocRetriever.getDocumentsForQuery(queryId, relevantPath);
        List<Document> retrievedDocs = RetrievedDocsRetriver.getDocumentForQuery(query, retrievedPath);

        List<Integer> idealScores = idealScores(retrievedDocs, relevantDocs);


        List<String> result = new ArrayList<>();
        List<Double> precisionList = new ArrayList<>();

        for(int rank = 1; rank <= retrievedDocs.size(); rank++) {

            String doc_id = retrievedDocs.get(rank - 1).getId();
            double doc_score = retrievedDocs.get(rank - 1).getScore();

            double precision = Metrics.precisionAtRank(relevantDocs, retrievedDocs, rank);
            double recall = Metrics.recallAtRank(relevantDocs, retrievedDocs, rank);
            int relevance = relevantDocs.contains(retrievedDocs.get(rank - 1).getId()) ? 1 : 0;
            double ndcg = Metrics.ndcg(relevantDocs, retrievedDocs, rank, idealScores);

            if(relevance == 1){
                precisionList.add(precision);
            }

            String line = String.format("%3d  %6s  %.10f    %.5f    %.5f      %1d      %.5f",
                                        rank, doc_id, doc_score, precision, recall, relevance, ndcg);

            result.add(line);
        }
        double avgPre = precisionList.stream().mapToDouble(e -> e).average().getAsDouble();
        avgPrecision.add(avgPre);

        System.out.println("AVG precision : "+avgPrecision);
        return result;
    }

    private static List<Integer> idealScores(List<Document> retrievedDocs, List<String> relevantDocs) {
        List<String> orderList = new ArrayList<>();
        orderList.addAll(relevantDocs);

        for(int i = 0; i < retrievedDocs.size() && orderList.size() < 100; i++){
            if(!orderList.contains(retrievedDocs.get(i).getId())){
                orderList.add(retrievedDocs.get(i).getId());
            }
        }

        return orderList.stream()
                .map(doc -> relevantDocs.contains(doc) ? 1 : 0)
                .collect(Collectors.toList());
    }



    private static String getQueryString(String line){
        return line.substring(0, line.indexOf('(')).trim();
    }

    private static String getQueryId(String line){
        return line.substring(line.indexOf('(') + 1, line.length() - 1);
    }

}
