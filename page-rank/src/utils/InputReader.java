package utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 *
 */
public class InputReader {

    public static Stream<String> readInputFile(String file) throws IOException {
        return Files.lines(Paths.get(file));
    }
}
