package utils;

import model.PageModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.stream.Stream;

/**
 *
 */
public class FileWriter {
    /**
     * write file for top page rank
     * @param filename String file name
     * @param content Stream [PageModel] stream of Page Model
     * @throws FileNotFoundException
     */
    public static void writePageRankFile(String filename, Stream<PageModel> content) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(new File(filename));
        content.forEach(page -> writer.printf("%s \t %.20f\n", page.getPageId(), page.getRank()));
        writer.close();
    }


    /**
     * write file for top inlink page
     * @param filename String file name
     * @param content Stream [PageModel] stream of Page Model
     * @throws FileNotFoundException
     */
    public static void writeInlinkFile(String filename, Stream<PageModel> content) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(new File(filename));
        content.forEach(page -> writer.printf("%s \t %d\n", page.getPageId(), page.getInlinkCount()));
        writer.close();
    }


    /**
     * write perplexity into file
     * @param filename String file name
     * @param perplexity String perplexity
     * @throws FileNotFoundException
     */
    public static void writePerplexity(String filename, String perplexity) throws IOException {
        perplexity = perplexity + System.lineSeparator();
        Path filePath = Paths.get(filename);
        if (!Files.exists(filePath)) {
            Files.createFile(filePath);
        }

        Files.write(filePath, perplexity.getBytes(), StandardOpenOption.APPEND);
    }

    /**
     * append input into output file for question 1
     * @param filename String file name
     * @param line String content
     * @throws IOException
     */
    public static void appendOutputQuestion1(String filename, String line) throws IOException {
        Path filePath = Paths.get(filename);
        if (!Files.exists(filePath)) {
            Files.createFile(filePath);
        }
        Files.write(filePath, line.getBytes(), StandardOpenOption.APPEND);
    }


    /**
     * append page rank into output file for question 1
     * @param filename String file name
     * @param stream Stream[PageRank] String content
     * @throws IOException
     */
    public static void appendOutputQuestion1(String filename, Stream<Map.Entry<String, Double>> stream) throws
            IOException {
        Path filePath = Paths.get(filename);
        if (!Files.exists(filePath)) {
            Files.createFile(filePath);
        }
        stream.forEach(entry -> {
            try {
                Files.write(filePath,
                        String.format("%s \t %.20f\n", entry.getKey(), entry.getValue()).getBytes(),
                        StandardOpenOption.APPEND);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
