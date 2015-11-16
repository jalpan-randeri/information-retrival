package crawler;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

/**
 * File walker will walk the directory tree
 */
public class DirectoryWalker extends SimpleFileVisitor<Path> {

    private Map<String, Long> termFrequency = new HashMap<>();


    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {



        return FileVisitResult.CONTINUE;
    }


}
