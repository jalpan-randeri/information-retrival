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

    public static final String SEPARATOR_SPACE = " ";
    public static final String SEPARATOR_EQUAL = "=";
    private static final double K1 = 1.2;
    public static final double K2 = 100.0;
    public static final double B = 0.75;

    public static void main(String[] args) throws IOException {
        if(args.length == 3){
            long limit = Long.parseLong(args[2]);
            Map<String, List<DocumentScore>> scores = getQueryRank(args[0], args[1]);

            printOnConsole(scores, limit);

        }else{
            System.out.println("Usage: BM25Ranker query_file index_file doc_limit");
        }

    }


    /**
     * print values on console
     * @param docScore Map of query and document scores
     * @param limit long limit of print output
     */
    private static void printOnConsole(Map<String, List<DocumentScore>> docScore, long limit){
        System.out.printf("query_id  Q0  doc_id  rank   BM25_score  system_name %n");
        int index = 1;
        for(List<DocumentScore> scoredDocs : docScore.values()){
            System.out.println("Total docs found : "+scoredDocs.size());
            for(int count = 1; count <= limit; count++) {
                DocumentScore doc = scoredDocs.get(count - 1);
                System.out.printf("%-9d Q0  %-6s %5d   %-10.6f  %s %n", index, doc.getDocumentId(),
                        count, doc.getScore(), "jalpan");
            }
            index++;
        }
    }


    /**
     * get ranked documents for queries
     * @param queryDocument String query document file
     * @param invertedIndexFile String inverted index file
     * @return Map of Query and list of ranked document in decreasing sorted fashion
     * @throws IOException
     */
    private static Map<String, List<DocumentScore>> getQueryRank(String invertedIndexFile, String queryDocument) throws IOException {
        // get all documents and their respective length
        Map<String, Long> documentLength = getAllDocumentsAndLength(invertedIndexFile);
        double avgDocumentLength = documentLength.values()
                                        .parallelStream()
                                        .mapToLong(Long::longValue)
                                        .average()
                                        .getAsDouble();


        Map<String, List<DocumentScore>> queryDocumentScores = new LinkedHashMap<>();
        Stream<String> queries = FileUtils.readFiles(queryDocument);
        Iterator<String> iterator = queries.iterator();
        while(iterator.hasNext()){
            String query = iterator.next();
            List<DocumentScore> rankedDocuments = getRank(query, invertedIndexFile, documentLength, avgDocumentLength);
            // store result
            queryDocumentScores.put(query, rankedDocuments);
        }

        return queryDocumentScores;
    }


    /**
     * get all documents and their respective length
     * @param invertedIndexFile String inverted index file
     * @return Map of Document -> length
     */
    private static Map<String, Long> getAllDocumentsAndLength(String invertedIndexFile){

        HashMap<String, Long> documentLength = new HashMap<>();


        try {
            Stream<String> invertedFile = FileUtils.readFiles(invertedIndexFile);
            Iterator<String> iterator = invertedFile.iterator();

            while(iterator.hasNext()){
                String line = iterator.next();
                // skip till the separator
                if(line.equals(Indexer.DOC_LENGTH_SEPARATOR)) {
                    // read all docs and length
                    while(iterator.hasNext()) {
                        line = iterator.next();
                        String[] tokens = line.split(SEPARATOR_EQUAL);
                        documentLength.put(tokens[0], Long.parseLong(tokens[1]));
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return documentLength;
    }


    /**
     * get ranked document for query
     * @param query String query
     * @param invertedIndexFile String inverted index file
     * @param documentLength Map of Document -> Length
     * @param avgDocumentLength Double average document length
     * @return List of scored document length sorted in decreasing fashion
     */
    private static List<DocumentScore> getRank(String query,
                                               String invertedIndexFile,
                                               Map<String, Long> documentLength,
                                               double avgDocumentLength){
        // get term frequency inside query
        Map<String, Long> queryTermFrequency = getQueryTermFrequency(query);
        // total document length
        int N = documentLength.size();
        Long R = 0L;  // relevance is not available
        Long ri = 0L; // relevance is not available



        // compute score
        List<List<DocumentScore>> termDocumentScores = queryTermFrequency
                .entrySet()
                .stream()
                .map(queryTerm -> {

                    String term = queryTerm.getKey();
                    long qfi = queryTerm.getValue();

                    // get inverted index for the term
                    InvertedIndex invertedIndex = getInvertedIndex(term, invertedIndexFile).get();

                    // get all documents from inverted index
                    Map<String, Long> docTermFreq = invertedIndex.getDocumentFrequency();
                    int ni = invertedIndex.getDocumentFrequency().keySet().size();

                    // calculate score for each term
                    return docTermFreq.entrySet()
                                      .stream()
                                      .map(docEntry -> {
                                            String docName = docEntry.getKey();
                                            long fi = docEntry.getValue();
                                            long dl = documentLength.get(docName);
                                            double currentScore = computeBM25Score(N, R, ni, ri, fi, qfi, K1, K2, B, dl, avgDocumentLength);
                                            return new DocumentScore(docName, currentScore);
                                      })
                                      .collect(Collectors.toList());

                }).collect(Collectors.toList());


        HashMap<String, Double> scores = new HashMap<>();
        termDocumentScores
                .stream()
                .forEach(scoredDocList -> {
                    scoredDocList
                            .stream()
                            .forEach(score -> {
                                if(scores.containsKey(score.getDocumentId())){
                                    scores.put(score.getDocumentId(),
                                               scores.get(score.getDocumentId()) + score.getScore());
                                }else{
                                    scores.put(score.getDocumentId(), score.getScore());
                                }
                            });
                });



        return scores.entrySet()
                .stream()
                .map(entry -> new DocumentScore(entry.getKey(), entry.getValue()))
                .sorted((d1, d2) -> (d2.getScore().compareTo(d1.getScore())))
                .collect(Collectors.toList());
    }


    /**
     * compute BM25 score
     * @param N total number of document in collection
     * @param R total number of relevant documents in collection
     * @param ni total number of document containing ith term
     * @param ri total number of relevant document containing ith term
     * @param fi frequency of ith term in document
     * @param qfi frequency of ith term in query
     * @param k1 parameter
     * @param k2 parameter
     * @param b parameter
     * @param dl document length
     * @param avdl average document length
     * @return double BM25 score
     */
    private static double computeBM25Score(double N, double R, double ni, double ri, double fi, double qfi, double k1,
                                           double k2, double b, long dl, double avdl){

        // Note: Formula for BM25 score
        /**
         *
         *                                                            (ri + 0.5)
         *                                                         ----------------
         *            (k1 + 1) fi     (k2 + 1) qfi                  (R - ri + 0.5)
         *  SUM    -------------- X --------------   X   Log {-----------------------------}
         *  i->Q        K + fi          k2 + qfi                    (ni - ri + 0.5)
         *                                                     -------------------------
         *                                                     (N - ni - R + ri + 0.5)
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

        double frequencyPart = ((k1 + 1) * fi) / (K + fi);
        double queryFreqPart = ((k2 + 1) * qfi) / (k2 + qfi);

        // log part
        double firstNumerator = ((ri + 0.5) / (R - ri + 0.5));
        double firstDivider = (ni - ri + 0.5) / (N - ni - R + ri + 0.5);

        return frequencyPart * queryFreqPart * Math.log((firstNumerator / firstDivider));

    }

    /**
     * compute K parameter
     * @param k1 parameter
     * @param b parameter
     * @param documentLength Double
     * @param avgDocumentLength Double
     * @return Parameter K
     */
    private static double computeK(double k1, double b, double documentLength, double avgDocumentLength){
        /**
         *                             dl
         *  K =  k1(  (1 - b) + b X ------ )
         *                            avdl
         *
         *  dl   ->   document length
         *  avdl -> average document length
         */

        return k1 * ( (1 - b) + (b * (documentLength / avgDocumentLength)));
    }


    /**
     * get query term frequency
     * @param query String query
     * @return Map of Term -> Frequency inside query
     */
    private static Map<String, Long> getQueryTermFrequency(String query) {
        LinkedHashMap<String, Long> queryTermFrequency = new LinkedHashMap<>();
        for(String term : query.split(SEPARATOR_SPACE)){
            if(queryTermFrequency.containsKey(term)){
                queryTermFrequency.put(term, queryTermFrequency.get(term) + 1);
            }else{
                queryTermFrequency.put(term, 1L);
            }
        }

        return queryTermFrequency;
    }

    /**
     * retrieve inverted index
     * @param term String term
     * @param invertedIndexFile String inverted index file
     * @return may be inverted index if index is present in collection
     */
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
