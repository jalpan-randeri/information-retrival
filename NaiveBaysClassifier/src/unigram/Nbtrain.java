package unigram;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import model.Term;
import utils.TermUtils;
/**
 *
 */
public class Nbtrain {

    public static final String LABEL_P_YES_NO = "p(yes)(no)";
    public static final String LABEL_P_DEFAULT = "p(default)";
    public static final double LAPLACE_SMOOTHING = 1.0;

    public static void main(String[] args) throws IOException {


        if(args.length != 2){
            System.out.println("Usage : model.txt textcat/train");
            System.exit(1);
        }


        String modelFile = args[0];
        String inputPath = args[1];

//        String modelFile = "model.txt";
//        String inputPath = "textcat/train";

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

        pOfYes = Math.log(pOfYes);
        pOfNo = Math.log(pOfNo);

        writer.printf("%s,%.10f,%.10f%n", LABEL_P_YES_NO, pOfYes, pOfNo);

        double defaultValueYes = LAPLACE_SMOOTHING / (countOfYes + totalUniqueTermCount);
        double defaultValueNo = LAPLACE_SMOOTHING / (countOfNo +  totalUniqueTermCount);

        defaultValueYes = Math.log(defaultValueYes);
        defaultValueNo = Math.log(defaultValueNo);

        writer.printf("%s,%.10f,%.10f%n", LABEL_P_DEFAULT,defaultValueYes, defaultValueNo);

        List<Term> list = new ArrayList<>();

        for(String term : termFreq.keySet()){

            long countOfTermForYes = TermUtils.getCountOfTermForYes(term, posTermFreq);
            long countOfTermForNo = TermUtils.getCountOfTermForNo(term, negTermFreq);


            double termOverYes = (countOfTermForYes + LAPLACE_SMOOTHING) / (countOfYes + totalUniqueTermCount);
            double termOverNo = (countOfTermForNo + LAPLACE_SMOOTHING) / (countOfNo + totalUniqueTermCount);

            termOverYes = Math.log(termOverYes);
            termOverNo = Math.log(termOverNo);

            writer.printf("%s,%.10f,%.10f%n",term, termOverYes, termOverNo);

            Term t = new Term();
            t.setWord(term);
            t.setPosToNegRatio(termOverYes / termOverNo);
            t.setNegToPosRatio(termOverNo / termOverYes);

            list.add(t);
        }

        writer.close();


        list.sort((o1, o2) -> o2.getPosToNegRatio().compareTo(o1.getPosToNegRatio()));
        System.out.println("Positive to negative ratio");
        for(int i = 0; i < 20; i++){
            System.out.println(list.get(i).getWord()+", "+list.get(i).getPosToNegRatio());
        }

        System.out.println();

        list.sort((o1, o2) -> o2.getNegToPosRatio().compareTo(o1.getNegToPosRatio()));
        System.out.println("Negative to positive ratio");
        for(int i = 0; i < 20; i++){
            System.out.println(list.get(i).getWord()+", "+list.get(i).getNegToPosRatio());
        }

    }










}
