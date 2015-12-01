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

    public static final String LABEL_P_YES_NO = "p(yes)(no)";
    public static final String LABEL_P_DEFAULT = "p(default)";
    public static final double LAPLACE_SMOOTHING = 1.0;

    public static void main(String[] args) throws IOException {


        String modelFile = "model.txt";
        String inputPath = "textcat/train";

        PrintWriter writer = new PrintWriter(modelFile);
        Path positiveDir = Paths.get(inputPath);

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

        writer.printf("%s,%.10f,%.10f%n", LABEL_P_YES_NO, pOfYes, pOfNo);

        double defaultValueYes = LAPLACE_SMOOTHING / (countOfYes + totalUniqueTermCount);
        double defaultValueNo = LAPLACE_SMOOTHING / (countOfNo +  totalUniqueTermCount);

        writer.printf("%s,%.10f,%.10f%n", LABEL_P_DEFAULT,defaultValueYes, defaultValueNo);

        for(String term : termFreq.keySet()){


            long countOfTermForYes = getCountOfTermForYes(term, posTermFreq);
            long countOfTermForNo = getCountOfTermForNo(term, negTermFreq);


            double termOverYes = (countOfTermForYes + LAPLACE_SMOOTHING) / (countOfYes + totalUniqueTermCount);
            double termOverNo = (countOfTermForNo + LAPLACE_SMOOTHING) / (countOfNo + totalUniqueTermCount);

            writer.printf("%s,%.10f,%.10f%n",term, termOverYes, termOverNo);

        }

        writer.close();


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
