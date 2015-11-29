package utils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Metrics {

    public static double recall(List<String> relevantList, List<String> retrievedList){
        long common = getCommonElementsCount(relevantList, retrievedList);
        return common / (double) relevantList.size();
    }


    public static double precision(List<String> relevantList, List<String> retrievedList){
        long common = getCommonElementsCount(relevantList, retrievedList);
        return common / (double) retrievedList.size();
    }



    public static double precisionAtRank(List<String> relevantList, List<String> retrievedList, int rank){
        if(retrievedList.size() > rank) {
            retrievedList = retrievedList.subList(0, rank);
        }

        return precision(relevantList, retrievedList);
    }


    private static long  getCommonElementsCount(List<String> a, List<String> b){
        return a.parallelStream().filter(b::contains).count();
    }


    public static double averagePrecisionForRank(List<String> relevantDocs, List<String> retrievedDocs, int rank) {
        double sum = 0;
        int count = 0;
        for(int i = 1; i <= rank; i++){
            if(relevantDocs.contains(retrievedDocs.get(i - 1))) {
                sum = sum + precisionAtRank(relevantDocs, retrievedDocs, i);
                count++;
            }
        }
        return sum / count;
    }



    private static int getRelevenceScore(String line){
        return 1;
    }

    public static double discountedCumulativeGain(List<Integer> relevanceScores){
        double score = 0;
        for(int i = 0; i < relevanceScores.size(); i++){
            score = score + ( (Math.pow(2, relevanceScores.get(i)) - 1)  / Math.log(1 + 1  + i));
        }
        return score;
    }


    private static double idealDiscountedCumulativeGain(int rank){
        double score = 0;
        for(int i = 0; i < rank; i++){
            score = score + ( 1 / Math.log(1 + 1 + i));
        }
        return score;
    }

    public static double normalizedDiscountedCumulativeGain(List<String> relevantDocs, List<String> retrievedDocs,
                                                            int rank){
        retrievedDocs = retrievedDocs.subList(0, rank);
        List<Integer> relevanceScores = getRelevanceScoreList(relevantDocs, retrievedDocs);

        return discountedCumulativeGain(relevanceScores) / idealDiscountedCumulativeGain(rank);
    }


    private static List<Integer> getRelevanceScoreList(List<String> relevantDocs, List<String> retrivedDocs){
        List<Integer> relevanceScore = new ArrayList<>();
        for (String doc : retrivedDocs) {
            if (relevantDocs.contains(doc)) {
                relevanceScore.add(1);
            } else {
                relevanceScore.add(0);
            }
        }
        return relevanceScore;
    }

    public static double recallAtRank(List<String> relevantDocs, List<String> retrievedDocs, int rank) {
        retrievedDocs = retrievedDocs.subList(0, rank);
        return  recall(relevantDocs, retrievedDocs);
    }
}
