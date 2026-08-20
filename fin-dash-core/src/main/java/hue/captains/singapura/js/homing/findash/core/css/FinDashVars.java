package hue.captains.singapura.js.homing.findash.core.css;

import hue.captains.singapura.js.homing.core.CssVar;

/**
 * The fin-dash token vocabulary — the roles the framework's {@code StudioVars}
 * does not cover.
 *
 * <p>All of them are <b>status</b> roles. A trading desk's UI has to say what
 * state something is in (UI study P2: a degraded state must be loud, and never
 * signalled by colour alone), and the framework's semantic set stops at
 * surface / text / border / accent. These fill exactly that gap and nothing
 * more — anything the framework already names is referenced, not re-declared.</p>
 *
 * <p><b>Deliberately unbound.</b> No theme binds these yet. Class bodies in
 * {@link FdStatusCss} reference them with the pre-existing literal as the
 * {@code var()} fallback, so today's rendering is pixel-identical while the
 * <em>structure</em> is already correct. Choosing validated light/dark values
 * is then a theme-only edit — one file, touching no widget — and the fallbacks
 * become dead and can be dropped once every theme binds them.</p>
 *
 * <p>That separation is the whole point of tokenising: declaring a token is
 * cheap and unblocks the discipline; designing its value is a different job
 * that no longer blocks anything.</p>
 */
public final class FinDashVars {

    private FinDashVars() {}

    // Status roles — foreground (text/icon) …
    public static final CssVar COLOR_STATUS_GOOD     = new CssVar("--color-status-good");
    public static final CssVar COLOR_STATUS_WARN     = new CssVar("--color-status-warn");
    public static final CssVar COLOR_STATUS_SERIOUS  = new CssVar("--color-status-serious");
    public static final CssVar COLOR_STATUS_CRITICAL = new CssVar("--color-status-critical");
    public static final CssVar COLOR_STATUS_NEUTRAL  = new CssVar("--color-status-neutral");

    // … and the matching tint each sits on.
    public static final CssVar COLOR_STATUS_GOOD_BG     = new CssVar("--color-status-good-bg");
    public static final CssVar COLOR_STATUS_WARN_BG     = new CssVar("--color-status-warn-bg");
    public static final CssVar COLOR_STATUS_SERIOUS_BG  = new CssVar("--color-status-serious-bg");
    public static final CssVar COLOR_STATUS_CRITICAL_BG = new CssVar("--color-status-critical-bg");
    public static final CssVar COLOR_STATUS_NEUTRAL_BG  = new CssVar("--color-status-neutral-bg");

    /**
     * The meter's fill fraction — the one genuinely dynamic value in the
     * vocabulary. Live data cannot be a static class, so a widget sets this
     * custom property and {@code fd-meter-fill} consumes it as
     * {@code calc(var(--fd-meter-frac) * 100%)}. This is the custom-property
     * escape hatch {@code no-inline-style} explicitly permits — and the only
     * place fin-dash needs it.
     */
    public static final CssVar FD_METER_FRAC = new CssVar("--fd-meter-frac");
}
