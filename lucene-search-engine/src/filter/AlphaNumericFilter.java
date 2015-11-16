package filter;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.jsoup.Jsoup;

import java.io.IOException;

/**
 *
 */
public class AlphaNumericFilter extends TokenFilter {

    public AlphaNumericFilter(TokenStream input) {
        super(input);
    }

    @Override
    public boolean incrementToken() throws IOException {
        String nextToken = null;

        while(nextToken == null) {
            // reached end of stream
            if(!this.input.incrementToken()){
                return false;
            }

            // Get text of the current token and remove any
            // leading/trailing whitespace.
            String currentTokenInStream = this.input
                    .getAttribute(CharTermAttribute.class).toString().toLowerCase().trim();

            // Save the token if it is not an empty string
            if (isAlphaNumeric(currentTokenInStream)) {
                nextToken = currentTokenInStream;
            }

        }

        return true;
    }


    private static boolean isAlphaNumeric(String token){

        boolean isAllDigit = true;
        for(char c : token.toCharArray()){
            if(c >= '0' && c <= '9'){
                isAllDigit = true;
            }else{
                isAllDigit = false;
                break;
            }
        }

        return !isAllDigit;
    }

}
