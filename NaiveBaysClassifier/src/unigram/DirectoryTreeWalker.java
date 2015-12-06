package unigram;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
public class DirectoryTreeWalker extends SimpleFileVisitor<Path> {

    Map<String, Long> posTermFrequency;
    Map<String, Long> negTermFrequency;
    Map<String, Long> termFrequency;


    public DirectoryTreeWalker() {
        posTermFrequency = new HashMap<>();
        negTermFrequency = new HashMap<>();
        termFrequency = new HashMap<>();
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
            String[] contents = line.split(" ");
            Arrays.stream(contents).forEach(term -> {
                String key = term.toLowerCase().trim();


                if(!key.trim().isEmpty() && map.containsKey(key)){
                    map.put(key, map.get(key) + 1L);
                } else {
                    map.put(key, 1L);
                }
            });
        });

        return FileVisitResult.CONTINUE;
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
