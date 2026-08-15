package hue.captains.singapura.js.homing.findash.core.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Simulates a trade's post-execution lifecycle.
 *
 * <p>Three stages, in order:</p>
 *
 * <ol>
 *   <li><b>Scheme by product type.</b> A knock-out barrier, a one-touch, a
 *       digital and a vanilla do not share a lifecycle — the barrier has a
 *       monitoring period that can terminate it early, the digital resolves at
 *       a single fixing, the vanilla runs to expiry and is exercised or
 *       abandoned. The scheme is read off the ticket, which is where the desk
 *       already writes the product.</li>
 *   <li><b>The trade's own facts as seed.</b> Its id and ticket derive the
 *       random seed, so a given trade's lifecycle is the <b>same on every
 *       call</b>. That is not a detail: a demo where the audit trail changes
 *       under refresh would contradict the thing this desk is about (P4 — any
 *       screen any participant saw is reconstructible), and the golden-replay
 *       determinism the platform console reports would be a lie.</li>
 *   <li><b>Per-trade randomness.</b> Within the scheme, the seeded stream
 *       decides the branches — did the barrier trigger, did confirmation break,
 *       was the fixing disputed, is settlement still pending — and jitters the
 *       timings. Two vanillas booked minutes apart get different stories.</li>
 * </ol>
 *
 * <p>Pure: no I/O, no clock, no shared state. {@link #of} is a function of the
 * trade alone, which is what makes it replayable.</p>
 */
public final class TradeLifecycle {

    private TradeLifecycle() {}

    /** One journaled step in a trade's life. */
    public record Event(String at, String kind, String label, String detail,
                        String actor, String severity) {}

    /** The simulated life of one trade: which scheme applied, and what happened. */
    public record Result(String tradeId, String scheme, String schemeNote,
                         String outcome, List<Event> events) {}

    /**
     * Product schemes. The desk writes the product into the ticket
     * ({@code ko}, {@code ot}, {@code digital}, else vanilla), so that is where
     * this reads it from rather than inventing a parallel classification.
     */
    public enum Scheme {
        VANILLA("European vanilla — runs to expiry, then exercised or abandoned"),
        BARRIER("Knock-out barrier — monitored continuously; a touch ends it early"),
        ONE_TOUCH("One-touch — pays on touch at any time before expiry"),
        DIGITAL("Digital — resolves at a single fixing, pays all or nothing");

        public final String note;
        Scheme(String note) { this.note = note; }
    }

    /** Reads the product off the ticket the desk typed. */
    public static Scheme schemeOf(DeskData.Trade t) {
        String s = t.ticket().toLowerCase();
        if (s.contains(" ot ") || s.endsWith(" ot")) return Scheme.ONE_TOUCH;
        if (s.contains("digital"))                   return Scheme.DIGITAL;
        if (s.contains(" ko ") || s.contains(" ki ")) return Scheme.BARRIER;
        return Scheme.VANILLA;
    }

    /**
     * The seed. Derived from facts that identify the trade and never change, so
     * the same trade yields the same life on every call, in every process.
     * {@code String.hashCode} is specified by the JDK, so this is stable across
     * JVMs and restarts in a way an identity hash would not be.
     */
    private static long seedOf(DeskData.Trade t) {
        return (long) t.id().hashCode() * 31L + t.ticket().hashCode();
    }

    public static Result of(DeskData.Trade t) {
        Scheme scheme = schemeOf(t);
        Random rnd = new Random(seedOf(t));
        List<Event> ev = new ArrayList<>();

        // ---- common opening: every trade is booked, then confirmed --------
        ev.add(new Event(t.time(), "booked", "Booked",
                t.trader() + " · " + t.notional() + " · PV at booking " + t.pvAtBooking(),
                t.trader(), "good"));

        boolean confirmBreak = rnd.nextInt(100) < 25;
        ev.add(confirmBreak
                ? new Event(plus(t.time(), rnd, 20, 90), "confirm", "Confirmation mismatch",
                        "counterparty economics differ — raised to middle office",
                        "MO A", "serious")
                : new Event(plus(t.time(), rnd, 5, 40), "confirm", "Confirmed",
                        "economics matched against counterparty",
                        "MO A", "good"));
        if (confirmBreak) {
            ev.add(new Event(plus(t.time(), rnd, 90, 240), "confirm", "Confirmation agreed",
                    "amended to match · four-eyes: MO B ✓", "MO B", "good"));
        }

        // ---- the amendments already on the journal are real, not simulated --
        for (DeskData.Amendment a : t.amendments()) {
            ev.add(new Event(a.when(), "amend", "Amended", a.what()
                    + " · " + a.pnlImpact() + " · " + a.approval(), a.who(), "warn"));
        }

        // ---- the scheme's own middle ------------------------------------
        String outcome = switch (scheme) {
            case BARRIER   -> barrier(t, rnd, ev);
            case ONE_TOUCH -> oneTouch(t, rnd, ev);
            case DIGITAL   -> digital(t, rnd, ev);
            case VANILLA   -> vanilla(t, rnd, ev);
        };

        return new Result(t.id(), scheme.name(), scheme.note, outcome, List.copyOf(ev));
    }

    // ---- schemes --------------------------------------------------------

    private static String vanilla(DeskData.Trade t, Random rnd, List<Event> ev) {
        ev.add(new Event(tenorDay(t), "fixing", "Expiry fixing captured",
                "WMR " + fixRate(t, rnd) + " · NY 10am cut", "auto", "neutral"));
        boolean itm = rnd.nextInt(100) < 55;
        if (itm) {
            ev.add(new Event(tenorDay(t), "exercise", "Exercised",
                    "in the money at the cut · auto-exercise per config", "auto", "good"));
            return settlement(t, rnd, ev);
        }
        ev.add(new Event(tenorDay(t), "expiry", "Expired worthless",
                "out of the money at the cut · no exercise", "auto", "neutral"));
        return "expired OTM";
    }

    private static String barrier(DeskData.Trade t, Random rnd, List<Event> ev) {
        ev.add(new Event(plus(t.time(), rnd, 60, 180), "monitor", "Barrier monitoring active",
                "continuous observation · fixing source WMR", "auto", "neutral"));
        boolean knocked = rnd.nextInt(100) < 40;
        if (knocked) {
            ev.add(new Event(tenorDay(t), "barrier", "Knocked out",
                    "barrier touched · determination recorded, trade terminated",
                    "auto", "serious"));
            ev.add(new Event(tenorDay(t), "determination", "Determination reviewed",
                    "touch confirmed against WMR · no dispute raised", "MO A", "good"));
            return "knocked out";
        }
        ev.add(new Event(tenorDay(t), "barrier", "Survived to expiry",
                "barrier never touched · monitoring closed", "auto", "good"));
        return vanilla(t, rnd, ev);
    }

    private static String oneTouch(DeskData.Trade t, Random rnd, List<Event> ev) {
        ev.add(new Event(plus(t.time(), rnd, 60, 180), "monitor", "Touch monitoring active",
                "continuous observation until expiry", "auto", "neutral"));
        boolean touched = rnd.nextInt(100) < 45;
        if (touched) {
            ev.add(new Event(tenorDay(t), "touch", "Touched — payout due",
                    "level touched before expiry · full payout triggered", "auto", "warn"));
            return settlement(t, rnd, ev);
        }
        ev.add(new Event(tenorDay(t), "expiry", "Expired untouched",
                "level never reached · no payout", "auto", "neutral"));
        return "expired untouched";
    }

    private static String digital(DeskData.Trade t, Random rnd, List<Event> ev) {
        boolean disputed = rnd.nextInt(100) < 20;
        ev.add(new Event(tenorDay(t), "fixing", "Fixing captured",
                "WMR " + fixRate(t, rnd) + (disputed ? " · counterparty queried the print" : ""),
                "auto", disputed ? "warn" : "neutral"));
        if (disputed) {
            ev.add(new Event(tenorDay(t), "fixing", "Fixing upheld",
                    "reviewed against source · original print stands", "MO B", "good"));
        }
        boolean pays = rnd.nextInt(100) < 50;
        ev.add(new Event(tenorDay(t), "determination", pays ? "Paid" : "Did not pay",
                pays ? "condition met at the fixing · full payout"
                     : "condition not met at the fixing · zero payout",
                "auto", pays ? "good" : "neutral"));
        return pays ? settlement(t, rnd, ev) : "no payout";
    }

    // ---- tail -----------------------------------------------------------

    private static String settlement(DeskData.Trade t, Random rnd, List<Event> ev) {
        boolean pending = rnd.nextInt(100) < 30;
        if (pending) {
            ev.add(new Event(tenorDay(t), "settlement", "Settlement pending",
                    "awaiting counterparty confirmation of payment instructions",
                    "Ops", "warn"));
            return "settlement pending";
        }
        ev.add(new Event(tenorDay(t), "settlement", "Settled",
                "cash delivered · reconciled against the journal", "Ops", "good"));
        return "settled";
    }

    // ---- small helpers ---------------------------------------------------

    /** A clock time offset by a seeded number of minutes; falls back for date-form stamps. */
    private static String plus(String at, Random rnd, int minLo, int minHi) {
        int add = minLo + rnd.nextInt(Math.max(1, minHi - minLo));
        int colon = at.indexOf(':');
        if (colon < 0) return at;                       // "08-Aug" — a date, leave it
        try {
            int h = Integer.parseInt(at.substring(0, colon));
            int m = Integer.parseInt(at.substring(colon + 1, colon + 3));
            int total = (h * 60 + m + add) % (24 * 60);
            return String.format("%02d:%02d", total / 60, total % 60);
        } catch (NumberFormatException e) {
            return at;
        }
    }

    /** The tenor's landing day, read off the ticket ("3m" → "+3M"). */
    private static String tenorDay(DeskData.Trade t) {
        for (String part : t.ticket().split(" ")) {
            if (part.matches("\\d+[dwmy]")) return "expiry " + part.toUpperCase();
        }
        return "expiry";
    }

    private static String fixRate(DeskData.Trade t, Random rnd) {
        for (String part : t.ticket().split(" ")) {
            if (part.matches("\\d+\\.\\d+")) {
                double strike = Double.parseDouble(part);
                double drift = (rnd.nextInt(200) - 100) / 10000.0 * strike;
                return String.format("%.4f", strike + drift);
            }
        }
        return "n/a";
    }
}
