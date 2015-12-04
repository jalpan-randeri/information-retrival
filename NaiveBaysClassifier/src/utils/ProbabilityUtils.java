package utils;

import java.util.Map;

/**
 *
 */
public class ProbabilityUtils {

    public static long getCountOfTermForYes(String term, Map<String, Long> posFreq){
        if(posFreq.containsKey(term)){
            return posFreq.get(term);
        }

        return 0;
    }

    public static long getCountOfTermForNo(String term, Map<String, Long> negFreq){
        if(negFreq.containsKey(term)){
            return negFreq.get(term);
        }

        return 0;
    }


    public static long getCountOfYes(Map<String, Long> posFreq){
        return posFreq.values().stream().mapToLong(Long::longValue).sum();
    }

    public static long getCountOfNo(Map<String, Long> negFreq){
        return negFreq.values().stream().mapToLong(Long::longValue).sum();
    }

    public static long getTotalValueCount(Map<String, Long> termFreq){
        return termFreq.values().stream().mapToLong(Long::longValue).sum();
    }


    public static long getTotalUniqueTermCount(Map<String, Long> termFreq){
        return termFreq.keySet().size();
    }

}
