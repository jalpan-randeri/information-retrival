package jm;

import utils.TermUtils;

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
    public static double LAMBDA = 0.0003;
    public static final String LABEL_P_DEFAULT = "p(default)";


    public static void setLambda(double l){
        LAMBDA = l;
    }

    public static double getLambda(){
        return LAMBDA;
    }

    public static void main(String[] args) throws IOException {


        String modelFile = "jm-model.txt";
        String inputPath = "textcat/train";

        PrintWriter writer = new PrintWriter(modelFile);
        Path positiveDir = Paths.get(inputPath);

        DirectoryTreeWalker walker = new DirectoryTreeWalker();
        Files.walkFileTree(positiveDir, walker);

        Map<String, Long> posTermFreq = walker.getPosTermFrequency();
        Map<String, Long> negTermFreq = walker.getNegTermFrequency();
        Map<String, Long> termFreq = walker.getTermFrequency();

        double countOfYes = TermUtils.getCountOfYes(posTermFreq);
        double countOfNo = TermUtils.getCountOfNo(negTermFreq);
        double totalUniqueTermCount = TermUtils.getTotalUniqueTermCount(termFreq);


        double pOfYes = (double) TermUtils.getCountOfYes(posTermFreq) / TermUtils.getTotalValueCount(termFreq);
        double pOfNo = (double) TermUtils.getCountOfNo(negTermFreq) / TermUtils.getTotalValueCount(termFreq);

        writer.printf("%s,%.10f,%.10f%n", LABEL_P_YES_NO, pOfYes, pOfNo);

        double defaultValueYes = 1.0 / (countOfYes + totalUniqueTermCount);
        double defaultValueNo = 1.0 / (countOfNo +  totalUniqueTermCount);


        writer.printf("%s,%.10f,%.10f%n", LABEL_P_DEFAULT,defaultValueYes, defaultValueNo);

        for(String term : termFreq.keySet()){

            long countOfTermForYes = TermUtils.getCountOfTermForYes(term, posTermFreq);
            long countOfTermForNo = TermUtils.getCountOfTermForNo(term, negTermFreq);


            double termOverYes =  (1 - LAMBDA) * (countOfTermForYes / (countOfYes + totalUniqueTermCount));
            termOverYes = termOverYes + (LAMBDA * defaultValueYes);



            double termOverNo = (1 - LAMBDA) * (countOfTermForNo / (countOfNo + totalUniqueTermCount));
            termOverNo = termOverNo + (LAMBDA * defaultValueNo);


            writer.printf("%s,%.10f,%.10f%n",term, termOverYes, termOverNo);

//            System.out.printf("%s,%.10f,%.10f%n",term, termOverYes, termOverNo);

        }

        writer.close();


    }










}
