package crawler;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * File walker will walk the directory tree
 */
public class DirectoryWalker extends SimpleFileVisitor<Path> {

    private Queue<Path> queue = new ArrayDeque<>();


    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

        String filename = file.getFileName().toString();
        if (filename.endsWith(".htm")
                || filename.endsWith(".html")
                || filename.endsWith(".xml")
                || filename.endsWith(".txt")) {
            queue.add(file);
        }
        return FileVisitResult.CONTINUE;
    }

    public Queue<Path> getQueue(){
        return queue;
    }
}
