package jm;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
public class Predictor extends SimpleFileVisitor<Path> {

    public static final String SEPARATOR = ",";
    private Map<String, String> probabilities;

    private int falsePositive = 0;
    private int falseNegative = 0;
    private int truePositive = 0;
    private int trueNegative = 0;
    private int unknown = 0;

    public int getUnknown() {
        return unknown;
    }

    public int getFalsePositive() {
        return falsePositive;
    }


    public int getFalseNegative() {
        return falseNegative;
    }


    public int getTruePositive() {
        return truePositive;
    }


    public int getTrueNegative() {
        return trueNegative;
    }

    public Predictor(String modelFile) throws IOException {
        List<String> lines = Files.lines(Paths.get(modelFile)).collect(Collectors.toList());

        probabilities = new HashMap<>();
        for(String line : lines){
            String key = line.substring(0, line.indexOf(SEPARATOR));
            String value = line.substring(line.indexOf(SEPARATOR) + 1);

            probabilities.put(key, value);
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

        String[] temp = probabilities.get(Nbtrain.LABEL_P_YES_NO).split(SEPARATOR);
        double pOfYes =  Double.parseDouble(temp[0]);
        double pOfNo = Double.parseDouble(temp[1]);

        for(String term : terms){

                String value = probabilities.get(term);
                if(value == null){
                    // default

                    yes = yes + Math.log(Nbtrain.LAMBDA * pOfYes);
                    no = no + Math.log(Nbtrain.LAMBDA * pOfNo);
                }else {
                    String[] val = value.split(SEPARATOR);

                    double termOverYes = Double.parseDouble(val[0]);
                    double termOverNo = Double.parseDouble(val[1]);

                    yes = yes + Math.log(termOverYes);
                    no = no + Math.log(termOverNo);
                }

        }
        yes = yes + Math.log(pOfYes);
        no = no + Math.log(pOfNo);


        if(yes == no){
            System.out.println(file.toString()+ " -> Unknown ");
            unknown++;
        }else if(yes > no){
            System.out.println(file.toString()+ " -> Yes " +yes);

            if(file.toString().contains("neg")){
                falsePositive++;
            }else{
                truePositive++;
            }

        }else{
            System.out.println(file.toString()+ " -> No "+no);

            if(file.toString().contains("pos")){
                falseNegative++;
            }else {
                trueNegative++;
            }
        }


        return FileVisitResult.CONTINUE;
    }








}
