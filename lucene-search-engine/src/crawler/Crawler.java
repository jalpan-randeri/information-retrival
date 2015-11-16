package crawler;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Crawler process retrieves document and clean it
 * for all the unnecessary words such as digits and html tags
 */
public class Crawler {



    public static void main(String[] args) throws IOException {
        // 1. read the input directory
        //   1.1 for each file in this directory
        //       read it and remove unwanted contents
        // 2. add it to term frequency
        // 3. add content to  to lucene index



        final Path sourceDir = Paths.get("cacm");

        Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        System.out.printf("Visited file %s%n", file.getFileName());

                        return FileVisitResult.CONTINUE;
                    }
                });

    }

}
