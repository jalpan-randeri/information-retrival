package analyzer;

import filter.AlphaNumericFilter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.util.Version;
import tokenizer.AlphaNumericTokenizer;

import java.io.Reader;

/**
 *
 */
public class HtmlAnalyzer extends Analyzer {


    @Override
    protected TokenStreamComponents createComponents(String s, Reader reader) {

        Tokenizer tokenizer = new AlphaNumericTokenizer(Version.LUCENE_47, reader);
        TokenStream filter = new AlphaNumericFilter(tokenizer);
        filter = new LowerCaseFilter(Version.LUCENE_47, filter);

        return new TokenStreamComponents(tokenizer, filter);
    }
}
