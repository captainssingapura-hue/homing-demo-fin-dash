package hue.captains.singapura.js.homing.findash.core.data;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * The tolerance table is the one place a measurement becomes a verdict, so
 * its boundaries are pinned here — a widget that used to say {@code > 10}
 * now relies on this saying the same thing.
 */
class DeskTolerancesTest {

    @Test
    @DisplayName("freshness: at the budget is fresh, past it is stale")
    void freshness() {
        assertEquals(DeskTolerances.FRESH, DeskTolerances.freshState(0.0));
        assertEquals(DeskTolerances.FRESH, DeskTolerances.freshState(10.0));
        assertEquals(DeskTolerances.STALE, DeskTolerances.freshState(10.01));
        assertEquals(DeskTolerances.STALE, DeskTolerances.freshState(45.0));
    }

    @Test
    @DisplayName("budget: ok below warn, warn from 0.75, over from 0.90")
    void budget() {
        assertEquals(DeskTolerances.OK,   DeskTolerances.budgetState(0.0));
        assertEquals(DeskTolerances.OK,   DeskTolerances.budgetState(0.749));
        assertEquals(DeskTolerances.WARN, DeskTolerances.budgetState(0.75));
        assertEquals(DeskTolerances.WARN, DeskTolerances.budgetState(0.899));
        assertEquals(DeskTolerances.OVER, DeskTolerances.budgetState(0.90));
        assertEquals(DeskTolerances.OVER, DeskTolerances.budgetState(1.2));
    }

    @Test
    @DisplayName("not published is carried, never invented")
    void unpublished() {
        assertNull(DeskTolerances.freshState(null));
        assertNull(DeskTolerances.budgetState(null));
    }

    @Test
    @DisplayName("the desk's SLO table judged by the same function the console renders")
    void slosAgreeWithTheTable() {
        // The one SLO the demo data puts over budget ("reval-wave lag", 0.95)
        // must come out OVER, and nothing else may.
        long over = DeskData.SLOS.stream()
                .filter(s -> DeskTolerances.OVER.equals(DeskTolerances.budgetState(s.budgetFrac())))
                .count();
        assertEquals(1, over);
    }
}
