package unigram;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 *
 */
public class Nbtest {
    public static void main(String[] args) throws IOException {

        if(args.length != 2){
            System.out.println("Usage : model.txt textcat/dev");
            System.exit(1);
        }

        String modelFile = args[0];
        String testPath = args[1];

//        String modelFile = "model.txt";
//        String testPath = "textcat/dev";

        Predictor predictor = new Predictor(modelFile);
        Files.walkFileTree(Paths.get(testPath), predictor);

        System.out.println("False Negative : "+predictor.getFalseNegative());
        System.out.println("False Positive : "+predictor.getFalsePositive());
        System.out.println("True Negative : "+predictor.getTrueNegative());
        System.out.println("True Positive : "+predictor.getTruePositive());

        System.out.println("Accuracy = "+((double) predictor.getTrueNegative() + predictor.getTruePositive())
                / (predictor.getTrueNegative() + predictor.getFalseNegative() + predictor.getFalsePositive() +
                predictor.getTruePositive()));
    }
}
