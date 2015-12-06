package ensamble;

import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.flexible.standard.QueryParserUtil;
import org.apache.lucene.util.Version;
import trigram.stem.DirectoryTreeWalker;
import trigram.stem.Nbtrain;
import utils.TextUtils;

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
    private Map<String, String> probUnigram =  new HashMap<>();
    private Map<String, String> probBigram =  new HashMap<>();
    private Map<String, String> probTrigram =  new HashMap<>();

    private QueryParser parser;

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


    public Predictor(String uniFile, String biFile, String triFile) throws IOException {

        readProbabilities(uniFile, probUnigram);
        readProbabilities(biFile, probBigram);
        readProbabilities(triFile, probTrigram);


        EnglishAnalyzer analyzer = new EnglishAnalyzer(Version.LUCENE_47);
        parser  = new QueryParser(Version.LUCENE_47, DirectoryTreeWalker.FIELD_TERM, analyzer);

    }

    private void readProbabilities(String file, Map<String, String> probabilities) throws IOException {
        List<String> lines = Files.lines(Paths.get(file)).collect(Collectors.toList());
        for (String line : lines) {
            String key = line.substring(0, line.indexOf(SEPARATOR));
            String value = line.substring(line.indexOf(SEPARATOR) + 1);
            probabilities.put(key, value);
        }
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {


        String resultUni = predictUni(file);
        String resultBi = predictBi(file);
        String resultTri = predictTri(file);

        String[] results = {resultUni, resultBi, resultTri};

        int yes = 0;
        int no = 0;

        for(String r : results){
            if(r.equals("YES")) {
                yes++;
            } else {
                no++;
            }
        }

        if(yes == no) {
//            System.out.println(file.toString() +" -> Unknown");
            unknown++;
        }else if(yes > no){
//            System.out.println(file.toString()+" -> YES");

            if(file.toString().contains("pos")){
                truePositive++;
            }else{
                falsePositive++;
            }

        }else{
//            System.out.println(file.toString()+" -> NO");

            if(file.toString().contains("neg")){
                trueNegative++;
            }else{
                falseNegative++;
            }
        }

        System.out.printf("%s   %d   %d%n",file, yes, no);


        return FileVisitResult.CONTINUE;
    }

    private String predictTri(Path file) throws IOException {

        List<String> lines = Files.lines(file).collect(Collectors.toList());
        Set<String> terms = new HashSet<>();
        for(String line : lines){

            try {

                String stemmed = parser.parse(QueryParserUtil.escape(line)).toString(DirectoryTreeWalker.FIELD_TERM);
                stemmed = TextUtils.removeStopWords(stemmed);
                String[] tokens =  stemmed.split(" ");

                for(int i = 2; i < tokens.length; i++){

                    List<String> temp = new ArrayList<>();
                    temp.add(tokens[i - 2]);
                    temp.add(tokens[i - 1]);
                    temp.add(tokens[i]);

                    terms.addAll(TextUtils.permute(temp)
                            .stream()
                            .filter(s -> probTrigram.containsKey(s))
                            .collect(Collectors.toList()));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        double yes = 0;
        double no = 0;

        String[] temp = probTrigram.get(Nbtrain.LABEL_P_YES_NO).split(SEPARATOR);
        double pOfYes =  Double.parseDouble(temp[0]);
        double pOfNo = Double.parseDouble(temp[1]);

        for(String term : terms){

            String value = probTrigram.get(term);
            if(value == null){
                value = probTrigram.get(Nbtrain.LABEL_P_DEFAULT);
            }

            String[] val = value.split(SEPARATOR);

            double termOverYes = Double.parseDouble(val[0]);
            double termOverNo = Double.parseDouble(val[1]);

            yes = yes + Math.log(termOverYes);
            no = no + Math.log(termOverNo);

        }
        yes = yes + Math.log(pOfYes);
        no = no + Math.log(pOfNo);

        return (yes >= no) ? "YES" : "NO";
    }

    private String predictBi(Path file) throws IOException {
        List<String> lines = Files.lines(file).collect(Collectors.toList());
        Set<String> terms = new HashSet<>();
        for(String line : lines){

            try {
                String stemmed = parser.parse(QueryParserUtil.escape(line)).toString(bigram.stem.DirectoryTreeWalker.FIELD_TERM);
                stemmed = TextUtils.removeStopWords(stemmed);
                String[] tokens =  stemmed.split(" ");

                for(int i = 1; i < tokens.length; i++){
                    String forward = tokens[i - 1]+" "+tokens[i];
                    terms.add(forward);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        double yes = 0;
        double no = 0;

        String[] temp = probBigram.get(bigram.stem.Nbtrain.LABEL_P_YES_NO).split(SEPARATOR);
        double pOfYes =  Double.parseDouble(temp[0]);
        double pOfNo = Double.parseDouble(temp[1]);

        for(String term : terms){

            String value = probBigram.get(term);
            if(value == null){
                value = probBigram.get(bigram.stem.Nbtrain.LABEL_P_DEFAULT);
            }

            String[] val = value.split(SEPARATOR);

            double termOverYes = Double.parseDouble(val[0]);
            double termOverNo = Double.parseDouble(val[1]);

            yes = yes + Math.log(termOverYes);
            no = no + Math.log(termOverNo);

        }
        yes = yes + Math.log(pOfYes);
        no = no + Math.log(pOfNo);


        return yes >= no ? "YES" : "NO";
    }

    private String predictUni(Path file) throws IOException {
        List<String> lines = Files.lines(file).collect(Collectors.toList());
        Set<String> terms = new HashSet<>();
        for(String line : lines){


            try {

                String stemmed = parser.parse(QueryParserUtil.escape(line)).toString(stem.DirectoryTreeWalker.FIELD_TERM);
                stemmed = TextUtils.removeStopWords(stemmed);
                String[] t =  stemmed.split(" ");
                Arrays.stream(t).filter(term -> !term.isEmpty()).forEach(terms::add);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        double yes = 0;
        double no = 0;

        String[] temp = probUnigram.get(stem.Nbtrain.LABEL_P_YES_NO).split(SEPARATOR);
        double pOfYes =  Double.parseDouble(temp[0]);
        double pOfNo = Double.parseDouble(temp[1]);

        for(String term : terms){

            String value = probUnigram.get(term);
            if(value == null){
                value = probUnigram.get(stem.Nbtrain.LABEL_P_DEFAULT);
            }

            String[] val = value.split(SEPARATOR);

            double termOverYes = Double.parseDouble(val[0]);
            double termOverNo = Double.parseDouble(val[1]);

            yes = yes + Math.log(termOverYes);
            no = no + Math.log(termOverNo);

        }
        yes = yes + Math.log(pOfYes);
        no = no + Math.log(pOfNo);


        return (yes >= no) ? "YES" : "NO";
    }


}
