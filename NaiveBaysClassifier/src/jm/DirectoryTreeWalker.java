package jm;

import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.flexible.standard.QueryParserUtil;
import org.apache.lucene.util.Version;
import utils.TextUtils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
public class DirectoryTreeWalker extends SimpleFileVisitor<Path> {

    public static final String FIELD_TERM = "contents";

    private Map<String, Long> posTermFrequency;
    private Map<String, Long> negTermFrequency;
    private Map<String, Long> termFrequency;
    private QueryParser parser;

    public DirectoryTreeWalker() {
        posTermFrequency = new HashMap<>();
        negTermFrequency = new HashMap<>();
        termFrequency = new HashMap<>();
        EnglishAnalyzer analyzer =  new EnglishAnalyzer(Version.LUCENE_47);
        parser  = new QueryParser(Version.LUCENE_47, FIELD_TERM, analyzer);
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Map<String, Long> map;
        if(file.toString().contains("pos")){
            map = posTermFrequency;
        }else{
            map = negTermFrequency;
        }





        Files.lines(file).forEach(line -> {

            try {

                String[] contents = stemmLine(line);
                Arrays.stream(contents).forEach(term -> {
                    String key = term.toLowerCase().trim();

                    if(map.containsKey(key)){
                        map.put(key, map.get(key) + 1L);
                    } else {
                        map.put(key, 1L);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }



        });

        return FileVisitResult.CONTINUE;
    }

    private String[] stemmLine(String line) throws Exception {
        String stemmed = parser.parse(QueryParserUtil.escape(line)).toString(FIELD_TERM);
        stemmed = TextUtils.removeStopWords(stemmed);
        return stemmed.split(" ");
    }




    private void pruneInvalidTerms(){
        Map<String, Long> map = new HashMap<>(posTermFrequency);

        for(Map.Entry<String, Long> entry : negTermFrequency.entrySet()){
            if(map.containsKey(entry.getKey())){
                map.put(entry.getKey(), map.get(entry.getKey()) + entry.getValue());
            }else{
                map.put(entry.getKey(), entry.getValue());
            }
        }


        termFrequency = map.entrySet()
           .stream()
           .filter(entry -> entry.getValue() >= 5)
           .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));


        map.entrySet()
            .stream()
            .filter(entry -> entry.getValue() < 5)
            .forEach(entry -> {
                posTermFrequency.remove(entry.getKey());
                negTermFrequency.remove(entry.getKey());
            });


    }

    public Map<String, Long> getTermFrequency(){
        pruneInvalidTerms();
        return termFrequency;
    }

    public Map<String, Long> getPosTermFrequency(){
        pruneInvalidTerms();
        return posTermFrequency;
    }

    public Map<String, Long> getNegTermFrequency(){
        pruneInvalidTerms();
        return negTermFrequency;
    }

}
