package daa;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class EditDistanceTest {
    private final List<EditDistance> versions =
            List.of(new RecursiveEditDistance(), new TableEditDistance(), new TwoRowEditDistance());

    @Test
    void referenceValues() {
        for (EditDistance ed : versions) {
            assertEquals(3, ed.distance("kitten", "sitting"), ed.name());
            assertEquals(2, ed.distance("flaw", "lawn"), ed.name());
            assertEquals(5, ed.distance("intention", "execution"), ed.name());
            assertEquals(3, ed.distance("", "abc"), ed.name());
            assertEquals(0, ed.distance("abc", "abc"), ed.name());
        }
    }

    @Test
    void emptyStrings() {
        for (EditDistance ed : versions) {
            assertEquals(0, ed.distance("", ""), ed.name());
            assertEquals(4, ed.distance("abcd", ""), ed.name());
            assertEquals(4, ed.distance("", "abcd"), ed.name());
        }
    }

    @Test
    void realWorldTypo() {
        for (EditDistance ed : versions) {
            assertEquals(2, ed.distance("Abylai Han", "Abylai Khan"), ed.name()); // insert K + replace H->h
            assertEquals(1, ed.distance("abylai han", "abylai khan"), ed.name()); // one missing letter
        }
    }

    @Test
    void symmetric() {
        for (EditDistance ed : versions) {
            assertEquals(ed.distance("sunday", "saturday"), ed.distance("saturday", "sunday"), ed.name());
        }
    }

    @Test
    void allVersionsAgreeOnRandomShortStrings() {
        Random r = new Random(12345);
        Generator g = new Generator(777);
        for (int t = 0; t < 100; t++) {
            String a = g.random(r.nextInt(9), 3);
            String b = g.random(r.nextInt(9), 3);
            int expected = versions.get(0).distance(a, b);
            for (EditDistance ed : versions) {
                assertEquals(expected, ed.distance(a, b), ed.name() + " on '" + a + "','" + b + "'");
            }
        }
    }

    @Test
    void countersAreMeasured() {
        TableEditDistance t = new TableEditDistance();
        t.distance("abcd", "xyz");
        assertEquals(12, t.ops());
        assertEquals(20, t.memCells());
        TwoRowEditDistance w = new TwoRowEditDistance();
        w.distance("abcd", "xyz");
        assertEquals(12, w.ops());
        assertEquals(8, w.memCells());
    }
}
