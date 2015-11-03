package ranker;

import indexer.Indexer;
import model.InvertedIndex;
import model.DocumentScore;
import utils.FileUtils;

import javax.print.Doc;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
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
            Map<String, List<DocumentScore>> scores = getQueryRank(args[0], args[1]);
            scores.entrySet()
                  .stream()
                  .forEach(entry -> {
                      String query = entry.getKey();
                      List<DocumentScore> scoreList = entry.getValue();

                      scoreList.stream()
                              .limit(100)
                              .forEach(doc -> System.out.printf("%s  %s  %f\n",query, doc.getDocumentId(), doc.getScore()));

                  });
        }else{
            System.out.println("Usage: BM25Ranker query_file index_file");
        }

    }

    private static Map<String, List<DocumentScore>> getQueryRank(String queryDocument, String invertedIndexFile) throws IOException {

        Map<String, Long> documentLength = getAllDocumentsAndLength(invertedIndexFile);
        double avgDocumentLength = documentLength.values()
                                        .parallelStream()
                                        .mapToLong(l -> l)
                                        .average()
                                        .getAsDouble();


        Stream<String> queries = FileUtils.readFiles(queryDocument);
        Iterator<String> iterator = queries.iterator();
        Map<String, List<DocumentScore>> queryDocumentScores = new HashMap<>();

        while(iterator.hasNext()){
            String query = iterator.next();
            List<DocumentScore> rankedDocuments = getRank(query, invertedIndexFile, documentLength, avgDocumentLength);
            // store result
            queryDocumentScores.put(query, rankedDocuments);
        }

        return queryDocumentScores;
    }


    private static Map<String, Long> getAllDocumentsAndLength(String invertedIndexFile){

        HashMap<String, Long> documentLength = new HashMap<>();


        try {
            Stream<String> invertedFile = FileUtils.readFiles(invertedIndexFile);


            Iterator<String> iterator = invertedFile.iterator();
            while(iterator.hasNext()){
                String line = iterator.next();
                InvertedIndex index = Indexer.deserializeEntry(line);

                index.getDocumentFrequency().forEach((doc,length) -> {
                    if(documentLength.containsKey(doc)){
                        documentLength.put(doc, documentLength.get(doc) + length);
                    }else{
                        documentLength.put(doc, length);
                    }
                });

            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        return documentLength;
    }



    private static List<DocumentScore> getRank(String query,
                                               String invertedIndexFile,
                                               Map<String, Long> documentLength,
                                               double avgDocumentLength){

        Map<String, Long> queryTermFrequency = getQueryTermFrequency(query);
        int N = documentLength.size();
        Long R = 0L;  // relevance is not available
        Long ri = 0L; // relevance is not available

        HashMap<String, Double> scores = new HashMap<>();

        queryTermFrequency.entrySet().stream().forEach(queryTerm -> {
            String term = queryTerm.getKey();
            long qfi = queryTerm.getValue();

            InvertedIndex invertedIndex = getInvertedIndex(term, invertedIndexFile).get();
            Map<String, Long> docTermFreq = invertedIndex.getDocumentFrequency();
            int ni = invertedIndex.getDocumentFrequency().size();

            docTermFreq.entrySet().stream().forEach(docEntry ->{
                String docName = docEntry.getKey();
                long fi = docEntry.getValue();
                long dl = documentLength.get(docName);

                double currentScore = computeBM25Rank(N, R, ni, ri, fi, qfi, K1, K2, B, dl, avgDocumentLength);

                // check and add previously computed score with current score
                if(scores.containsKey(docName)){
                    scores.put(docName, scores.get(docName) + currentScore);
                }else{
                    scores.put(docName, currentScore);
                }

            });

        });


        return scores.entrySet()
                .stream()
                .map(entry -> new DocumentScore(entry.getKey(), entry.getValue()))
                .sorted((d1, d2) -> (d2.getScore().compareTo(d1.getScore())))
                .collect(Collectors.toList());
    }





    private static double computeBM25Rank(long N, long R, long ni, long ri, long fi, long qfi, double k1,
                                                       double k2, double b, long dl, double avdl){
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
         *
         *
         *  N   -> Total number of documents in collection
         *  R   -> Total number of document in Relevant collection
         *  ni  -> Total number of document containing ith term
         *  ri  -> Total number of document in relevant collection containing ith term
         *
         *  fi  -> Frequency of ith term in Document
         *  qfi -> Frequency of ith term in query
         *
         *  k1  -> parameter
         *  k2  -> parameter
         *  b   -> parameter
         *
         *  K   -> length normalization parameter
         *
         */


        double K = computeK(k1, b, dl, avdl);

        double firstNumerator = ((ri + 0.5) / (R - ri + 0.5));
        double firstDivider = (ni - ri + 0.5) / (N - ni - R + ri + 0.5);

        double frequencyPart = ((k1 + 1) * fi) / (K + fi);
        double queryFreqPart = ((k2 + 1) * qfi) / (k2 + qfi);

        return Math.log((firstNumerator / firstDivider) * frequencyPart * queryFreqPart);

    }


    private static double computeK(double k1, double b, long documentLength, double avgDocumentLength){
        /**
         *                             dl
         *  K =  k1(  (1 - b) + b X ------ )
         *                            avdl
         *
         *  dl   ->   document length
         *  avdl -> average document length
         */

        return k1 * ( (1 - b) + b * (documentLength / avgDocumentLength));
    }


    private static Map<String, Long> getQueryTermFrequency(String query) {
        HashMap<String, Long> queryTermFrequency = new HashMap<>();
        for(String term : query.split(SEPARATOR)){
            if(queryTermFrequency.containsKey(term)){
                queryTermFrequency.put(term, queryTermFrequency.get(term) + 1);
            }else{
                queryTermFrequency.put(term, 1L);
            }
        }

        return queryTermFrequency;
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
