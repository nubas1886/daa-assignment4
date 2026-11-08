package daa;

/**
 * Version C: the same DP, but only the previous and the current row are kept.
 *
 * DP design:
 *  State:      prev[j] = dp[i-1][j], cur[j] = dp[i][j] (only two rows of length m+1).
 *  Transition: cur[j] = prev[j-1] if a[i-1] == b[j-1],
 *              else 1 + min(prev[j], cur[j-1], prev[j-1]); after each i the rows are swapped.
 *  Base case:  prev[j] = j (row 0), cur[0] = i (column 0).
 *
 * Counter: ops = number of inner cells computed; memCells = total length of both rows.
 */
public class TwoRowEditDistance implements EditDistance {
    private long cells;
    private long mem;

    @Override
    public int distance(String a, String b) {
        int n = a.length(), m = b.length();
        cells = 0;
        int[] prev = new int[m + 1];
        int[] cur = new int[m + 1];
        mem = 2L * (m + 1);
        for (int j = 0; j <= m; j++) prev[j] = j;
        for (int i = 1; i <= n; i++) {
            cur[0] = i;
            for (int j = 1; j <= m; j++) {
                cells++;
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    cur[j] = prev[j - 1];
                } else {
                    cur[j] = 1 + Math.min(prev[j], Math.min(cur[j - 1], prev[j - 1]));
                }
            }
            int[] t = prev; prev = cur; cur = t;
        }
        return prev[m];
    }

    @Override public long ops() { return cells; }
    @Override public long memCells() { return mem; }
    @Override public String name() { return "edit_two_rows"; }
}
