import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 *
 */
public class Nbtest {
    public static void main(String[] args) throws IOException {

        String modelFile = "model.txt";
        String testPath = "textcat/test";

        Predictor predictor = new Predictor(modelFile);
        Files.walkFileTree(Paths.get(testPath), predictor);
    }
}
