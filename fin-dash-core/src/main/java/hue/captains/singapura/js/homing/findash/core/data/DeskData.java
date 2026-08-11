package hue.captains.singapura.js.homing.findash.core.data;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * The fabricated demo book — deterministic, hand-authored numbers lifted from
 * the UI study's wireframes (W1 pricer, W2 surface, W4 blotter) so every
 * persona's screens tell one coherent story: EURUSD marked and healthy, USDJPY
 * degraded (1M BF quarantined → surface stale → reval budget stretched), a
 * barrier cluster near current spot.
 *
 * <p>Plain Java (not a served module): persona modules' GET actions read these
 * records and serialise JSON. No randomness, no clock — the same request
 * always returns the same numbers (P5: two consumers at the same as-of see the
 * same number; the {@code deterministic-risk} rule applied server-side by
 * discipline).</p>
 */
public final class DeskData {

    private DeskData() {}

    /** The shared market-slice stamp (P1 lineage root). */
    public static final String SLICE = "C204";
    public static final String MODEL = "VV-2.3";

    // ---------------------------------------------------------------- surfaces

    public record SmilePoint(String label, double mkt, double fit) {}
    public record PillarRow(String name, double mkt, double fit, Double ovr) {}
    public record TenorSurface(String tenor, boolean flagged, String flagNote,
                               double resid, double tol,
                               List<SmilePoint> smile, List<PillarRow> pillars) {}
    public record PairSurface(String pair, String epoch, boolean stale, String staleNote,
                              String anchorDrift, String eventVols, String arbGates,
                              String provenance, List<TenorSurface> tenors) {}

    private static TenorSurface tenor(String t, boolean flagged, String note, double resid, double tol,
                                      double atm, double rr, double bf) {
        // Smile in delta space from (atm, rr25, bf25): puts above calls when rr < 0.
        double p25 = atm + bf - rr / 2, c25 = atm + bf + rr / 2;
        double p10 = atm + 2.6 * bf - 0.9 * rr, c10 = atm + 2.6 * bf + 0.9 * rr;
        List<SmilePoint> smile = List.of(
                new SmilePoint("10ΔP", round2(p10 + resid / 3), round2(p10)),
                new SmilePoint("25ΔP", round2(p25 + resid / 5), round2(p25)),
                new SmilePoint("ATM",       round2(atm),             round2(atm)),
                new SmilePoint("25ΔC", round2(c25 - resid / 5), round2(c25)),
                new SmilePoint("10ΔC", round2(c10 + resid / 2), round2(c10)));
        List<PillarRow> pillars = List.of(
                new PillarRow("ATM",  round2(atm), round2(atm), null),
                new PillarRow("RR25", round2(rr),  round2(rr + 0.02), null),
                new PillarRow("BF25", round2(bf),  round2(bf - 0.01), null));
        return new TenorSurface(t, flagged, note, resid, tol, smile, pillars);
    }

    public static final List<PairSurface> SURFACES = List.of(
            new PairSurface("EURUSD", "S513/87", false, null,
                    "0.3 of band", "NFP 05-Sep w=2.1 · FOMC 17-Sep w=3.0",
                    "butterfly ✓ · calendar ✓ · wings ✓",
                    "3 sources, composite 40/40/20",
                    List.of(tenor("ON", false, null, 0.10, 0.30, 8.60, -0.90, 0.24),
                            tenor("1W", false, null, 0.12, 0.30, 8.10, -1.00, 0.26),
                            tenor("1M", true,  "BF residual over tolerance", 0.36, 0.30, 7.95, -1.10, 0.30),
                            tenor("3M", false, null, 0.42, 0.50, 7.85, -1.20, 0.32),
                            tenor("6M", false, null, 0.18, 0.50, 7.80, -1.28, 0.33),
                            tenor("1Y", false, null, 0.14, 0.50, 7.78, -1.35, 0.35),
                            tenor("2Y", false, null, 0.11, 0.50, 7.80, -1.40, 0.36))),
            new PairSurface("USDJPY", "S498", true, "recalibration waiting on 1M BF quarantine",
                    "0.7 of band", "BoJ 19-Sep w=2.8",
                    "butterfly ✓ · calendar ⚠ margin thin · wings ✓",
                    "2 sources, composite 60/40 (broker C quarantined)",
                    List.of(tenor("ON", false, null, 0.15, 0.30, 9.40, 1.10, 0.28),
                            tenor("1W", false, null, 0.19, 0.30, 9.10, 1.20, 0.30),
                            tenor("1M", true,  "BF quarantined (jump filter)", 0.61, 0.30, 8.90, 1.35, 0.34),
                            tenor("3M", false, null, 0.22, 0.50, 8.75, 1.42, 0.36),
                            tenor("6M", false, null, 0.17, 0.50, 8.70, 1.50, 0.37),
                            tenor("1Y", false, null, 0.13, 0.50, 8.72, 1.55, 0.39),
                            tenor("2Y", false, null, 0.12, 0.50, 8.78, 1.60, 0.40))),
            new PairSurface("EURJPY", "S204 (cross)", false, null,
                    "0.2 of band", "—",
                    "butterfly ✓ · calendar ✓ · wings ✓",
                    "cross from EURUSD × USDJPY",
                    List.of(tenor("1M", false, null, 0.20, 0.40, 9.60, -0.40, 0.38),
                            tenor("3M", false, null, 0.16, 0.40, 9.45, -0.48, 0.40),
                            tenor("6M", false, null, 0.13, 0.40, 9.40, -0.55, 0.41))),
            new PairSurface("GBPUSD", "S471", false, null,
                    "0.4 of band", "MPC 18-Sep w=1.9",
                    "butterfly ✓ · calendar ✓ · wings ✓",
                    "3 sources, composite 45/35/20",
                    List.of(tenor("1W", false, null, 0.11, 0.30, 8.90, -0.70, 0.27),
                            tenor("1M", false, null, 0.14, 0.30, 8.70, -0.80, 0.29),
                            tenor("3M", false, null, 0.17, 0.50, 8.55, -0.92, 0.31),
                            tenor("6M", false, null, 0.12, 0.50, 8.50, -1.00, 0.32))),
            new PairSurface("AUDUSD", "S502", false, null,
                    "0.3 of band", "RBA 02-Sep w=1.4",
                    "butterfly ✓ · calendar ✓ · wings ✓",
                    "2 sources, composite 55/45",
                    List.of(tenor("1M", false, null, 0.13, 0.30, 9.80, -1.60, 0.33),
                            tenor("3M", false, null, 0.15, 0.50, 9.65, -1.72, 0.35),
                            tenor("6M", false, null, 0.12, 0.50, 9.60, -1.80, 0.36))));

    public static Optional<PairSurface> surface(String pair) {
        String p = pair == null ? "" : pair.toUpperCase(Locale.ROOT);
        return SURFACES.stream().filter(s -> s.pair().equals(p)).findFirst();
    }

    // ---------------------------------------------------------------- blotter

    /** One blotter row; {@code tenor == null} ⇒ the pair's group row. */
    public record BucketRow(String pair, String tenor, double delta, double vAtm,
                            double vRr, double vBf, double theta,
                            double freshSecs, double budgetFrac, String note) {}

    public static final List<BucketRow> BOOK = List.of(
            new BucketRow("EURUSD", null, 8.2e6, 212e3, -38e3, 11e3, -21e3, 2, 0.31, null),
            new BucketRow("EURUSD", "1W", 1.1e6,  18e3,  -2e3,  1e3,  -6e3, 2, 0.22, null),
            new BucketRow("EURUSD", "1M", 3.4e6,  92e3, -21e3,  6e3,  -9e3, 2, 0.28, null),
            new BucketRow("EURUSD", "3M", 3.7e6, 102e3, -15e3,  4e3,  -6e3, 2, 0.35, null),
            new BucketRow("USDJPY", null, -2.1e6, 148e3, 22e3,  9e3, -11e3, 45, 0.92, "budget 92% — wave queued"),
            new BucketRow("USDJPY", "1M", -0.8e6,  61e3,  9e3,  4e3,  -5e3, 45, 0.92, null),
            new BucketRow("USDJPY", "6M", -1.3e6,  87e3, 13e3,  5e3,  -6e3, 45, 0.90, null),
            new BucketRow("EURJPY", null, 0.4e6,  41e3,  -6e3,  3e3,  -4e3, 3, 0.41, null),
            new BucketRow("EURJPY", "3M", 0.4e6,  41e3,  -6e3,  3e3,  -4e3, 3, 0.41, null),
            new BucketRow("GBPUSD", null, 4.5e6,  52e3,  -8e3,  2e3,  -5e3, 2, 0.26, null),
            new BucketRow("GBPUSD", "1M", 1.9e6,  21e3,  -3e3,  1e3,  -2e3, 2, 0.24, null),
            new BucketRow("GBPUSD", "3M", 2.6e6,  31e3,  -5e3,  1e3,  -3e3, 2, 0.27, null),
            new BucketRow("AUDUSD", null, 1.4e6,  33e3,   4e3,  2e3,  -3e3, 4, 0.33, null),
            new BucketRow("AUDUSD", "3M", 1.4e6,  33e3,   4e3,  2e3,  -3e3, 4, 0.33, null));

    public record Totals(double delta, double vega, double theta, double vegaLimitFrac,
                         double revalWaveFrac) {}

    public static final Totals TOTALS = new Totals(12.4e6, 486e3, -38e3, 0.61, 0.43);

    public record BarrierWatch(String pair, String type, double level, int pips,
                               String notional, String note, String severity) {}

    public static final List<BarrierWatch> BARRIERS = List.of(
            new BarrierWatch("USDJPY", "NT", 152.00, 12, "€40M", "budget tightened ×8", "serious"),
            new BarrierWatch("EURUSD", "KO", 1.1200, 38, "€25M", "gamma flips at level", "warn"),
            new BarrierWatch("GBPUSD", "OT", 1.3550, 96, "€8M",  "quiet", "good"));

    // ------------------------------------------------------- expiries & pins

    /** A same-cut expiry cluster (bulk processing unit — UI study §10). */
    public record ExpiryCluster(String cut, String pair, String notional,
                                String note, String severity) {}

    public static final List<ExpiryCluster> EXPIRIES = List.of(
            new ExpiryCluster("NY 10am · today",    "EURUSD", "€120M",
                    "vanillas across 1.0800–1.0950", "warn"),
            new ExpiryCluster("NY 10am · today",    "USDJPY", "$45M",
                    "digitals at 151.50 — near NT level", "serious"),
            new ExpiryCluster("Tokyo 3pm · today",  "USDJPY", "$35M",
                    "quiet", "good"),
            new ExpiryCluster("NY 10am · tomorrow", "GBPUSD", "€28M",
                    "strangle wings 1.3400 / 1.3700", "good"));

    /** A pin-risk candidate: expiry-day open interest that can magnetise spot. */
    public record PinCandidate(String pair, double strike, String notional,
                               String note, String severity) {}

    public static final List<PinCandidate> PINS = List.of(
            new PinCandidate("EURUSD", 1.0900, "€45M", "0.4 figs away · theta magnet into the cut", "warn"),
            new PinCandidate("GBPUSD", 1.3500, "€12M", "1.1 figs away", "good"));

    // -------------------------------------------------------------- quoting

    /** One auto-quoting pair row (W3): state + reason when automatic. */
    public record QuotePair(String pair, String state, String stateNote, String spread,
                            String skew, String age, String hitPct, String volume) {}

    public static final List<QuotePair> QUOTING = List.of(
            new QuotePair("EURUSD", "streaming",    null,
                    "0.05", "+0.2Δ", "0.3s", "23%", "€340M"),
            new QuotePair("USDJPY", "auto-widened", "surface degraded (1M BF quarantined) → ×2.0 · rule QW-3",
                    "0.06→0.12", "−0.4Δ", "0.4s", "9%", "€185M"),
            new QuotePair("EURJPY", "pulled",       "manual — cross vol mismatch",
                    "—", "—", "412s", "—", "€22M"),
            new QuotePair("GBPUSD", "streaming",    null,
                    "0.07", "0", "0.5s", "18%", "€96M"),
            new QuotePair("AUDUSD", "streaming",    null,
                    "0.08", "+0.1Δ", "0.7s", "21%", "€41M"));

    /** The quoting event stream — automatic state changes and manual actions in
     *  the SAME stream (study §4: "why were we wide for 40 minutes" must have a
     *  scrollable answer). */
    public record QuoteEvent(String time, String kind, String text) {}

    public static final List<QuoteEvent> QUOTE_EVENTS = List.of(
            new QuoteEvent("14:31:07", "auto",   "USDJPY surface degraded (1M BF quarantined) → auto-widened ×2.0 · rule QW-3"),
            new QuoteEvent("14:12:40", "manual", "EURJPY pulled by supervisor · reason: cross vol mismatch · four-eyes pending (>30 min)"),
            new QuoteEvent("13:58:22", "auto",   "GBPUSD spread restored ×1.0 — surface healthy again"),
            new QuoteEvent("13:41:05", "auto",   "AUDUSD quote age breach (1.9s) → size cap ×0.5 · rule QA-1, expired 13:52"));

    /** One RFQ tape entry — every quote stamped with its slice (P1). */
    public record Rfq(String time, String source, String desc, String quote,
                      String outcome, String stamp) {}

    public static final List<Rfq> RFQS = List.of(
            new Rfq("14:32:07", "bank client A", "EURUSD 1M 25ΔP €25M",
                    "7.91 / 8.03", "WON",    "S513/87"),
            new Rfq("14:31:44", "platform RFQ",  "USDJPY 3M ATM $40M",
                    "wide (degraded)", "LOST", "S498"),
            new Rfq("14:30:12", "bank client B", "EURUSD 2W KO RKO €15M",
                    "quoted manual (desk)", "WON", "S513/86"),
            new Rfq("14:27:58", "bank client A", "GBPUSD 6M ATM €30M",
                    "8.49 / 8.61", "PENDING", "S471"));

    // ---------------------------------------------------------------- pricer

    public record Ticket(String pair, String tenor, double strike, String barrierType,
                         Double barrier, double notionalM, String type) {}

    public record Priced(Ticket ticket, String vol, double pvAmount, double pvPct,
                         double bid, double offer, double mid, double spread, double skew,
                         String delta, String gamma, String vega, String vanna,
                         String volga, String theta, String barrierNote,
                         List<double[]> spotLadder, double volUp, double volDown,
                         String slice, String epoch, String model) {}

    /** Desk shorthand: {@code eurusd 3m 1.0850 [ko|ki|nt|ot 1.1200] 10}. */
    public static Ticket parse(String shorthand) {
        String[] tok = shorthand.trim().toLowerCase(Locale.ROOT).split("\\s+");
        if (tok.length < 4) throw new IllegalArgumentException(
                "expected: <pair> <tenor> <strike> [ko|ki|nt|ot <level>] <notionalM>");
        String pair = tok[0].toUpperCase(Locale.ROOT);
        String tenorU = tok[1].toUpperCase(Locale.ROOT);
        double strike = Double.parseDouble(tok[2]);
        String bt = null; Double lvl = null; int i = 3;
        if (tok[i].equals("ko") || tok[i].equals("ki") || tok[i].equals("nt") || tok[i].equals("ot")) {
            bt = tok[i].toUpperCase(Locale.ROOT);
            lvl = Double.parseDouble(tok[i + 1]);
            i += 2;
        }
        double notional = Double.parseDouble(tok[i]);
        String type = bt == null ? "Vanilla Call"
                : ("KO".equals(bt) ? "Barrier KO Call" : "Barrier " + bt + " Call");
        return new Ticket(pair, tenorU, strike, bt, lvl, notional, type);
    }

    /** Deterministic parametric demo pricing, calibrated to the W1 wireframe. */
    public static Priced price(Ticket t) {
        TenorSurface ts = surface(t.pair()).map(ps -> ps.tenors().stream()
                        .filter(x -> x.tenor().equals(t.tenor())).findFirst()
                        .orElse(ps.tenors().get(ps.tenors().size() / 2)))
                .orElseThrow(() -> new IllegalArgumentException("unknown pair " + t.pair()));
        String epoch = surface(t.pair()).map(PairSurface::epoch).orElse("?");
        double atm = ts.pillars().get(0).mkt();
        double years = tenorYears(t.tenor());
        boolean ko = t.barrierType() != null;
        double pvPct = 0.4 * atm * Math.sqrt(years) * (ko ? 0.907 : 1.0);
        double pvAmount = pvPct / 100.0 * t.notionalM() * 1e6;
        double spread = 0.020 + 0.010 * Math.sqrt(years);
        double skew = 0.005;
        double mid = pvPct;
        double n = t.notionalM() / 10.0;         // scale factor vs the 10M canonical
        double[][] ladderRatio = { {-1.0, 0.689}, {-0.5, 0.840}, {0.0, 1.0},
                                   {0.5, 1.131}, {1.0, 1.181} };
        List<double[]> ladder = new java.util.ArrayList<>();
        for (double[] r : ladderRatio) ladder.add(new double[]{ r[0], pvAmount * r[1] });
        return new Priced(t, fdVol(atm, t.tenor()), pvAmount, pvPct,
                round3(mid - spread), round3(mid + spread + skew), round4(mid), spread, skew,
                ko ? "34.2 % (spot, prem-adj)" : "48.6 % (spot, prem-adj)",
                "1.2 %/fig",
                "EUR " + Math.round(12.4 * n) + "." + (Math.round(12.4 * n * 10) % 10) + "k",
                "−2.1k", "0.8k",
                "−0." + (int) Math.round(42 * n) + "k/day",
                ko ? "barrier " + round2(Math.abs(t.barrier() - t.strike()) * 100 / (t.pair().endsWith("JPY") ? 100 : 1))
                        + " figs away · KO band monitor: quiet" : null,
                ladder, pvAmount * 1.207, pvAmount * 0.791,
                SLICE, epoch, MODEL);
    }

    private static String fdVol(double atm, String tenor) {
        return round2(atm) + " (" + tenor + " smile)";
    }

    private static double tenorYears(String tenor) {
        return switch (tenor) {
            case "ON" -> 1.0 / 365; case "1W" -> 7.0 / 365; case "1M" -> 1 / 12.0;
            case "3M" -> 0.25; case "6M" -> 0.5; case "1Y" -> 1.0; case "2Y" -> 2.0;
            default -> 0.25;
        };
    }

    private static double round2(double x) { return Math.round(x * 100.0) / 100.0; }
    private static double round3(double x) { return Math.round(x * 1000.0) / 1000.0; }
    private static double round4(double x) { return Math.round(x * 10000.0) / 10000.0; }
}
