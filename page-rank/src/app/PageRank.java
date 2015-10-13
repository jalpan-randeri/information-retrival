package app;

import model.PageModel;
import utils.InputReader;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class PageRank {
    private static final double DAMPING_FACTOR = 0.85;
    private static final String SEPARATOR = " ";


    /**
     * compute page ranks
     *
     * @param inputFile String file
     * @throws IOException
     */
    public static void computePageRank(String inputFile) throws IOException {

        // calculate size
        long graphSize = InputReader.readInputFile(inputFile).count();

        // initial page rank
        HashMap<String, Double> pageRank = (HashMap<String, Double>) getInitialPageRank(graphSize,
                InputReader.readInputFile(inputFile));

        // find sinks
        Set<String> sinks = getSinks(pageRank.keySet(), InputReader.readInputFile(inputFile));

        // outlink
        HashMap<String, Integer> outlinks = getOutlinks(InputReader.readInputFile(inputFile));

        // computed page ranks
        HashMap<String, Double> ranks = pagerankHelper(inputFile, pageRank, sinks, outlinks);

        // sort the pages
        List<PageModel> sorted_ranks = ranks.entrySet()
                                            .parallelStream()
                                            .sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue()))
                                            .map(entry -> new PageModel(entry.getKey(), entry.getValue()))
                                            .collect(Collectors.toList());


        // question 1
        // top 50 pages with highest page rank
        List<PageModel> top50 = sorted_ranks.subList(0, 50);
        top50.forEach(System.out::println);

        // top 50 pages and their pagerank with highest inlinks count
        List<PageModel> top50inlinks = InputReader.readInputFile(inputFile)
                .map(line -> {
                    String[] page = line.split(SEPARATOR);
                    PageModel model = new PageModel();
                    model.setPageId(page[0]);
                    model.setRank(ranks.get(page[0]));
                    model.setInlinkCount(page.length - 1);
                    return model;
                })
                .sorted((o1, o2) -> o2.getInlinkCount().compareTo(o1.getInlinkCount()))
                .limit(50)
                .collect(Collectors.toList());
        top50inlinks.forEach(page ->
                System.out.printf("%s   %.20f   %d\n", page.getPageId(), page.getRank(), page.getInlinkCount()));
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

        double perplexity = perplexity(pageRank);
        Queue<Double> history = new ArrayDeque<>(4);

        while (!isConverged(perplexity, history)) {

            final double sink_pagerank = sinks.stream().mapToDouble(pageRank::get).sum();

            // must be exclusive final
            final HashMap<String, Double> finalPageRank = pageRank;
            pageRank = (HashMap<String, Double>) calculatePageRank(outlinks,
                    sink_pagerank,
                    finalPageRank,
                    InputReader.readInputFile(inputFile));

            perplexity = perplexity(pageRank);
            System.out.printf("perplexity = %.10f\n", perplexity);
        }


        // sort
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
        // adjust iteration to -1 coz we are iterating from 0 instead of 1
        int iteration = -1;
        for (double d : history) {
            if (Math.abs(d - prev) < 1) {
                iteration++;
            } else {
                iteration = 0;
            }
            prev = d;
        }
        return iteration == 3;
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


        return input.map(line -> {
                    String[] tokens = line.split(SEPARATOR);
                    double rank = (1.0 - DAMPING_FACTOR) / pageRank.size();
                    rank = rank + (sink_pagerank * DAMPING_FACTOR / pageRank.size());

                    for (int i = 1; i < tokens.length; i++) {
                        rank = rank + (DAMPING_FACTOR * pageRank.get(tokens[i]) / outlinks.get(tokens[i]));
                    }

                    return new PageModel(tokens[0], rank);
                }
        ).collect(Collectors.toMap(PageModel::getPageId, PageModel::getRank));
    }

    /**
     * find outlinks from the graph
     *
     * @param input Stream of String where each string is page
     * @return HashMap of String, Integer as get outlinks
     */
    private static HashMap<String, Integer> getOutlinks(Stream<String> input) {
        HashMap<String, Integer> outlinks = new HashMap<>();

        input.forEach(line -> {
            String[] tokens = line.split(SEPARATOR);
            for (int i = 1; i < tokens.length; i++) {
                String page = tokens[i];
                if (outlinks.containsKey(page)) {
                    outlinks.put(page, outlinks.get(page) + 1);
                } else {
                    outlinks.put(page, 1);
                }
            }
        });

        return outlinks;
    }

    /**
     * compute initial page rank
     *
     * @param graphSize long size of graph
     * @param input     Stream of String where each string is unique page
     * @return Map of String, Double as page rank
     */
    private static Map<String, Double> getInitialPageRank(long graphSize, Stream<String> input) {
        return input.collect(Collectors.toMap(
                line -> {
                    int place = line.indexOf(' ');
                    return place != -1 ? line.substring(0, place) : line;
                },
                line -> 1.0 / graphSize));
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
                .stream()
                .mapToDouble(rank -> (rank * (Math.log10(1 / rank) / Math.log10(2))))
                .sum();

        return Math.pow(2, entropy);
    }

}
