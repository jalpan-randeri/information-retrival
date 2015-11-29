package utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class RelevantDocRetriever {

    public static List<String> getDocumentsForQuery(String queryId, String path){
        List<String> list = new ArrayList<>();
        try {
            list =  Files.lines(Paths.get(path))
                    .filter(line -> isSameQuery(line, queryId))
                    .map(RelevantDocRetriever::retrieveDoc)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    private static boolean isSameQuery(String queryLine, String queryId){
        return queryLine.split(" ")[0].equals(queryId);
    }

    private static String retrieveDoc(String queryLine){
        return queryLine.split(" ")[2];
    }

}
