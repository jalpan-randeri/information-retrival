package utils;

import crawler.FocusedCrawler;
import org.jsoup.nodes.Document;

/**
 * Document Checker checks the validity of document
 */
public class DocumentChecker {


    public static final String VISIBLE_TEXT_SELECTOR = "div#mw-content-text";



    /**
     * checks validity of document
     * @param level integer depth of document
     * @param doc Document
     * @param keyword String keyword
     * @return True iff current doc is at seed level
     *                 OR doc is within MAX_DEPTH AND keyword present in doc
     */
    public static boolean isValidDocument(int level, Document doc, String keyword){
        return isSeedLevel(level) || (isValidLevel(level) && isKeywordPresent(doc, keyword));
    }

    /**
     * is Seed Level
     * @param level Integer level document
     * @return True iff level = SEED_LEVEL
     */
    private static boolean isSeedLevel(int level){
        return FocusedCrawler.SEED_LEVEL == level;
    }

    /**
     * is valid level
     * @param level Integer level of document
     * @return True iff document level  < MAX_DEPTH
     */
    private static boolean isValidLevel(int level){
        return level <= FocusedCrawler.MAX_DEPTH;
    }

    /**
     * is keyword present
     * @param doc Document DOM content of page
     * @param keyword String keyword phrase
     * @return True iff keyword is null OR present inside document
     */
    private static boolean isKeywordPresent(Document doc, String keyword) {
        return keyword == null
                || doc.select(VISIBLE_TEXT_SELECTOR)
                      .text()
                      .toLowerCase()
                      .contains(keyword.toLowerCase());
    }
}
