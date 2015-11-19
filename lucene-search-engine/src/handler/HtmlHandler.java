package handler;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexableField;
import org.jsoup.Jsoup;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * HTML handler will handles html files
 */
public class HtmlHandler implements DocumentHandler {

    @Override
    public Document getDocument(Path file) throws IOException {
        String contents = Jsoup.parse(file.toFile(), String.valueOf(StandardCharsets.UTF_8)).text();

        Document doc = new Document();
        doc.add(new StringField(FIELD_FILE_NAME, file.getFileName().toString(), Field.Store.YES));
        doc.add(new StringField(FIELD_PATH, file.toAbsolutePath().toString(), Field.Store.YES));
        doc.add(new TextField(FIELD_CONTENTS, contents, Field.Store.YES));

        return doc;
    }
}
