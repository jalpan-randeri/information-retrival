import app.PageRank;

import java.io.IOException;

/**
 * Driver program for Page rank
 */
public class Main {

    public static void main(String[] args) throws IOException {
        if(args.length == 1) {
            PageRank.computePageRank(args[0]);
        }else{
            System.out.println("Invalid Arguments : Usage  Main  <filename> ");
            System.exit(1);
        }
    }
}
