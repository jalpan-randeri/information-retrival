package indexer;

import model.InvertedIndex;
import utils.FileUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Indexer will generate inverted indices for search engine
 */
public class Indexer {
    public static final String DOC_LENGTH_SEPRATOR = "=================";
    public static final String SEPRATOR_SPACE = " ";

    public static void main(String[] args) throws IOException {

        if(args.length == 2) {
            Map<String, Map<String, Long>> invertedIndex = getInvertedIndex(args[0]);
            invertedIndex.entrySet()
                    .forEach(entry -> {
                        String line = serializeIndexEntry(entry);
                        FileUtils.appendToFile(args[1], line);
                    });


            FileUtils.appendToFile(args[1], DOC_LENGTH_SEPRATOR);
            Map<String, Long> documentAndLength = getDocumentAndLength(args[0]);
            documentAndLength.entrySet().forEach(entry -> {
                String doc = entry.getKey();
                Long length = entry.getValue();
                FileUtils.appendToFile(args[1],String.format("%s=%d", doc, length));
            });

        } else {
            System.out.println("Usage: input_file output_file");
        }
    }


    private static Map<String, Long> getDocumentAndLength(String stemmedFile) throws IOException {
        HashMap<String, Long> documentLength = new HashMap<>();

        Stream<String> stemmed = FileUtils.readFiles(stemmedFile);
        Iterator<String> reader = stemmed.iterator();
        String lastDocId = null;
        while(reader.hasNext()){
            String line = reader.next();
            if(isDocument(line)){
                lastDocId =  line.replace('#',' ').trim();
                documentLength.put(lastDocId, 0L);
            }else{
                long length = line.split(SEPRATOR_SPACE).length;
                if(documentLength.containsKey(lastDocId)) {
                    documentLength.put(lastDocId, documentLength.get(lastDocId) + length);
                } else {
                    documentLength.put(lastDocId, length);
                }
            }
        }

        return documentLength;
    }



    /**
     * flatten the entry in map
     * @param entry Map.Entry {docId, inverted index}
     * @return String representation of entry
     */
    public static String serializeIndexEntry(Map.Entry<String, Map<String, Long>> entry){
        StringBuilder builder = new StringBuilder();
        builder.append(entry.getKey());
        builder.append("[");
        for(Map.Entry<String, Long> e : entry.getValue().entrySet()){
            builder.append(String.format("%s=%d ",e.getKey(), e.getValue()));
        }
        builder.append("]");
        return builder.toString();
    }


    /**
     * Deserialized string into inverted index entry
     * @param serializedEntry String serialized form of inverted index
     * @return Inverted Index entry
     */
    public static InvertedIndex deserializeEntry(String serializedEntry){
        String term = serializedEntry.substring(0, serializedEntry.indexOf('[')).trim();
        String entry = serializedEntry.substring(serializedEntry.indexOf('['));
        entry = entry.replace('[',' ').replace(']',' ').trim();
        String[] indices = entry.split(" ");
        Map<String, Long> map = Arrays.stream(indices)
                                      .map(index -> index.split("="))
                                      .collect(Collectors.toMap(tokens -> tokens[0],
                                              tokens -> Long.parseLong(tokens[1])));

        return new InvertedIndex(term, map);
    }



    /**
     * build inverted index from stemmed documents collection
     * @param inputFile String input file
     * @return Map of {Term, List of Document and Frequency}
     * @throws IOException when input file is not accessible
     */
    private static Map<String, Map<String, Long>> getInvertedIndex(String inputFile) throws IOException {
        Map<String, Map<String, Long>> cache = new TreeMap<>();


        Iterator<String> reader = FileUtils.readFiles(inputFile).iterator();
        String lastDocId = null;
        while(reader.hasNext()){
            String line = reader.next();

            if(isDocument(line)){
                lastDocId = line.replace('#',' ').trim();

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
                        index = new TreeMap<>();
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
        return Arrays.stream(line.split(SEPRATOR_SPACE))
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
