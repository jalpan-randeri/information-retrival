package utils;

import model.Document;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Metrics {

    public static double recall(List<String> relevantList, List<Document> retrievedList){
        long common = getCommonElementsCount(relevantList, retrievedList);
        return common / (double) relevantList.size();
    }


    public static double precision(List<String> relevantList, List<Document> retrievedList){
        long common = getCommonElementsCount(relevantList, retrievedList);
        return common / (double) retrievedList.size();
    }



    public static double precisionAtRank(List<String> relevantList, List<Document> retrievedList, int rank){
        if(retrievedList.size() > rank) {
            retrievedList = retrievedList.subList(0, rank);
        }

        return precision(relevantList, retrievedList);
    }


    private static long  getCommonElementsCount(List<String> a, List<Document> b){
        return b.stream().filter(doc -> a.contains(doc.getId())).count();
    }


    public static double discountedCumulativeGain(List<Integer> relevanceScores){

        /**
         *        P     2^rel(i) - 1
         * DCG = sum   --------------
         *       i=1     log (1 + i)
         *
         */

        double score = 0;
        for(int i = 1; i <= relevanceScores.size(); i++){
            score = score + ( (Math.pow(2, relevanceScores.get(i - 1)) - 1)  / Math.log(1 + i));
        }
        return score;
    }


    public static double ndcg(List<String> relevantDocs, List<Document> retrievedDocs,
                              int rank, List<Document> orderedList){
        retrievedDocs = retrievedDocs.subList(0, rank);
        List<Integer> relevanceScores = getRelevanceScoreList(relevantDocs, retrievedDocs);
        List<Integer> idealScores = getRelevanceScoreList(relevantDocs, orderedList);

        return discountedCumulativeGain(relevanceScores) / discountedCumulativeGain(idealScores);
    }


    private static List<Integer> getRelevanceScoreList(List<String> relevantDocs, List<Document> retrivedDocs){
        List<Integer> relevanceScore = new ArrayList<>();
        for (Document doc : retrivedDocs) {
            if (relevantDocs.contains(doc.getId())) {
                relevanceScore.add(1);
            } else {
                relevanceScore.add(0);
            }
        }
        return relevanceScore;
    }

    public static double recallAtRank(List<String> relevantDocs, List<Document> retrievedDocs, int rank) {
        retrievedDocs = retrievedDocs.subList(0, rank);
        return  recall(relevantDocs, retrievedDocs);
    }
}
