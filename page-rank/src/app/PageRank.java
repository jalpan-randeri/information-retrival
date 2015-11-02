package app;

import model.PageModel;
import utils.FileWriter;
import utils.InputReader;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class PageRank {
    private static final double DAMPING_FACTOR = 0.85;
    private static final String SEPARATOR = " ";
    public static final String FILE_PAGE_RANK = "page_rank.txt";
    public static final String FILE_SORTED_PAGE_RANK = "sorted_page_rank.txt";
    public static final String FILE_TOP_50_PAGE_ON_PAGE_RANK = "top_50_page_on_page_rank.txt";
    public static final String FILE_TOP_50_PAGE_ON_INLINK_COUNT = "top_50_page_on_inlink_count.txt";


    /**
     * compute page ranks
     *
     * @param inputFile String file
     * @throws IOException
     */
    public static void computePageRank(String inputFile) throws IOException {

        // calculate size
        long graphSize = InputReader.readInputFile(inputFile).count();

        final double initialRank = 1.0 / graphSize;


        // initial page rank
        HashMap<String, Double> pageRank = (HashMap<String, Double>) getInitialPageRank(initialRank,
                                                                                        InputReader.readInputFile(inputFile));

        // find sinks
        Set<String> sinks = getSinks(pageRank.keySet(), InputReader.readInputFile(inputFile));

        // outlink
        HashMap<String, Integer> outlinks = getOutlinks(InputReader.readInputFile(inputFile));

        // computed page ranks
        HashMap<String, Double> ranks = pagerankHelper(inputFile, pageRank, sinks, outlinks);

        List<PageModel> finalPageRankList = ranks.entrySet()
                                                 .stream()
                                                 .map(e -> {
                                                    PageModel model = new PageModel();
                                                     model.setPageId(e.getKey());
                                                     model.setRank(e.getValue());
                                                     return model;
                                                 })
                                                 .collect(Collectors.toList());

        FileWriter.writePageRankFile(FILE_PAGE_RANK, finalPageRankList.stream());

        //NOTE: uncomment this to print output for question - 2
        printQuestion2Output(inputFile, graphSize, sinks, ranks);

    }

    /**
     * print question required output
     * @param inputFile String inputfile
     * @param graphSize long graph size
     * @param sinks Set[String] sinks
     * @param ranks Map[String, Double] pageRank
     * @throws IOException
     */
    private static void printQuestion2Output(String inputFile,
                                             long graphSize,
                                             Set<String> sinks,
                                             HashMap<String, Double> ranks) throws IOException {
        // sort the pages
        List<PageModel> sorted_ranks = ranks.entrySet()
                .parallelStream()
                .sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue()))
                .map(entry -> new PageModel(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        FileWriter.writePageRankFile(FILE_SORTED_PAGE_RANK, sorted_ranks.stream());

        // top 50 pages with highest page rank
        System.out.println("\nTop 50 Page based on Page Rank\n");
        List<PageModel> top50 = sorted_ranks.subList(0, 50);
        top50.stream().forEach(System.out::println);
        FileWriter.writePageRankFile(FILE_TOP_50_PAGE_ON_PAGE_RANK, top50.stream());

        // top 50 pages and their pagerank with highest inlinks count
        List<PageModel> top50inlinks = InputReader.readInputFile(inputFile)
                .map(line -> {
                    String[] page = line.split(SEPARATOR);
                    List<String> inlinks = Arrays.asList(page);
                    inlinks = inlinks.subList(1, inlinks.size());

                    PageModel model = new PageModel();
                    model.setPageId(page[0]);
                    model.setRank(ranks.get(page[0]));
                    model.setInlinkCount(inlinks.stream().distinct().count());

                    return model;
                })
                .sorted((o1, o2) -> o2.getInlinkCount().compareTo(o1.getInlinkCount()))
                .limit(50)
                .collect(Collectors.toList());

        System.out.println("\nTop 50 Page based on Inlink Count\n");
        top50inlinks.stream().forEach(e -> System.out.printf("%s %d\n",e.getPageId(), e.getInlinkCount()));
        FileWriter.writeInlinkFile(FILE_TOP_50_PAGE_ON_INLINK_COUNT, top50inlinks.stream());

        // sources proportion
        long source = InputReader.readInputFile(inputFile)
                                 .filter(line -> line.split(SEPARATOR).length == 1)
                                 .count();
        System.out.printf("Source Proportion %.2f\n",((double) source / graphSize));


        // sink proportion
        System.out.printf("Sink Proportion %.2f\n",((double) sinks.size()/ graphSize));

        final double INITIAL_RANK = 1.0 / graphSize;
        // proportion of pages who's rank is less than initial page rank
        long lessThanInitialPageRankCount = ranks.entrySet()
                                                 .parallelStream()
                                                 .filter(entry ->  entry.getValue() < INITIAL_RANK)
                                                 .count();

        System.out.printf("Less than initial page rank proportion %.2f\n",
                (double) lessThanInitialPageRankCount /graphSize);
    }

    /**
     * compute page rank
     *
     * @param inputFile String file name
     * @param pageRank  HashMap initial page rank
     * @param sinks     Set of sinks
     * @param outlinks  HashMap of outlinks
     * @return HashMap as page and their rank
     * @throws IOException
     */
    private static HashMap<String, Double> pagerankHelper(String inputFile,
                                                          HashMap<String, Double> pageRank,
                                                          Set<String> sinks,
                                                          HashMap<String, Integer> outlinks) throws IOException {


        Queue<Double> history = new ArrayDeque<>(4);

        double perplexity = perplexity(pageRank);
        System.out.printf("Perplexity \t %.20f\n",perplexity);

        // NOTE: Uncomment this for Question 1
//         int[] printIndexes = new int[]{ 1, 10, 100};
//         int index = 0;

        // NOTE: condition for question 2
        while (!isConverged(perplexity, history)) {

        // NOTE: condition for question 1
//        while (index < 100) {
//            index++;
            final double sink_pagerank = sinks.stream().mapToDouble(pageRank::get).sum();


            // must be exclusive final
            final HashMap<String, Double> finalPageRank = pageRank;
            pageRank = (HashMap<String, Double>) calculatePageRank(outlinks,
                                                                   sink_pagerank,
                                                                   finalPageRank,
                                                                   InputReader.readInputFile(inputFile));

            // NOTE: requirement for question 2
            perplexity = perplexity(pageRank);
            System.out.printf("Perplexity %.20f\n",perplexity);

            // NOTE: required condition for question 1
//            if(Arrays.binarySearch(printIndexes, index) >= 0){
//                System.out.println("Iteration "+index);
//                pageRank.entrySet().stream().forEach(System.out::println);
//            }

        }

        return pageRank;
    }

    /**
     * check if page rank converged
     *
     * @param perplexity double perplexity
     * @param history    Queue of perilously computed perplexity
     * @return return true iff page rank is converged
     */
    private static boolean isConverged(double perplexity, Queue<Double> history) {
        if (history.size() == 4) {
            history.remove();
        }
        history.add(perplexity);

        double prev = history.peek();
        int iteration = 0;
        for (double d : history) {
            if (Math.pow(d - prev, 2) < 1) {
                iteration++;
            } else {
                iteration = 0;
            }
            prev = d;
        }
        return iteration == 4;
    }

    /**
     * calculate page rank
     *
     * @param outlinks      HashMap of outlinks
     * @param sink_pagerank double page rank of all sinks
     * @param pageRank      previously computed pagerank
     * @param input         Stream of String
     * @return Map of String, Double as new page rank
     */
    private static Map<String, Double> calculatePageRank(HashMap<String, Integer> outlinks,
                                                         double sink_pagerank,
                                                         HashMap<String, Double> pageRank,
                                                         Stream<String> input) {


        return input.map(line -> getPageRankForDocument(outlinks, sink_pagerank, pageRank, line))
                    .collect(Collectors.toMap(PageModel::getPageId, PageModel::getRank));
    }

    /**
     * calculate page rank for document
     * @param outlinks Map[String, Integer] outlinks
     * @param sink_pagerank Double sink page rank
     * @param pageRank Map[String, Double] pageRank
     * @param document String Document
     * @return PageModel which contains rank of page
     */
    private static PageModel getPageRankForDocument(HashMap<String, Integer> outlinks,
                                                    double sink_pagerank,
                                                    HashMap<String, Double> pageRank,
                                                    String document) {
        String[] tokens = document.split(SEPARATOR);
        List<String> inlinks = Arrays.asList(tokens);
        inlinks = inlinks.subList(1, inlinks.size());

        // compute rank
        double rank = (1.0 - DAMPING_FACTOR) / pageRank.size();
        rank = rank + (sink_pagerank * DAMPING_FACTOR / pageRank.size());
        rank = rank + inlinks.stream()
                             .distinct()
                             .mapToDouble(link -> DAMPING_FACTOR * (pageRank.get(link) / outlinks.get(link)))
                             .sum();

        return new PageModel(tokens[0], rank);
    }

    /**
     * find outlinks from the graph
     *
     * @param input Stream of String where each string is page
     * @return HashMap of String, Integer as get outlinks
     */
    private static HashMap<String, Integer> getOutlinks(Stream<String> input) {
        HashMap<String, Integer> outlinks = new HashMap<>();

        input.forEach(line -> updateOutlinks(outlinks, line));

        return outlinks;
    }

    /**
     * update outlink
     * @param outlinks Map[String, Integer]
     * @param line String document and inlinks
     */
    private static void updateOutlinks(HashMap<String, Integer> outlinks, String line) {
        String[] tokens = line.split(SEPARATOR);
        List<String> inlinks = Arrays.asList(tokens);
        inlinks = inlinks.subList(1, inlinks.size());

        inlinks.stream()
               .distinct()
               .forEach(page -> {
                            if (outlinks.containsKey(page)) {
                                outlinks.put(page, outlinks.get(page) + 1);
                            } else {
                                outlinks.put(page, 1);
                            }
               });
    }

    /**
     * compute initial page rank
     *
     * @param initialRank double initial rank
     * @param input     Stream of String where each string is unique page
     * @return Map of String, Double as page rank
     */
    private static Map<String, Double> getInitialPageRank(double initialRank, Stream<String> input) {
        return input.collect(Collectors.toMap(
                line -> {
                    int place = line.indexOf(' ');
                    return place != -1 ? line.substring(0, place) : line;
                },
                line -> initialRank));
    }


    /**
     * get sinks form the graph
     *
     * @param pages Set of string as pages
     * @param input Stream of string
     * @return Set of String as pages which are sinks
     */
    private static Set<String> getSinks(Set<String> pages, Stream<String> input) {
        Set<String> sinks = new HashSet<>(pages.size());
        sinks.addAll(pages);

        input.forEach(line -> {
            String[] tokens = line.split(SEPARATOR);
            for (int i = 1; i < tokens.length; i++) {
                if (sinks.contains(tokens[i])) {
                    sinks.remove(tokens[i]);
                }
            }
        });
        return sinks;
    }

    /**
     * compute perplexity
     *
     * @param pageRank HashMap as String, Double
     * @return double perplexity
     */
    private static double perplexity(HashMap<String, Double> pageRank) {
        double entropy = pageRank.values()
                .parallelStream()
                .mapToDouble(rank -> (rank * (Math.log(1.0 / rank) / Math.log(2))))
                .sum();

        return Math.pow(2, entropy);
    }

}
