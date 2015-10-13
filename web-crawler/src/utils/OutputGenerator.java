package utils;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.List;

/**
 * Output Generator will save the output file
 */
public class OutputGenerator {

    private static final Logger logger = Logger.getLogger(OutputGenerator.class);

    /**
     * write output file
     * @param filename String filename
     * @param content List of String as links that were crawled
     */
    public static void writeOutputFile(String filename, List<String> content) {
        try {
            File file = new File(filename);
            Writer w = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            PrintWriter writer = new PrintWriter(w);
            for(String s : content){
                writer.println(s);
            }
            writer.close();
        }catch (IOException e){
            logger.error(e);
        }
    }
}
