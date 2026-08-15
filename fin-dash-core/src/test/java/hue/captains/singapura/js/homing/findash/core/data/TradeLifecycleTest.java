package hue.captains.singapura.js.homing.findash.core.data;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The lifecycle simulator's contract. It is a pure function of a trade — no
 * clock, no I/O, no shared state — so it is testable exactly as written.
 */
class TradeLifecycleTest {

    private static DeskData.Trade trade(String id, String ticket) {
        return new DeskData.Trade(id, "pf-eur-van", "10:00:00", "trader A", ticket,
                "EURUSD", "€10M", "+€1,000", "C204 · S513/87 · VV-2.3", "booked", List.of());
    }

    @Test
    @DisplayName("the same trade always yields the same journal")
    void isDeterministic() {
        // The claim the whole demo leans on: a reconstructed screen must not
        // change under refresh (P4), and the platform console reports replay
        // determinism as a fact.
        for (DeskData.Trade t : DeskData.TRADES) {
            TradeLifecycle.Result a = TradeLifecycle.of(t);
            TradeLifecycle.Result b = TradeLifecycle.of(t);
            assertEquals(a.outcome(), b.outcome(), "outcome moved for " + t.id());
            assertEquals(a.events(), b.events(), "journal moved for " + t.id());
        }
    }

    @Test
    @DisplayName("different trades get different lives")
    void isNotAConstant() {
        // Determinism is worthless if every trade gets the same story; the seed
        // has to actually vary with the trade.
        Set<String> shapes = DeskData.TRADES.stream()
                .map(t -> TradeLifecycle.of(t).outcome() + "/" + TradeLifecycle.of(t).events().size())
                .collect(Collectors.toSet());
        assertTrue(shapes.size() > 1, "every trade produced an identical lifecycle: " + shapes);
    }

    @Test
    @DisplayName("the scheme is read off the product in the ticket")
    void schemeFollowsTheTicket() {
        assertEquals(TradeLifecycle.Scheme.BARRIER,
                TradeLifecycle.schemeOf(trade("T-1", "eurusd 3m 1.0850 ko 1.1200 10")));
        assertEquals(TradeLifecycle.Scheme.ONE_TOUCH,
                TradeLifecycle.schemeOf(trade("T-2", "gbpusd 3m ot 1.3550 8")));
        assertEquals(TradeLifecycle.Scheme.DIGITAL,
                TradeLifecycle.schemeOf(trade("T-3", "usdjpy 1m digital 151.50 45")));
        assertEquals(TradeLifecycle.Scheme.VANILLA,
                TradeLifecycle.schemeOf(trade("T-4", "usdjpy 6m 145.00 put 25")));
    }

    @Test
    @DisplayName("every lifecycle opens with a booking and reaches an outcome")
    void alwaysWellFormed() {
        for (DeskData.Trade t : DeskData.TRADES) {
            TradeLifecycle.Result r = TradeLifecycle.of(t);
            assertFalse(r.events().isEmpty(), t.id() + " produced no events");
            assertEquals("booked", r.events().get(0).kind(),
                    t.id() + " does not open with a booking");
            assertFalse(r.outcome() == null || r.outcome().isBlank(),
                    t.id() + " reached no outcome");
            for (TradeLifecycle.Event e : r.events()) {
                assertFalse(e.label() == null || e.label().isBlank(),
                        t.id() + " has an unlabelled event");
                assertTrue(Set.of("good", "warn", "serious", "neutral", "critical")
                                .contains(e.severity()),
                        t.id() + " has severity '" + e.severity() + "', which no chip renders");
            }
        }
    }

    @Test
    @DisplayName("a trade's real amendments are carried, not invented over")
    void realAmendmentsSurvive() {
        // Amendments are journal facts. The simulator may add around them but
        // must never drop or rewrite them.
        DeskData.Trade amended = DeskData.TRADES.stream()
                .filter(t -> !t.amendments().isEmpty()).findFirst().orElseThrow();
        List<String> whats = TradeLifecycle.of(amended).events().stream()
                .filter(e -> "amend".equals(e.kind()))
                .map(TradeLifecycle.Event::detail).toList();
        assertEquals(amended.amendments().size(), whats.size(),
                "amendment count changed for " + amended.id());
        for (DeskData.Amendment a : amended.amendments()) {
            assertTrue(whats.stream().anyMatch(w -> w.contains(a.what())),
                    "lost the amendment: " + a.what());
        }
    }
}
