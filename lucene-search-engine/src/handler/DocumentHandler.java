package handler;

import org.apache.lucene.document.Document;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Document handler interface for handling html documents
 */
public interface DocumentHandler {
    String FIELD_CONTENTS = "contents";
    String FIELD_PATH = "path";
    String FIELD_FILE_NAME = "filename";

    /**
     * Read file and convert it to document
     * @param file Path to file
     * @return Document
     * @throws IOException
     */
    Document getDocument(Path file) throws IOException;
}
