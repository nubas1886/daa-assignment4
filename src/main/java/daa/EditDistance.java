package daa;

/** Common interface for all Edit Distance versions, so the experiment is one loop. */
public interface EditDistance {
    int distance(String a, String b);

    /** Number of subproblems evaluated during the LAST call to distance(). */
    long ops();

    /** Number of int cells allocated for DP storage during the LAST call (0 for recursion). */
    long memCells();

    String name();
}
