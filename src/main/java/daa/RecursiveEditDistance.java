package daa;

/**
 * Version A: plain recursion, no memory.
 *
 * DP design:
 *  State:      f(i, j) = edit distance between the first i chars of a and the first j chars of b.
 *  Transition: if a[i-1] == b[j-1]: f(i-1, j-1)
 *              else 1 + min(f(i-1, j) [delete], f(i, j-1) [insert], f(i-1, j-1) [replace]).
 *  Base case:  f(0, j) = j and f(i, 0) = i.
 *
 * Counter: ops = number of calls of f. It shows how often the same (i, j) is solved again.
 */
public class RecursiveEditDistance implements EditDistance {
    private long calls;

    @Override
    public int distance(String a, String b) {
        calls = 0;
        return f(a, b, a.length(), b.length());
    }

    private int f(String a, String b, int i, int j) {
        calls++;
        if (i == 0) return j;
        if (j == 0) return i;
        if (a.charAt(i - 1) == b.charAt(j - 1)) return f(a, b, i - 1, j - 1);
        int del = f(a, b, i - 1, j);
        int ins = f(a, b, i, j - 1);
        int rep = f(a, b, i - 1, j - 1);
        return 1 + Math.min(del, Math.min(ins, rep));
    }

    @Override public long ops() { return calls; }
    @Override public long memCells() { return 0; }
    @Override public String name() { return "edit_recursive"; }
}
