import app.PageRank;

import java.io.IOException;

/**
 *
 */
public class Main {
        public static String FILE = /*"ip.txt"; */ "wt2g_inlinks.txt";
    public static void main(String[] args) throws IOException {
        PageRank.computePageRank(FILE);
    }
}
