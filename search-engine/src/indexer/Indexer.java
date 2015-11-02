package indexer;

import model.DocumentModel;
import utils.FileUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Indexer will generate inverted indices for search engine
 */
public class Indexer {
    public static void main(String[] args) throws IOException {

        if(args.length == 2) {
            Map<String, List<DocumentModel>> invertedIndex = getInvertedIndex(args[0]);
            invertedIndex.entrySet().forEach(entry -> {
                        String line = getFlattern(entry);
                        try {
                            FileUtils.appendToFile(args[1], line);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
            );
        }else{
            System.out.println("Usage: inputfile outputfile");
        }
    }


    private static String getFlattern(Map.Entry<String, List<DocumentModel>> entry){
        return String.format("%s %s",entry.getKey(), entry.getValue());
    }



    private static Map<String, List<DocumentModel>> getInvertedIndex(String inputFile) throws IOException {
        HashMap<String, List<DocumentModel>> cache = new HashMap<>();
        Map<String, Long> termFrequencyCache = getTermFrequency(FileUtils.readFiles(inputFile));

        Iterator<String> reader = FileUtils.readFiles(inputFile).iterator();
        String lastDocId = null;
        while(reader.hasNext()){
            String line = reader.next();

            if(isDocument(line)){
                lastDocId = line.trim();

            }else if(lastDocId != null){
                DocumentModel document = new DocumentModel(lastDocId, termFrequencyCache.get(lastDocId));

                List<String> terms = getAllTerms(line);

                for(String term : terms){
                    List<DocumentModel> list;
                    if(cache.containsKey(term)){
                        list = cache.get(term);
                    }else {
                        list = new ArrayList<>();
                    }
                    list.add(document);
                    cache.put(term, list);
                }

            }
        }

        return cache;
    }


    private static List<String> getAllTerms(String line){
        return Arrays.stream(line.split(" "))
                     .filter(Indexer::isWord)
                     .collect(Collectors.toList());
    }


    private static Map<String, Long> getTermFrequency(Stream<String> docTerms){
        HashMap<String, Long> cacheTermFrequency = new HashMap<>();

        Iterator<String> reader = docTerms.iterator();
        String lastDoc = null;
        while(reader.hasNext()){
            String line = reader.next();

            if(isDocument(line)){
                // add document with 0 term
                lastDoc = line.trim();
                cacheTermFrequency.put(lastDoc, 0L);

            }else if(lastDoc != null) {
                long count = getTotalTermsInDocument(line);
                cacheTermFrequency.put(lastDoc, cacheTermFrequency.get(lastDoc) + count);
            }
        }

        return cacheTermFrequency;
    }

    private static boolean isDocument(String line) {
        return line.startsWith("#");
    }

    private static long getTotalTermsInDocument(String line) {
        return Arrays.stream(line.split(" "))
                     .filter(Indexer::isWord)
                     .count();
    }

    private static boolean isWord(String word){
        return !isDigit(word);
    }

    private static boolean isDigit(String word) {
        return word.chars().allMatch(c ->  c >= '0' && c <= '9');
    }
}
