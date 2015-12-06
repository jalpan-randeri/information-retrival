import model.Document;
import utils.Metrics;
import utils.RelevantDocRetriever;
import utils.RetrievedDocsRetriver;

import javax.print.Doc;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 */
public class Main {
    public static void main(String[] args) throws IOException {

        String relevantPath = "cacm.rel";
        String retrievedPath = "query-results.txt";
        String inputPath = "query.txt";


        List<String> queries = Files.lines(Paths.get(inputPath)).collect(Collectors.toList());
        processQueries(queries,relevantPath, retrievedPath);


    }



    private static void processQueries(List<String> quries,  String relevantPath, String retrievedPath){

        String HEADER = "rank   doc_id     doc_score    precision    recall  relevance     NDCG";
        System.out.println(HEADER);
        for(String queryLine : quries){
            List<String> result = getMetrics(queryLine, relevantPath, retrievedPath);
            result.forEach(System.out::println);
        }

    }


    private static List<String> getMetrics(String queryLine, String relevantPath, String retrievedPath){
        String query = getQueryString(queryLine);
        String queryId = getQueryId(queryLine);

        List<String> relevantDocs = RelevantDocRetriever.getDocumentsForQuery(queryId, relevantPath);
        List<Document> retrievedDocs = RetrievedDocsRetriver.getDocumentForQuery(query, retrievedPath);

        List<Document> reorderList = reorder(retrievedDocs, relevantDocs);


        List<String> result = new ArrayList<>();
        for(int rank = 1; rank <= 100; rank++) {

            String doc_id = retrievedDocs.get(rank - 1).getId();
            double doc_score = retrievedDocs.get(rank - 1).getScore();

            double precision = Metrics.precisionAtRank(relevantDocs, retrievedDocs, rank);
            double recall = Metrics.recallAtRank(relevantDocs, retrievedDocs, rank);

            int relevance = relevantDocs.contains(retrievedDocs.get(rank - 1).getId()) ? 1 : 0;

            double ndcg = Metrics.ndcg(relevantDocs, retrievedDocs, rank, reorderList);

            String line = String.format("%3d  %6s  %.10f    %.5f    %.5f      %1d      %.5f",
                                        rank, doc_id, doc_score, precision, recall, relevance, ndcg);

            result.add(line);
        }

        return result;
    }

    private static List<Document> reorder(List<Document> retrievedDocs, List<String> relevantDocs) {
        List<Document> orderList = new ArrayList<>();

        // add first relevant docs in relevant order
        for(String relDoc : relevantDocs){
            Optional<Document> doc = getDocumentWithID(relDoc, retrievedDocs);
            if(doc.isPresent()){
                orderList.add(doc.get());
            }
        }

        // add remaining docs
        retrievedDocs.stream()
                .filter(d -> !orderList.contains(d))
                .forEach(orderList::add);

        return orderList;
    }

    private static Optional<Document> getDocumentWithID(String id, List<Document> list){
        return list.stream().filter(d -> d.getId().equals(id)).findFirst();
    }


    private static String getQueryString(String line){
        return line.substring(0, line.indexOf('(')).trim();
    }

    private static String getQueryId(String line){
        return line.substring(line.indexOf('(') + 1, line.length() - 1);
    }

}
