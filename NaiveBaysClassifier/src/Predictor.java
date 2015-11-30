import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 */
public class Predictor extends SimpleFileVisitor<Path> {

    private Map<String, String> probalities;

    public Predictor(String modelFile) throws IOException {
        List<String> lines = Files.lines(Paths.get(modelFile)).collect(Collectors.toList());

        probalities = new HashMap<>();
        for(String line : lines){
            String key = line.substring(0, line.indexOf(','));
            String value = line.substring(line.indexOf(',') + 1);

            probalities.put(key, value);
        }
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {


        List<String> lines = Files.lines(file).collect(Collectors.toList());
        Set<String> terms = new HashSet<>();
        for(String line : lines){
            String[] t = line.split(" ");
            Arrays.stream(t).filter(term -> !term.isEmpty()).forEach(terms::add);
        }

        double yes = 0;
        double no = 0;

        String[] temp = probalities.get("p(yes)(no)").split(",");
        double pOfYes =  Double.parseDouble(temp[0]);
        double pOfNo = Double.parseDouble(temp[1]);

        for(String term : terms){

                String value = probalities.get(term);
                if(value == null){
                    value = probalities.get("p(default)");
                }

                String[] val = value.split(",");

                double termOverYes = Double.parseDouble(val[0]);
                double termOverNo = Double.parseDouble(val[1]);

                yes = yes + Math.log(termOverYes);
                no = no + Math.log(termOverNo);

        }
        yes = yes + Math.log(pOfYes);
        no = no + Math.log(pOfNo);


        if(yes > no){
            System.out.println(file.toString()+ " -> Yes " +yes);
        }else{
            System.out.println(file.toString()+ " -> No "+no);
        }


        return FileVisitResult.CONTINUE;
    }








}
