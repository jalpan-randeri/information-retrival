import org.apache.lucene.document.Document;

import java.io.InputStream;

/**
 *
 */
public interface DocumentHandler {
    Document getDocument(InputStream is);
}
