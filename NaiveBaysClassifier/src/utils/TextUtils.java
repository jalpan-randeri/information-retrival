package utils;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class TextUtils {

    public static String removeStopWords(String textFile) throws Exception {
        CharArraySet stopWords = EnglishAnalyzer.getDefaultStopSet();
        TokenStream tokenStream = new StandardTokenizer(Version.LUCENE_47, new StringReader(textFile.trim()));

        tokenStream = new StopFilter(Version.LUCENE_47, tokenStream, stopWords);
        StringBuilder sb = new StringBuilder();
        CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
        tokenStream.reset();
        while (tokenStream.incrementToken()) {
            String term = charTermAttribute.toString();
            sb.append(term + " ");
        }
        return sb.toString();
    }



    public static List<String> permute(List<String> tokens){
        List<String> list = new ArrayList<>();
        permuteHelper(tokens, list,  "");
        return list;
    }

    private static void permuteHelper(List<String> base, List<String> result,  String acc){
        int length = base.size();
        if(length == 0){
            result.add(acc.trim());
        }else{
            for(int i = 0; i < length; i++){
                String s = base.get(i);
                List<String> prefix = base.subList(0, i);
                List<String> suffix = base.subList(i + 1, length);
                List<String> combined = new ArrayList<>(prefix);
                combined.addAll(suffix);

                permuteHelper(combined, result, acc + s +" ");
            }
        }
    }

}
