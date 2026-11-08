package daa;

import java.util.ArrayList;
import java.util.List;

/**
 * Knuth-Morris-Pratt. lps[i] = length of the longest proper prefix of p[0..i]
 * that is also a suffix of p[0..i]. On a mismatch the pattern index falls back
 * via lps, the text index i never moves backwards.
 */
public class KmpSearch implements StringSearch {
    private long cmp;
    private long mem;

    /** Public so that tests can call it directly. */
    public static int[] lps(String p) {
        return buildLps(p, new long[1]);
    }

    private static int[] buildLps(String p, long[] counter) {
        int m = p.length();
        int[] lps = new int[m];
        int len = 0, i = 1;
        while (i < m) {
            counter[0]++;
            if (p.charAt(i) == p.charAt(len)) {
                lps[i++] = ++len;
            } else if (len > 0) {
                len = lps[len - 1];
            } else {
                lps[i++] = 0;
            }
        }
        return lps;
    }

    @Override
    public List<Integer> search(String text, String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            throw new IllegalArgumentException("pattern must not be empty");
        }
        long[] c = new long[1];
        List<Integer> res = new ArrayList<>();
        int n = text.length(), m = pattern.length();
        int[] lps = buildLps(pattern, c);
        mem = m;
        int j = 0;
        for (int i = 0; i < n; i++) {
            while (true) {
                c[0]++;
                if (text.charAt(i) == pattern.charAt(j)) { j++; break; }
                if (j == 0) break;
                j = lps[j - 1];
            }
            if (j == m) {
                res.add(i - m + 1);
                j = lps[j - 1];
            }
        }
        cmp = c[0];
        return res;
    }

    @Override public long comparisons() { return cmp; }
    @Override public long memCells() { return mem; }
    @Override public String name() { return "search_kmp"; }
}
