package jm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 *
 */
public class Nbtest {

    private static double accuracy = 0;

    public static void main(String[] args) throws IOException {

        String modelFile = "jm-model.txt";
        String testPath = "textcat/test";

        Predictor predictor = new Predictor(modelFile);
        Files.walkFileTree(Paths.get(testPath), predictor);

        System.out.println("False Negative : "+predictor.getFalseNegative());
        System.out.println("False Positive : "+predictor.getFalsePositive());
        System.out.println("True Negative : "+predictor.getTrueNegative());
        System.out.println("True Positive : "+predictor.getTruePositive());
        System.out.println("Unknown : "+predictor.getUnknown());
        System.out.println("Accuracy = "+accuracy(predictor));
        accuracy = accuracy(predictor);

    }


    private static double accuracy(Predictor p){
        return (double) (p.getTrueNegative() + p.getTruePositive())
                / (p.getTrueNegative() + p.getFalseNegative() + p.getFalsePositive() +
                p.getTruePositive() +  p.getUnknown());
    }


    public double getAccuracy(){
        return accuracy;
    }
}
