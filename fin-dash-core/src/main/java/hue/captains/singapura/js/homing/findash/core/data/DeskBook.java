package hue.captains.singapura.js.homing.findash.core.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Builds the demo book to a size a desk would recognise, from the curated
 * fixtures as a spine.
 *
 * <h2>Why generated rather than hand-written</h2>
 *
 * <p>The curated trades carry the <b>stories</b> — the confirm mismatch that
 * ties to a break, the amendment with its four-eyes record — and are worth
 * writing by hand. What they cannot carry is <b>volume</b>, and volume is what
 * makes a blotter read as a book rather than a screenshot: the widget's own NFR
 * is thirty rows without scrolling, and six curated trades is six.</p>
 *
 * <h2>Referential integrity by construction</h2>
 *
 * <p>Positions carry a {@code tradeId} — the door from the risk view into the
 * journal. Seven of them pointed at trades that did not exist, so that door
 * opened onto nothing. Here <b>every position's trade is generated if it is not
 * curated</b>, derived from the position's own pair, tenor and notional, so the
 * two views cannot disagree.</p>
 *
 * <h2>Deterministic</h2>
 *
 * <p>One fixed seed. The book is identical on every start, which is what lets a
 * demo be rehearsed and lets {@link TradeLifecycle} — itself seeded per trade —
 * tell the same story twice.</p>
 */
public final class DeskBook {

    private DeskBook() {}

    private static final long SEED = 20250815L;

    /** Leaf portfolio → the pair it books, and whether it runs exotics. */
    private record Desk(String portfolioId, String pair, String ccy, boolean exotic) {}

    private static final List<Desk> DESKS = List.of(
            new Desk("pf-eur-van", "EURUSD", "€", false),
            new Desk("pf-eur-exo", "EURUSD", "€", true),
            new Desk("pf-gbp-van", "GBPUSD", "€", false),
            new Desk("pf-gbp-exo", "GBPUSD", "€", true),
            new Desk("pf-jpy-van", "USDJPY", "$", false),
            new Desk("pf-jpy-exo", "USDJPY", "$", true),
            new Desk("pf-aud-van", "AUDUSD", "A$", false),
            new Desk("pf-eurjpy",  "EURJPY", "€", false));

    private static final List<String> TRADERS =
            List.of("trader A", "trader B", "trader C", "sales D (RFQ)", "sales E (RFQ)");
    private static final List<String> TENORS = List.of("1W", "2W", "1M", "2M", "3M", "6M", "1Y");

    /** Spot-ish anchors, so strikes land near where the pair actually trades. */
    private static final Map<String, Double> SPOT = Map.of(
            "EURUSD", 1.0850, "GBPUSD", 1.3600, "USDJPY", 151.50,
            "AUDUSD", 0.6400, "EURJPY", 162.00);

    // ---- public: the book the demo serves --------------------------------

    /**
     * Curated trades first (they are the ones other fixtures reference), then a
     * trade for every position that lacks one, then filler for depth.
     */
    public static List<DeskData.Trade> allTrades() {
        var byId = new LinkedHashMap<String, DeskData.Trade>();
        for (DeskData.Trade t : DeskData.CURATED_TRADES) byId.put(t.id(), t);

        Random rnd = new Random(SEED);

        // 1. every position must be able to open its trade
        for (DeskData.Position p : DeskData.CURATED_POSITIONS) {
            byId.computeIfAbsent(p.tradeId(), id -> fromPosition(id, p, rnd));
        }

        // 2. depth — spread across the desks, walking back through the session
        for (int i = 0; i < 44; i++) {
            String id = "T-" + (4300 - i * 3);
            if (byId.containsKey(id)) continue;
            byId.put(id, filler(id, i, rnd));
        }
        return List.copyOf(new ArrayList<>(byId.values()));
    }

    /** The curated positions, plus depth for the desks that would carry more. */
    public static List<DeskData.Position> allPositions() {
        var out = new ArrayList<>(DeskData.CURATED_POSITIONS);
        Random rnd = new Random(SEED + 1);
        int n = 1600;
        for (int i = 0; i < 26; i++) {
            Desk d = DESKS.get(rnd.nextInt(DESKS.size()));
            String tenor = TENORS.get(rnd.nextInt(TENORS.size()));
            double strike = strikeNear(d.pair(), rnd);
            boolean exo = d.exotic() && rnd.nextInt(100) < 60;
            String instrument = exo
                    ? (rnd.nextBoolean() ? "Barrier KO Call " : "One-touch ") + fmt(d.pair(), strike)
                    : (rnd.nextBoolean() ? "Vanilla Call " : "Vanilla Put ") + fmt(d.pair(), strike);
            int notional = 5 + rnd.nextInt(45);
            int pv = (rnd.nextInt(180) - 60);
            double fresh = 2 + rnd.nextInt(12);
            double budget = 0.15 + rnd.nextInt(70) / 100.0;
            out.add(new DeskData.Position(
                    "P-" + (++n), d.portfolioId(), d.pair(), tenor, instrument,
                    d.ccy() + notional + "M",
                    (pv < 0 ? "−" : "+") + "€" + Math.abs(pv) + "k",
                    (rnd.nextBoolean() ? "+" : "−") + (1 + rnd.nextInt(30)) / 10.0 + "M",
                    (5 + rnd.nextInt(60)) + "k",
                    exo && rnd.nextInt(100) < 25 ? (10 + rnd.nextInt(80)) + " pips" : null,
                    fresh, budget,
                    "T-" + (4300 - rnd.nextInt(44) * 3)));
        }
        return List.copyOf(out);
    }

    // ---- generation ------------------------------------------------------

    /** A trade that matches the position pointing at it. */
    private static DeskData.Trade fromPosition(String id, DeskData.Position p, Random rnd) {
        Desk d = deskOf(p.portfolioId());
        String ticket = ticketFor(d, p.tenor(), strikeNear(p.pair(), rnd), rnd);
        return new DeskData.Trade(id, p.portfolioId(), clock(rnd), pick(TRADERS, rnd),
                ticket, p.pair(), p.notional(), p.pv(),
                stamp(rnd), "booked", List.of());
    }

    private static DeskData.Trade filler(String id, int i, Random rnd) {
        Desk d = DESKS.get(rnd.nextInt(DESKS.size()));
        String tenor = TENORS.get(rnd.nextInt(TENORS.size()));
        double strike = strikeNear(d.pair(), rnd);
        int notional = 5 + rnd.nextInt(45);
        int pv = rnd.nextInt(220) - 80;
        // Older trades sit on earlier days; the session's own trades keep a clock.
        String when = i < 12 ? clock(rnd) : (28 - i / 3) + "-Jul";
        return new DeskData.Trade(id, d.portfolioId(), when, pick(TRADERS, rnd),
                ticketFor(d, tenor, strike, rnd), d.pair(),
                d.ccy() + notional + "M",
                (pv < 0 ? "−" : "+") + "€" + Math.abs(pv) + "," + (100 + rnd.nextInt(899)),
                stamp(rnd), "booked", List.of());
    }

    /** The desk shorthand — the same grammar the pricer parses. */
    private static String ticketFor(Desk d, String tenor, double strike, Random rnd) {
        String pair = d.pair().toLowerCase();
        String k = fmt(d.pair(), strike);
        int size = 5 + rnd.nextInt(45);
        if (!d.exotic()) {
            return pair + " " + tenor.toLowerCase() + " " + k
                    + (rnd.nextBoolean() ? " put " : " ") + size;
        }
        return switch (rnd.nextInt(3)) {
            case 0 -> pair + " " + tenor.toLowerCase() + " " + k + " ko "
                      + fmt(d.pair(), strike * 1.03) + " " + size;
            case 1 -> pair + " " + tenor.toLowerCase() + " ot " + k + " " + size;
            default -> pair + " " + tenor.toLowerCase() + " digital " + k + " " + size;
        };
    }

    private static Desk deskOf(String portfolioId) {
        for (Desk d : DESKS) if (d.portfolioId().equals(portfolioId)) return d;
        return DESKS.get(0);
    }

    private static double strikeNear(String pair, Random rnd) {
        double spot = SPOT.getOrDefault(pair, 1.0);
        return spot * (1 + (rnd.nextInt(600) - 300) / 10000.0);
    }

    /** JPY crosses quote to 2dp, the rest to 4. */
    private static String fmt(String pair, double v) {
        return pair.endsWith("JPY") ? String.format("%.2f", v) : String.format("%.4f", v);
    }

    private static String clock(Random rnd) {
        return String.format("%02d:%02d:%02d", 8 + rnd.nextInt(9), rnd.nextInt(60), rnd.nextInt(60));
    }

    /** A plausible slice · surface · model stamp (P1 — every trade carries one). */
    private static String stamp(Random rnd) {
        return "C" + (190 + rnd.nextInt(15))
             + " · S" + (430 + rnd.nextInt(90)) + "/" + (10 + rnd.nextInt(80))
             + " · " + (rnd.nextBoolean() ? "VV-2.3" : "GK-1.8");
    }

    private static String pick(List<String> xs, Random rnd) { return xs.get(rnd.nextInt(xs.size())); }
}
