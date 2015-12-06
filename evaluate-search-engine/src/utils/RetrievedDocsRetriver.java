package utils;

import model.Document;

import javax.print.Doc;
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

    public static List<Document> getDocumentForQuery(String query, String path) {
        List<Document> docs = new ArrayList<>();
        try {
            List<String> lines = Files.lines(Paths.get(path)).collect(Collectors.toList());
            int startPos = getStartingPosition(lines, query);
            for(int i = startPos + 1; i < lines.size(); i++){
                if (lines.get(i).startsWith("Query,")) {
                    break;
                }


                String[] tokens = lines.get(i).split(",");
                docs.add(new Document(tokens[0], Double.parseDouble(tokens[1])));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return docs;
    }


    private static int getStartingPosition(List<String> list, String query){
        for(int i = 0; i < list.size(); i++){
            if(list.get(i).startsWith("Query,")){
                String[] tokens = list.get(i).split(",");

                if(tokens[1].trim().equals(query)) {
                    return i;
                }
            }
        }

        throw new IllegalStateException("query not found");
    }
}
