package utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * File utilities
 */
public class FileUtils {

    /**
     * read content of file
     * @param fileName String file name
     * @return List of string
     * @throws IOException if file is found or accessible
     */
    public List<String> readFiles(String fileName) throws IOException {
        return  Files.lines(Paths.get(fileName, "UTF-8"))
                     .collect(Collectors.toList());
    }

    /**
     * write the content of file
     * @param fileName String file name
     * @param content List of string as contnet
     * @throws FileNotFoundException if file is not accessible
     */
    public void writeFile(String fileName, List<String> content) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(fileName);
        content.stream()
               .forEach(writer::println);
        writer.close();
    }
}
