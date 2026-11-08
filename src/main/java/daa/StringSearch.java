package daa;

import java.util.List;

/** Common interface for string search. Returns all start indices (0-based), increasing, overlaps included. */
public interface StringSearch {
    List<Integer> search(String text, String pattern);

    /** Number of character comparisons (text vs pattern, plus LPS build for KMP) in the LAST call. */
    long comparisons();

    /** Extra int cells allocated by the algorithm in the LAST call (LPS array for KMP). */
    long memCells();

    String name();
}
