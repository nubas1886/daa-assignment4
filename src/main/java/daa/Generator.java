package daa;

import java.util.Random;

/** Seeded input generator: the same seed always gives the same strings. */
public class Generator {
    private final Random rnd;

    public Generator(long seed) { this.rnd = new Random(seed); }

    public String random(int n, int alphabetSize) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) sb.append((char) ('a' + rnd.nextInt(alphabetSize)));
        return sb.toString();
    }

    public static String repeat(char c, int n) {
        char[] arr = new char[n];
        java.util.Arrays.fill(arr, c);
        return new String(arr);
    }

    /** A substring of text of length m at a random place (so at least one match exists). */
    public String substringOf(String text, int m) {
        int start = rnd.nextInt(text.length() - m + 1);
        return text.substring(start, start + m);
    }
}
