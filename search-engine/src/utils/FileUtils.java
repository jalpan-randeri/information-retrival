package utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

/**
 * File utilities
 */
public class FileUtils {

    /**
     * read content of file
     * @param fileName String file name
     * @return Stream of string
     * @throws IOException if file is found or accessible
     */
    public static Stream<String> readFiles(String fileName) throws IOException {
        return  Files.lines(Paths.get(fileName));

    }

    /**
     * write the content of file
     * @param fileName String file name
     * @param content List of string as contnet
     * @throws FileNotFoundException if file is not accessible
     */
    public static void writeFile(String fileName, List<String> content) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(fileName);
        content.stream()
               .forEach(writer::println);
        writer.close();
    }

    /**
     * add line to file
     * @param filename String file name
     * @param content  String content line
     */
    public static void appendToFile(String filename, String content) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(filename, true));
            writer.append(content);
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
