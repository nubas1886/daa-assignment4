package daa;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * Runs the whole experiment and writes results/results.csv.
 * Usage: java -cp target/classes daa.Experiment [output.csv]
 * Fixed seeds: the counters and result values are identical on every run (times vary a little).
 */
public class Experiment {
    static final long SEED = 42;
    static final double RECURSIVE_LIMIT_MS = 300;
    static final String HEADER =
            "family,algorithm,input,n,m,result,runs,time_ms,ops,mem_cells";

    public static void main(String[] args) throws IOException {
        Path out = Paths.get(args.length > 0 ? args[0] : "results/results.csv");
        if (out.getParent() != null) Files.createDirectories(out.getParent());
        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(out))) {
            w.println(HEADER);
            editDistance(w);
            stringSearch(w);
        }
        System.out.println("Wrote " + out.toAbsolutePath());
    }

    // ---------- Edit Distance ----------

    static void editDistance(PrintWriter w) {
        int[] recSizes = {4, 6, 8, 10, 12, 14, 16, 18};
        int[] dpSizes = {8, 12, 16, 250, 500, 1000, 2000, 4000};
        String[] inputs = {"random", "identical", "disjoint"};

        for (String input : inputs) {
            boolean stopped = false;
            for (int n : recSizes) {
                if (stopped) break;
                Generator g = new Generator(SEED + n);
                String[] ab = makePair(input, n, g);
                Row r = measure(new RecursiveEditDistance(), ab[0], ab[1]);
                print(w, "edit_distance", "edit_recursive", input, n, n, r);
                if (r.timeMs > RECURSIVE_LIMIT_MS) stopped = true;
            }
        }
        List<EditDistance> dp = List.of(new TableEditDistance(), new TwoRowEditDistance());
        for (String input : inputs) {
            for (int n : dpSizes) {
                Generator g = new Generator(SEED + n);
                String[] ab = makePair(input, n, g);
                int expected = -1;
                for (EditDistance ed : dp) {
                    Row r = measure(ed, ab[0], ab[1]);
                    if (expected == -1) expected = (int) r.result;
                    if (expected != r.result) throw new IllegalStateException("versions disagree");
                    print(w, "edit_distance", ed.name(), input, n, n, r);
                }
            }
        }
    }

    static String[] makePair(String input, int n, Generator g) {
        switch (input) {
            case "random": return new String[] {g.random(n, 26), g.random(n, 26)};
            case "identical": {
                String s = g.random(n, 26);
                return new String[] {s, s};
            }
            case "disjoint": return new String[] {Generator.repeat('a', n), Generator.repeat('b', n)};
            default: throw new IllegalArgumentException(input);
        }
    }

    static Row measure(EditDistance ed, String a, String b) {
        // warm-up for fast cases only (never repeat a slow recursive run just to warm up)
        long t0 = System.nanoTime();
        int result = ed.distance(a, b);
        double first = (System.nanoTime() - t0) / 1e6;
        long ops = ed.ops(), mem = ed.memCells();
        int runs = first < 200 ? 5 : 3;
        double[] times = new double[runs];
        for (int i = 0; i < runs; i++) {
            long s = System.nanoTime();
            int r2 = ed.distance(a, b);
            times[i] = (System.nanoTime() - s) / 1e6;
            if (r2 != result || ed.ops() != ops) throw new IllegalStateException("not deterministic");
        }
        return new Row(result, runs, median(times), ops, mem);
    }

    // ---------- String search ----------

    static void stringSearch(PrintWriter w) {
        int[] sizes = {100_000, 200_000, 400_000, 800_000, 1_600_000};
        int m = 100;
        String[] inputs = {"random26", "random_binary", "worst_naive", "all_match"};
        List<StringSearch> algos = List.of(new NaiveSearch(), new KmpSearch());
        for (String input : inputs) {
            for (int n : sizes) {
                Generator g = new Generator(SEED + n);
                String text, pat;
                switch (input) {
                    case "random26": text = g.random(n, 26); pat = g.substringOf(text, m); break;
                    case "random_binary": text = g.random(n, 2); pat = g.substringOf(text, m); break;
                    case "worst_naive":
                        text = Generator.repeat('a', n);
                        pat = Generator.repeat('a', m - 1) + "b";
                        break;
                    case "all_match":
                        text = Generator.repeat('a', n);
                        pat = Generator.repeat('a', m);
                        break;
                    default: throw new IllegalArgumentException(input);
                }
                List<Integer> expected = null;
                for (StringSearch s : algos) {
                    // warm-up
                    for (int i = 0; i < 2; i++) s.search(text, pat);
                    int runs = 5;
                    double[] times = new double[runs];
                    List<Integer> res = null;
                    for (int i = 0; i < runs; i++) {
                        long t = System.nanoTime();
                        res = s.search(text, pat);
                        times[i] = (System.nanoTime() - t) / 1e6;
                    }
                    if (expected == null) expected = res;
                    else if (!expected.equals(res)) throw new IllegalStateException("search results differ");
                    Row r = new Row(res.size(), runs, median(times), s.comparisons(), s.memCells());
                    print(w, "string_search", s.name(), input, n, m, r);
                }
            }
        }
    }

    // ---------- helpers ----------

    record Row(long result, int runs, double timeMs, long ops, long mem) {}

    static void print(PrintWriter w, String family, String algo, String input, int n, int m, Row r) {
        w.printf(java.util.Locale.ROOT, "%s,%s,%s,%d,%d,%d,%d,%.4f,%d,%d%n",
                family, algo, input, n, m, r.result, r.runs, r.timeMs, r.ops, r.mem);
        w.flush();
        System.out.printf(java.util.Locale.ROOT, "%-14s %-16s %-14s n=%-8d time=%.3f ms ops=%d%n",
                family, algo, input, n, r.timeMs, r.ops);
    }

    static double median(double[] a) {
        double[] c = a.clone();
        Arrays.sort(c);
        return c.length % 2 == 1 ? c[c.length / 2] : (c[c.length / 2 - 1] + c[c.length / 2]) / 2;
    }
}
