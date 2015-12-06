package utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class RelevantDocRetriever {

    public static List<String> getDocumentsForQuery(String queryId, String path){

        try {
            List<String> file = Files.lines(Paths.get(path)).collect(Collectors.toList());
            List<String> validLines = file.stream()
                                          .filter(line -> isSameQuery(line, queryId))
                                          .collect(Collectors.toList());

            return validLines.stream()
                    .map(RelevantDocRetriever::retrieveDoc)
                    .collect(Collectors.toList());


        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static boolean isSameQuery(String queryLine, String queryId){
        return queryLine.split(" ")[0].trim().equals(queryId);
    }

    private static String retrieveDoc(String queryLine){
        return queryLine.split(" ")[2];
    }

}
