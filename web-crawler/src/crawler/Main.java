package crawler;

import utils.OutputGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * Driver Program for Focused Crawler
 */
public class Main {

    public static void main(String[] args)  {

        List<String> links = new ArrayList<String>();
        String outputFileName = null;
        switch (args.length){
            case 1:
                links.addAll(FocusedCrawler.crawlWithoutKeyword(args[0]));
                outputFileName = "crawled_links_without_keyword.txt";
                break;
            case 2:
                links.addAll(FocusedCrawler.crawlWithKeyword(args[0], args[1]));
                outputFileName = "crawled_links_with_keyword_timeout.txt";
                break;
            default:
                System.out.println("Usage: Main seed-doc [keyword]");
                System.exit(0);
        }

        OutputGenerator.writeOutputFile(outputFileName, links);

    }



}
