package hue.captains.singapura.js.homing.findash.core.data;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The served book's integrity.
 *
 * <p>These are the checks that would have caught the seven positions pointing at
 * trades that did not exist — a desk head drilling from a risk line into the
 * journal and landing on nothing. Cross-fixture references are exactly the class
 * of defect no single fixture's author can see.</p>
 */
class DeskBookTest {

    @Test
    @DisplayName("every position can open its trade")
    void noDanglingTradeReferences() {
        Set<String> tradeIds = DeskData.TRADES.stream()
                .map(DeskData.Trade::id).collect(Collectors.toSet());
        List<String> dangling = DeskData.POSITIONS.stream()
                .map(DeskData.Position::tradeId)
                .filter(id -> id != null && !tradeIds.contains(id))
                .distinct().toList();
        assertTrue(dangling.isEmpty(),
                "positions reference trades that do not exist: " + dangling);
    }

    @Test
    @DisplayName("ids are unique across the book")
    void idsAreUnique() {
        assertNoDuplicates(DeskData.TRADES.stream().map(DeskData.Trade::id).toList(), "trade");
        assertNoDuplicates(DeskData.POSITIONS.stream().map(DeskData.Position::id).toList(), "position");
    }

    @Test
    @DisplayName("the book is deep enough for the blotter's own NFR")
    void isDeepEnoughToLookLikeABook() {
        // The blotter's stated requirement is thirty rows without scrolling. Six
        // curated trades read as a screenshot, not a desk.
        assertTrue(DeskData.TRADES.size() >= 30,
                "only " + DeskData.TRADES.size() + " trades — the blotter cannot fill itself");
        assertTrue(DeskData.POSITIONS.size() >= 25,
                "only " + DeskData.POSITIONS.size() + " positions");
    }

    @Test
    @DisplayName("the curated stories survive generation")
    void curatedFixturesAreNotDisplaced() {
        // The generated book is built around the hand-written spine; if a
        // generated id ever collided with a curated one, the story would vanish
        // and only its absence downstream would show it.
        for (DeskData.Trade c : DeskData.CURATED_TRADES) {
            assertTrue(DeskData.TRADES.contains(c),
                    "curated trade " + c.id() + " was displaced by a generated one");
        }
        for (DeskData.Position c : DeskData.CURATED_POSITIONS) {
            assertTrue(DeskData.POSITIONS.contains(c),
                    "curated position " + c.id() + " was displaced");
        }
    }

    @Test
    @DisplayName("every row is filed under a real desk and carries lineage")
    void everyRowIsWellFormed() {
        Set<String> desks = DeskData.POSITIONS.stream()
                .map(DeskData.Position::portfolioId).collect(Collectors.toSet());
        for (DeskData.Trade t : DeskData.TRADES) {
            assertTrue(desks.contains(t.portfolioId()),
                    t.id() + " is filed under '" + t.portfolioId() + "', which holds no positions"
                            + " — it would vanish from every portfolio-filtered blotter");
            // P1: a number that cannot say what it was priced on is not evidence.
            assertTrue(t.stamp() != null && t.stamp().contains("·"),
                    t.id() + " carries no lineage stamp");
            assertTrue(t.ticket() != null && !t.ticket().isBlank(),
                    t.id() + " has no ticket");
        }
    }

    @Test
    @DisplayName("the book is the same on every start")
    void isDeterministic() {
        assertEquals(DeskBook.allTrades(), DeskBook.allTrades());
        assertEquals(DeskBook.allPositions(), DeskBook.allPositions());
    }

    private static void assertNoDuplicates(List<String> ids, String what) {
        Set<String> seen = new HashSet<>();
        List<String> dupes = ids.stream().filter(id -> !seen.add(id)).distinct().toList();
        assertTrue(dupes.isEmpty(), "duplicate " + what + " ids: " + dupes);
    }
}
