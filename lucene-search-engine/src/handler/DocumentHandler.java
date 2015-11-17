package handler;

import org.apache.lucene.document.Document;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 *
 */
public interface DocumentHandler {
    String FIELD_CONTENTS = "contents";
    String FIELD_PATH = "path";
    String FIELD_FILE_NAME = "filename";


    Document getDocument(Path file) throws IOException;
}
