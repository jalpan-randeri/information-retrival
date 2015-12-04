package stem;

import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
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

        posTermFrequency = posTermFrequency.entrySet()
                .stream()
                .filter(entry -> entry.getValue() > 16)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        negTermFrequency = negTermFrequency.entrySet()
                .stream()
                .filter(entry -> entry.getValue() > 16)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));


        termFrequency = new HashMap<>(posTermFrequency);
        for(Map.Entry<String, Long> entry : negTermFrequency.entrySet()){
            if(termFrequency.containsKey(entry.getKey())){
                termFrequency.put(entry.getKey(), termFrequency.get(entry.getKey()) + entry.getValue());
            }else{
                termFrequency.put(entry.getKey(), entry.getValue());
            }
        }



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
