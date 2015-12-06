package jm;

import java.io.IOException;

/**
 *
 */
public class CV {
    public static void main(String[] args) throws IOException {

        double accuracy = 0;
        double best = 0.0;
        for(double lambda = 0.0003; lambda <= 0.0004; lambda = lambda + 0.00001){

            Nbtrain.setLambda(lambda);
            Nbtrain.main(null);

            Nbtest tester = new Nbtest();
            Nbtest.main(null);
            double temp = tester.getAccuracy();

            if(temp > accuracy){
                accuracy = temp;
                best = lambda;
            }
        }

        System.out.printf("Best Lambda = %.10f%n",best);

    }
}
