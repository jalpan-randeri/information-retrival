package crawler;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Queue;

/**
 * Crawler process retrieves document and clean it
 * for all the unnecessary words such as digits and html tags
 */
public class Crawler {

    public Queue<Path> crawlDirectory(String source) throws IOException {
        Path sourceDir = Paths.get(source);
        DirectoryWalker walker = new DirectoryWalker();
        Files.walkFileTree(sourceDir, walker);
        return walker.getQueue();
    }


}
