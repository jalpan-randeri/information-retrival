package trigram.stem;

import utils.ProbabilityUtils;

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


        String modelFile = "model-tri-stem.txt";
        String inputPath = "textcat/train";

        PrintWriter writer = new PrintWriter(modelFile);
        Path positiveDir = Paths.get(inputPath);

        DirectoryTreeWalker walker = new DirectoryTreeWalker();
        Files.walkFileTree(positiveDir, walker);

        Map<String, Long> posTermFreq = walker.getPosTermFrequency();
        Map<String, Long> negTermFreq = walker.getNegTermFrequency();
        Map<String, Long> termFreq = walker.getTermFrequency();

        double countOfYes = ProbabilityUtils.getCountOfYes(posTermFreq);
        double countOfNo = ProbabilityUtils.getCountOfNo(negTermFreq);
        double totalUniqueTermCount = ProbabilityUtils.getTotalUniqueTermCount(termFreq);


        double pOfYes = (double) ProbabilityUtils.getCountOfYes(posTermFreq) / ProbabilityUtils.getTotalValueCount(termFreq);
        double pOfNo = (double) ProbabilityUtils.getCountOfNo(negTermFreq) / ProbabilityUtils.getTotalValueCount(termFreq);

        writer.printf("%s,%.10f,%.10f%n", LABEL_P_YES_NO, pOfYes, pOfNo);

        double defaultValueYes = LAPLACE_SMOOTHING / (countOfYes + totalUniqueTermCount);
        double defaultValueNo = LAPLACE_SMOOTHING / (countOfNo +  totalUniqueTermCount);

        writer.printf("%s,%.10f,%.10f%n", LABEL_P_DEFAULT,defaultValueYes, defaultValueNo);

        for(String term : termFreq.keySet()){


            long countOfTermForYes = ProbabilityUtils.getCountOfTermForYes(term, posTermFreq);
            long countOfTermForNo = ProbabilityUtils.getCountOfTermForNo(term, negTermFreq);


            double termOverYes = (countOfTermForYes + LAPLACE_SMOOTHING) / (countOfYes + totalUniqueTermCount);
            double termOverNo = (countOfTermForNo + LAPLACE_SMOOTHING) / (countOfNo + totalUniqueTermCount);

            writer.printf("%s,%.10f,%.10f%n",term, termOverYes, termOverNo);

        }

        writer.close();


    }










}
