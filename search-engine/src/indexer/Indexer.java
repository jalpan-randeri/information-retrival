package indexer;

import utils.FileUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Indexer will generate inverted indices for search engine
 */
public class Indexer {
    public static void main(String[] args) throws IOException {

        if(args.length == 2) {
            Map<String, Map<String, Long>> invertedIndex = getInvertedIndex(args[0]);
            invertedIndex.entrySet()
                    .forEach(entry -> {
                        String line = getFlatten(entry);
                        try {
                            FileUtils.appendToFile(args[1], line);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } else {
            System.out.println("Usage: input_file output_file");
        }
    }

    /**
     * flatten the entry in map
     * @param entry Map.Entry {docId, inverted index}
     * @return String representation of entry
     */
    private static String getFlatten(Map.Entry<String, Map<String, Long>> entry){
        StringBuilder builder = new StringBuilder();
        builder.append(entry.getKey());
        builder.append("[");
        for(Map.Entry<String, Long> e : entry.getValue().entrySet()){
            builder.append(String.format("{%s, %d}, ",e.getKey(), e.getValue()));
        }
        builder.append("]");
        return builder.toString();
    }


    /**
     * build inverted index from stemmed documents collection
     * @param inputFile String input file
     * @return Map of {Term, List of Document and Frequency}
     * @throws IOException when input file is not accessible
     */
    private static Map<String, Map<String, Long>> getInvertedIndex(String inputFile) throws IOException {
        HashMap<String, Map<String, Long>> cache = new HashMap<>();


        Iterator<String> reader = FileUtils.readFiles(inputFile).iterator();
        String lastDocId = null;
        while(reader.hasNext()){
            String line = reader.next();

            if(isDocument(line)){
                lastDocId = line.trim();

            }else if(lastDocId != null){

                List<String> terms = getAllTerms(line);
                for(String term : terms){
                    // if term already present in index
                    Map<String, Long> index;
                    if(cache.containsKey(term)) {
                        index = cache.get(term);
                        if (index.containsKey(lastDocId)) {
                            index.put(lastDocId, index.get(lastDocId) + 1L);
                        } else {
                            index.put(lastDocId, 1L);
                        }
                    }else {
                        index = new HashMap<>();
                        index.put(lastDocId, 1L);
                    }
                    cache.put(term, index);

                }

            }
        }

        return cache;
    }

    /**
     * get all terms
     * @param line String line
     * @return list of term
     */
    private static List<String> getAllTerms(String line){
        return Arrays.stream(line.split(" "))
                     .filter(Indexer::isNotNumber)
                     .collect(Collectors.toList());
    }




    /**
     * checks validity of documents
     * @param line String line
     * @return true if given document line starts with #
     */
    private static boolean isDocument(String line) {
        return line.startsWith("#");
    }


    /**
     * is not Number
     * @param word String word
     * @return true if given word is not number
     */
    private static boolean isNotNumber(String word){
        return !isDigit(word);
    }

    /**
     * checks for given word contains only digits ?
     * @param word String word
     * @return true if all characters in word are digit
     */
    private static boolean isDigit(String word) {
        return word.chars().allMatch(c ->  c >= '0' && c <= '9');
    }
}
