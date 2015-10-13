package utils;

/**
 * LinkModel Checker validates the links
 */
public class LinkChecker {

    private static final String DOMAIN_WIKIPEDIA = "//en.wikipedia.org/wiki/";
    private static final String ADMIN_PAGES = ":";
    private static final String WIKI_HOME_PAGE = "//en.wikipedia.org/wiki/Main_Page";

    /**
     * checks the validity of the link by against this conditions
     *  - absolute link should be from wikipedia domain
     *  - absolute link is not equal to current link
     *  - relative link is not wikipedia admin pages (link does not contains ':')
     *  - absolute link should not going to crawler.Main Page of wikipedia
     * @param absoluteLink String absolute link
     * @param relativeLink String relative link
     * @param currentLink String current document link
     * @return true if link passes all validation checks
     */
    public static boolean isValidLink(String absoluteLink, String relativeLink, String currentLink) {
        return  absoluteLink.contains(DOMAIN_WIKIPEDIA)
                && !relativeLink.contains(ADMIN_PAGES)
                && !absoluteLink.contains(currentLink)
                && !absoluteLink.contains(WIKI_HOME_PAGE);
    }
}
