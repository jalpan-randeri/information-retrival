import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 *
 */
public class Main {
    public static void main(String[] args) throws IOException {


        PrintWriter writer = new PrintWriter("model.txt");

        Path positiveDir = Paths.get("textcat/train");

        DirectoryTreeWalker walker = new DirectoryTreeWalker();
        Files.walkFileTree(positiveDir, walker);

        Map<String, Long> posTermFreq = walker.getPosTermFrequency();
        Map<String, Long> negTermFreq = walker.getNegTermFrequency();
        Map<String, Long> termFreq = walker.getTermFrequency();


        writer.println("====POS=====");
        posTermFreq.entrySet().forEach(e -> writer.printf("%s=%d%n",e.getKey(), e.getValue()));

        writer.println("====NEG=====");
        negTermFreq.entrySet().forEach(e -> writer.printf("%s=%d%n",e.getKey(), e.getValue()));

        writer.println("====TOTAL=====");
        termFreq.entrySet().forEach(e -> writer.printf("%s=%d%n",e.getKey(), e.getValue()));


        writer.close();



        //NOte: test prediction



        Predictor predictor = new Predictor(posTermFreq, negTermFreq, termFreq);
        Files.walkFileTree(Paths.get("textcat/train/pos"), predictor);

        Files.walkFileTree(Paths.get("textcat/train/neg"), predictor);


        Files.walkFileTree(Paths.get("textcat/test"), predictor);

    }










}
