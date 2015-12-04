package ensamble;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 *
 */
public class Nbtest {
    public static void main(String[] args) throws IOException {

        String uniFile = "model-stem.txt";
        String biFile = "model-bi-stem.txt";
        String triFile = "model-tri-stem.txt";

        String testPath = "textcat/dev";

        Predictor predictor = new Predictor(uniFile, biFile, triFile);
        Files.walkFileTree(Paths.get(testPath), predictor);


        System.out.println("False Negative : "+predictor.getFalseNegative());
        System.out.println("False Positive : "+predictor.getFalsePositive());
        System.out.println("True Negative : "+predictor.getTrueNegative());
        System.out.println("True Positive : "+predictor.getTruePositive());
        System.out.println("Unknown : "+predictor.getUnknown());

        System.out.println("Accuracy = "+((double) predictor.getTrueNegative() + predictor.getTruePositive())
                / (predictor.getTrueNegative() + predictor.getFalseNegative() + predictor.getFalsePositive() +
                predictor.getTruePositive() + predictor.getUnknown()));

    }
}
