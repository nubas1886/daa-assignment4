package daa;

import java.util.ArrayList;
import java.util.List;

/** Naive search: try every start position, compare left to right, stop at the first mismatch. */
public class NaiveSearch implements StringSearch {
    private long cmp;

    @Override
    public List<Integer> search(String text, String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            throw new IllegalArgumentException("pattern must not be empty");
        }
        cmp = 0;
        List<Integer> res = new ArrayList<>();
        int n = text.length(), m = pattern.length();
        for (int s = 0; s + m <= n; s++) {
            int k = 0;
            while (k < m) {
                cmp++;
                if (text.charAt(s + k) != pattern.charAt(k)) break;
                k++;
            }
            if (k == m) res.add(s);
        }
        return res;
    }

    @Override public long comparisons() { return cmp; }
    @Override public long memCells() { return 0; }
    @Override public String name() { return "search_naive"; }
}
