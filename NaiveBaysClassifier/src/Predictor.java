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

    private Map<String, Long> posTermFreq;
    private Map<String, Long> negTermFreq;
    private Map<String, Long> termFreq;


    private long countOfYes;
    private long countOfNo;
    private long totalUniqueTermCount;
    private long totalValueCount;



    public Predictor(Map<String, Long> posTermFreq, Map<String, Long> negTermFreq, Map<String, Long> termFreq) {
        this.posTermFreq = posTermFreq;
        this.negTermFreq = negTermFreq;
        this.termFreq = termFreq;



        countOfYes = getCountOfYes(posTermFreq);
        countOfNo = getCountOfNo(negTermFreq);
        totalValueCount = getTotalValueCount(termFreq);
        totalUniqueTermCount = getTotalUniqueTermCount(termFreq);
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

        double pOfYes = (double) countOfYes / totalValueCount;
        double pOfNo = (double) countOfNo / totalValueCount;


        for(String term : terms){

                long countOfTermForYes = getCountOfTermForYes(term, posTermFreq);
                long countOfTermForNo = getCountOfTermForNo(term, negTermFreq);


                double termOverYes = (countOfTermForYes + 1.0) / (countOfYes + totalUniqueTermCount);
                double termOverNo = (countOfTermForNo + 1.0) / (countOfNo + totalUniqueTermCount);



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




    private static long getCountOfTermForYes(String term, Map<String, Long> posFreq){
        if(posFreq.containsKey(term)){
            return posFreq.get(term);
        }

        return 0;
    }

    private static long getCountOfTermForNo(String term, Map<String, Long> negFreq){
        if(negFreq.containsKey(term)){
            return negFreq.get(term);
        }

        return 0;
    }


    private static long getCountOfYes(Map<String, Long> posFreq){
        return posFreq.values().stream().mapToLong(Long::longValue).sum();
    }

    private static long getCountOfNo(Map<String, Long> negFreq){
        return negFreq.values().stream().mapToLong(Long::longValue).sum();
    }

    private static long getTotalValueCount(Map<String, Long> termFreq){
        return termFreq.values().stream().mapToLong(Long::longValue).sum();
    }

    private static long getTotalCountOfTerm(String term, Map<String, Long> termFreq){
        if(termFreq.containsKey(term)){
            return termFreq.get(term);
        }
        return 0;
    }

    private static long getTotalUniqueTermCount(Map<String, Long> termFreq){
        return termFreq.keySet().size();
    }



}
