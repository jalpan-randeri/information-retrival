package crawler;

import model.LinkModel;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import utils.DocumentChecker;
import utils.LinkChecker;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;

/**
 * Focused Crawler
 */
public class FocusedCrawler {

    public static final int SEED_LEVEL = 1;
    public static final int POLITENESS = 1000;
    public static final int MAX_DEPTH = 5;
    public static final int MAX_SIZE = 10000;

    public static final String SELECTOR_ANCHOR = "a[href]";
    public static final String ANCHOR_ABSOLUTE = "abs:href";
    public static final String ANCHOR_RELATIVE = "href";
    public static final String FRAGMENT_IDENTIFIER = "#";


    private static final Logger logger = Logger.getLogger(FocusedCrawler.class);
    public static final int DELAY_TIMEOUT = 1000;

    /**
     * crawl without keyword
     * @param seedPage String seed page
     * @return Set of Links which are crawled
     */
    public static Set<String> crawlWithoutKeyword(String seedPage){
        LinkModel seed = new LinkModel(SEED_LEVEL, seedPage);
        return crawl(seed, null);
    }

    /**
     * crawl with keyword
     * @param seedPage String seed page
     * @param keyword String keyword
     * @return Set of Links which are crawled
     */
    public static Set<String> crawlWithKeyword(String seedPage, String keyword){
        LinkModel seed = new LinkModel(SEED_LEVEL, seedPage);
        return crawl(seed, keyword);
    }

    /**
     * crawl the web
     * @param seed LinkModel seed document
     * @param keyword String keyword
     * @return Set of String as unique crawled links
     */
    private static Set<String> crawl(LinkModel seed, String keyword) {

        // initialize queue
        Queue<LinkModel> frontier = new ArrayDeque<LinkModel>(MAX_SIZE);
        HashSet<String> visited = new HashSet<String>(MAX_SIZE);
        Set<String> crawledLinks = new HashSet<String>(MAX_SIZE);


        // start with seed document
        frontier.add(seed);

        while(!frontier.isEmpty() && crawledLinks.size() < MAX_SIZE){

            LinkModel currentLink = frontier.remove();

            // retrieve document from link
            Document doc = getDocument(currentLink.getLink());

            if(doc != null) {
                // save to visited
                visited.add(doc.location());

                // process document
                List<LinkModel> links = processDocument(keyword, frontier, visited, crawledLinks, currentLink, doc);

                // insert links extracted from document
                frontier.addAll(links);


                // politeness
                try {
                    Thread.sleep(POLITENESS);
                }catch (InterruptedException e){
                    logger.error("Interrupted", e);
                }
            }
        }
        logger.info(String.format("Total Number of links discovered " +
                "%d and total number of links crawled %d", visited.size(), crawledLinks.size()));

        return crawledLinks;
    }

    /**
     * process document will looks for the document validity and extracts the links from it
     * @param keyword String keyword phrase
     * @param frontier Queue of LinkModel
     * @param visited Set of String as links that are already visited
     * @param crawledLinks List of String as links that were crawled
     * @param currentLink LinkModel current link and its level
     * @param doc Document the DOM content of current document
     * @return List of LinkModel as the valid links that were extracted from current document
     */
    private static List<LinkModel> processDocument(String keyword,
                                                   Queue<LinkModel> frontier,
                                                   HashSet<String> visited,
                                                   Set<String> crawledLinks,
                                                   LinkModel currentLink,
                                                   Document doc) {
        // check whether link contains keyword phrase?
        if(DocumentChecker.isValidDocument(currentLink.getLevel(), doc, keyword)) {
            if(!crawledLinks.contains(doc.location())) {
                crawledLinks.add(doc.location());
                logger.info(String.format("%4d %2d  %s",crawledLinks.size(),
                                                        currentLink.getLevel(),
                                                        currentLink.getLink()));
            }

            return getValidLinksFromDocument(frontier, visited, currentLink.getLevel(), doc);
        }

        return new ArrayList<LinkModel>();
    }

    /**
     * get Document
     * @param currentLink String link url
     * @return Document DOM content of link
     */
    private static Document getDocument(String currentLink) {
        Document doc = null;
        try {
            doc = Jsoup.connect(currentLink).timeout(DELAY_TIMEOUT).get();
        } catch (MalformedURLException e){
            logger.error("Incorrect url syntax ", e);
        } catch (IOException e){
            logger.error("Unable to get document ", e);
        }

        return doc;
    }

    /**
     * Get valid links from document
     * @param frontier Queue of links that are not yet visited
     * @param visited Set of links that are already visited
     * @param level Integer depth of the current document
     * @param doc Document DOM content of current page
     * @return List of Links which are being validated
     */
    private static List<LinkModel> getValidLinksFromDocument(Queue<LinkModel> frontier,
                                                             HashSet<String> visited,
                                                             int level,
                                                             Document doc) {
        List<LinkModel> list = new ArrayList<LinkModel>(MAX_SIZE);

        // determine all links on the current page
        for (Element link : doc.select(SELECTOR_ANCHOR)) {

            // get link
            String absolute = normalizeLink(link.attr(ANCHOR_ABSOLUTE));
            String rel = link.attr(ANCHOR_RELATIVE);


            // check for the validity of links
            if (!visited.contains(absolute)
                    && !frontier.contains(new LinkModel(-1, absolute))
                    && LinkChecker.isValidLink(absolute, rel, doc.location())
                    && level  < MAX_DEPTH) {

                LinkModel l = new LinkModel(level + 1, absolute);
                list.add(l);
            }
        }
        return list;
    }


    /**
     * Normalize link
     * @param link String
     * @return String normalized link
     */
    private static String normalizeLink(String link) {
        if (link.contains(FRAGMENT_IDENTIFIER)) {
            link = link.substring(0, link.indexOf(FRAGMENT_IDENTIFIER));
        }
        return link;
    }

}
