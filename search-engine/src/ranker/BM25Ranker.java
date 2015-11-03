package ranker;

import indexer.Indexer;
import model.InvertedIndex;
import model.DocumentScore;
import utils.FileUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Binary Independence Ranking Model 25
 */
public class BM25Ranker {

    public static final String SEPARATOR = " ";
    private static final double K1 = 1.2;
    public static final double K2 = 100.0;
    public static final double B = 0.75;

    public static void main(String[] args) throws IOException {
        if(args.length == 2){
            getQueryRank(args[0], args[1]);
        }else{
            System.out.println("Usage: BM25Ranker query_file index_file");
        }

    }

    private static Map<String, List<DocumentScore>> getQueryRank(String queryDocument, String invertedIndexFile) throws IOException {
        Stream<String> queries = FileUtils.readFiles(queryDocument);
        Iterator<String> iterator = queries.iterator();
        Map<String, List<DocumentScore>> queryDocumentScores = new HashMap<>();

        while(iterator.hasNext()){
            String query = iterator.next();
            List<DocumentScore> rankedDocuments = getRank(query, invertedIndexFile);
            // sort document in decreasing order
            rankedDocuments.sort((doc1, doc2) -> doc2.getScore().compareTo(doc1.getScore()));
            // put list in result
            queryDocumentScores.put(query, rankedDocuments);
        }

        return queryDocumentScores;
    }


    private static List<DocumentScore> getRank(String query, String invertedIndexFile){
        String[] terms = query.split(SEPARATOR);
        List<InvertedIndex> termsDocuments = Arrays.stream(terms)
                .parallel()
                .distinct()
                .map(term -> getInvertedIndex(term, invertedIndexFile))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        return computeBM25Rank(termsDocuments, query);
    }





    private static List<DocumentScore> computeBM25Rank(List<InvertedIndex> documentList, String query){
        // find term frequency in query
        // calculate score for all documents inside document list

        // Note:
        /**
         *
         *                   (ri + 0.5)
         *               ----------------
         *                 (R - ri + 0.5)              (k1 + 1) fi     (k2 + 1) qfi
         *  SUM  Log ----------------------------- X -------------- X --------------
         *  i->Q           (ni - ri + 0.5)               K + fi          k2 + qfi
         *             -------------------------
         *              (N - ni - R + ri + 0.5)
         */

        HashSet<DocumentScore> scores = new HashSet<>();
        documentList.stream()
                .forEach(list -> {
                    String term = list.getTerm();
                    Map<String, Long> frequency = list.getDocumentFrequency();


                    frequency.entrySet()
                            .forEach(entry -> {

                                String docId = entry.getKey();
                                Long termFreq = entry.getValue();

                                // r = 0, R = 0 ( relevance is not available)

                            });




                });

    }



    private static Optional<InvertedIndex> getInvertedIndex(String term, String invertedIndexFile)  {
        try {
            Stream<String> invertedIndex = FileUtils.readFiles(invertedIndexFile);
            String serializedIndexEntry = invertedIndex.filter((word) -> word.startsWith(term))
                                                       .findFirst()
                                                       .get();
            InvertedIndex index = Indexer.deserializeEntry(serializedIndexEntry);
            return Optional.of(index);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}
