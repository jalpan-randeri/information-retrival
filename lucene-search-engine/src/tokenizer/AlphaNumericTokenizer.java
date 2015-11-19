package tokenizer;

import org.apache.lucene.analysis.util.CharTokenizer;
import org.apache.lucene.util.Version;

import java.io.Reader;

/**
 * Alphanumeric tokenizer will tokenize corpus if it is alphanumeric
 * it will remove other tokens
 */
public class AlphaNumericTokenizer extends CharTokenizer {


    public AlphaNumericTokenizer(Version matchVersion, Reader input) {
        super(matchVersion, input);
    }


    @Override
    protected boolean isTokenChar(int c) {
        return Character.isDigit(c)
                || Character.isAlphabetic(c);
    }
}
