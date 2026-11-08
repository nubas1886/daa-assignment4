package daa;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class SearchTest {
    private final List<StringSearch> algos = List.of(new NaiveSearch(), new KmpSearch());

    @Test
    void lpsReferenceArrays() {
        assertArrayEquals(new int[] {0, 1, 2, 0, 1, 2, 3, 3}, KmpSearch.lps("AAACAAAA"));
        assertArrayEquals(new int[] {0, 0, 1, 2, 0, 1, 2, 3, 4}, KmpSearch.lps("ABABCABAB"));
    }

    @Test
    void referenceMatches() {
        for (StringSearch s : algos) {
            assertEquals(List.of(10), s.search("ABABDABACDABABCABAB", "ABABCABAB"), s.name());
            assertEquals(List.of(0, 1, 2, 3), s.search("AAAAA", "AA"), s.name());
            assertEquals(List.of(0, 2, 4), s.search("ABABABA", "ABA"), s.name());
        }
    }

    @Test
    void noMatchGivesEmptyList() {
        for (StringSearch s : algos) {
            assertTrue(s.search("abcdef", "xyz").isEmpty(), s.name());
        }
    }

    @Test
    void emptyPatternThrows() {
        for (StringSearch s : algos) {
            assertThrows(IllegalArgumentException.class, () -> s.search("abc", ""), s.name());
        }
    }

    @Test
    void patternLongerThanTextGivesEmptyList() {
        for (StringSearch s : algos) {
            assertTrue(s.search("ab", "abc").isEmpty(), s.name());
            assertTrue(s.search("", "a").isEmpty(), s.name());
        }
    }

    @Test
    void patternEqualsText() {
        for (StringSearch s : algos) {
            assertEquals(List.of(0), s.search("abc", "abc"), s.name());
        }
    }

    @Test
    void naiveAndKmpAgreeOnRandomTexts() {
        Random r = new Random(2024);
        Generator g = new Generator(99);
        NaiveSearch naive = new NaiveSearch();
        KmpSearch kmp = new KmpSearch();
        for (int t = 0; t < 100; t++) {
            String text = g.random(1 + r.nextInt(60), 2);
            String pat = g.random(1 + r.nextInt(5), 2);
            assertEquals(naive.search(text, pat), kmp.search(text, pat),
                    "text=" + text + " pattern=" + pat);
        }
    }

    @Test
    void kmpComparisonsAreLinear() {
        KmpSearch kmp = new KmpSearch();
        String text = Generator.repeat('a', 1000);
        String pat = Generator.repeat('a', 49) + "b";
        kmp.search(text, pat);
        // search phase <= 2n, LPS build <= 2m
        assertTrue(kmp.comparisons() <= 2L * 1000 + 2L * 50);
        NaiveSearch naive = new NaiveSearch();
        naive.search(text, pat);
        assertEquals(50L * (1000 - 50 + 1), naive.comparisons());
    }

    @Test
    void generatorIsReproducible() {
        assertEquals(new Generator(5).random(50, 26), new Generator(5).random(50, 26));
    }
}
