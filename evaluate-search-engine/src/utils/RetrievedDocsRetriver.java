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
public class RetrievedDocsRetriver {

    public static List<String> getDocumentForQuery(String query, String path) {
        List<String> docs = new ArrayList<>();
        try {
            List<String> lines = Files.lines(Paths.get(path)).collect(Collectors.toList());
            int startPos = getStartingPosition(lines, query);
            for(int i = startPos + 1; i < lines.size(); i++){
                if (lines.get(i).startsWith("Query")) {
                    break;
                }

                docs.add(lines.get(i));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return docs;
    }


    private static int getStartingPosition(List<String> list, String query){
        for(int i = 0; i < list.size(); i++){
            if(list.get(i).contains(query)){
                return i;
            }
        }

        throw new IllegalStateException("query not found");
    }
}
