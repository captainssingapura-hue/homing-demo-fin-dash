package hue.captains.singapura.js.homing.findash.core.data;

/**
 * The desk's tolerances — the numbers that turn a measurement into a
 * <b>verdict</b> — and the one place a verdict is made.
 *
 * <p>These are desk facts, not UI facts. "Ten seconds is stale" is the reval
 * budget the desk runs to; "75% of an error budget is a warning" is the SLO
 * policy platform operations signed. A widget that compares {@code freshSecs
 * > 10} has copied a policy into a screen, where it will drift from the next
 * screen that copies it (this desk had four copies, three of them on the
 * freshness cut-off alone) and where it violates UI-study P5 outright: the
 * UI is a consumer of the desk's judgement, not the place it is made.</p>
 *
 * <p>So the actions publish the verdict beside the number — {@code freshState}
 * beside {@code freshSecs}, {@code budgetState} beside {@code budgetFrac} —
 * and a widget renders the state and never compares. Two consumers at the
 * same as-of see the same verdict because there is one function producing
 * it.</p>
 *
 * <p>Sources, so the numbers are not folklore: the freshness cut-off is the
 * desk's reval-wave budget (see {@code DeskData.SLOS}, "reval-wave lag"); the
 * budget bands are the SLO error-budget policy — warn at three quarters
 * consumed, over at nine tenths.</p>
 */
public final class DeskTolerances {

    private DeskTolerances() {}

    /** A reading older than this is stale: past the reval-wave budget. */
    public static final int FRESH_STALE_SECS = 10;

    /** Error-budget fraction at which a budget is a warning. */
    public static final double BUDGET_WARN = 0.75;

    /** Error-budget fraction at which a budget is over. */
    public static final double BUDGET_OVER = 0.90;

    public static final String FRESH = "fresh";
    public static final String STALE = "stale";
    public static final String OK    = "ok";
    public static final String WARN  = "warn";
    public static final String OVER  = "over";

    /**
     * The freshness verdict for a reading's age, or {@code null} when the
     * desk published no age — "not published" is carried, never invented.
     */
    public static String freshState(Double secs) {
        if (secs == null) return null;
        return secs > FRESH_STALE_SECS ? STALE : FRESH;
    }

    /** The budget verdict for a consumed fraction, or {@code null} when none was published. */
    public static String budgetState(Double frac) {
        if (frac == null) return null;
        if (frac >= BUDGET_OVER) return OVER;
        if (frac >= BUDGET_WARN) return WARN;
        return OK;
    }
}
