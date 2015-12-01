import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 *
 */
public class Nbtrain {
    public static void main(String[] args) throws IOException {


        PrintWriter writer = new PrintWriter("model.txt");

        Path positiveDir = Paths.get("textcat/train");

        DirectoryTreeWalker walker = new DirectoryTreeWalker();
        Files.walkFileTree(positiveDir, walker);

        Map<String, Long> posTermFreq = walker.getPosTermFrequency();
        Map<String, Long> negTermFreq = walker.getNegTermFrequency();
        Map<String, Long> termFreq = walker.getTermFrequency();

        double countOfYes = getCountOfYes(posTermFreq);
        double countOfNo = getCountOfNo(negTermFreq);
        double totalUniqueTermCount = getTotalUniqueTermCount(termFreq);


        double pOfYes = (double) getCountOfYes(posTermFreq) / getTotalValueCount(termFreq);
        double pOfNo = (double) getCountOfNo(negTermFreq) / getTotalValueCount(termFreq);

        writer.printf("%s,%.10f,%.10f%n","p(yes)(no)", pOfYes, pOfNo);

        double defaultValueYes = 1.0 / (countOfYes + totalUniqueTermCount);
        double defaultValueNo = 1.0 / (countOfNo +  totalUniqueTermCount);

        writer.printf("%s,%.10f,%.10f%n","p(default)",defaultValueYes, defaultValueNo);

        for(String term : termFreq.keySet()){


            long countOfTermForYes = getCountOfTermForYes(term, posTermFreq);
            long countOfTermForNo = getCountOfTermForNo(term, negTermFreq);


            double termOverYes = (countOfTermForYes + 1.0) / (countOfYes + totalUniqueTermCount);
            double termOverNo = (countOfTermForNo + 1.0) / (countOfNo + totalUniqueTermCount);

            writer.printf("%s,%.10f,%.10f%n",term, termOverYes, termOverNo);

        }

        writer.close();




        Predictor predictor = new Predictor("model.txt");
        Files.walkFileTree(Paths.get("textcat/test"), predictor);

    }




    private static long getCountOfTermForYes(String term, Map<String, Long> posFreq){
        if(posFreq.containsKey(term)){
            return posFreq.get(term);
        }

        return 0;
    }

    private static long getCountOfTermForNo(String term, Map<String, Long> negFreq){
        if(negFreq.containsKey(term)){
            return negFreq.get(term);
        }

        return 0;
    }


    private static long getCountOfYes(Map<String, Long> posFreq){
        return posFreq.values().stream().mapToLong(Long::longValue).sum();
    }

    private static long getCountOfNo(Map<String, Long> negFreq){
        return negFreq.values().stream().mapToLong(Long::longValue).sum();
    }

    private static long getTotalValueCount(Map<String, Long> termFreq){
        return termFreq.values().stream().mapToLong(Long::longValue).sum();
    }


    private static long getTotalUniqueTermCount(Map<String, Long> termFreq){
        return termFreq.keySet().size();
    }





}
