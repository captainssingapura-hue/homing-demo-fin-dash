package hue.captains.singapura.js.homing.findash.core.theme;

import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.core.ThemeGlobals;
import hue.captains.singapura.js.homing.core.ThemeVariables;
import hue.captains.singapura.js.homing.studio.base.theme.HomingDefault;
import hue.captains.singapura.js.homing.studio.base.theme.StudioVars;

import java.util.Map;

/**
 * The terminal theme — near-black surfaces, amber text and accent, square
 * corners, monospace body: the look a trading floor has had since the 1980s,
 * and the reason it persists is legibility at a glance in a dim room.
 *
 * <p>A single <b>dark identity</b>: both modes stay dark, so the dark-mode
 * override repeats the same values rather than inverting them. A terminal that
 * turned white in daylight would not be this theme.</p>
 *
 * <p>Authored per {@code docs/adding-a-theme.md}. Only the token bindings live
 * here; the structural CSS is reused verbatim from {@link HomingDefault}, which
 * is what makes a theme a palette swap rather than a fork of the chrome.</p>
 *
 * <p><b>Scope note (P2):</b> this retints the studio <i>chrome</i>. The UI
 * study's status language — degraded, stale, breach — lives in the widgets'
 * own palette (the {@code fdk.status} tokens), so a chip stays amber-on-black
 * only insofar as the kit says so. Unifying those is a separate, deliberate
 * piece of work; see the pitfalls section of the guide.</p>
 */
public record FinDashTerminal() implements Theme {

    public static final FinDashTerminal INSTANCE = new FinDashTerminal();

    @Override public String slug()  { return "fd-terminal"; }
    @Override public String label() { return "Terminal (amber)"; }

    /**
     * The border colours, named once. They are needed in three places — the
     * token map, the dark-mode override, and the divider overlay — and having
     * previously edited two of the three and shipped a half-change, they are
     * constants rather than repeated literals.
     */
    static final String BORDER = "#FF9900";
    static final String BORDER_EMPHASIS = "#FFD54F";

    /** The token bindings — every colour role bound; a missing one renders empty. */
    public record Vars() implements ThemeVariables<FinDashTerminal> {

        public static final Vars INSTANCE = new Vars();

        @Override public FinDashTerminal theme() { return FinDashTerminal.INSTANCE; }
        @Override public Map<CssVar, String> values() { return VALUES; }

        private static final Map<CssVar, String> VALUES = Map.ofEntries(
                // Surfaces — near-black, with the header band a shade lifted.
                Map.entry(StudioVars.COLOR_SURFACE,          "#0A0A0A"),
                Map.entry(StudioVars.COLOR_SURFACE_RAISED,   "#141414"),
                Map.entry(StudioVars.COLOR_SURFACE_RECESSED, "#000000"),
                Map.entry(StudioVars.COLOR_SURFACE_INVERTED, "#1C1C1C"),
                // Text — amber on black; links stay functional cyan so a link
                // never reads as just another piece of amber chrome.
                Map.entry(StudioVars.COLOR_TEXT_PRIMARY,           "#FF9900"),
                Map.entry(StudioVars.COLOR_TEXT_MUTED,             "#B0741A"),
                Map.entry(StudioVars.COLOR_TEXT_ON_INVERTED,       "#FFB84D"),
                Map.entry(StudioVars.COLOR_TEXT_ON_INVERTED_MUTED, "#C99A4A"),
                Map.entry(StudioVars.COLOR_TEXT_LINK,              "#4FC3F7"),
                Map.entry(StudioVars.COLOR_TEXT_LINK_HOVER,        "#81D4FA"),
                // Borders — bright amber rules. On a near-black surface a neutral
                // grey hairline (#2A2A2A ≈ 1.3:1) is simply not there, and even a
                // dimmed amber reads as murky against the panes it is meant to
                // separate. The frame is structure, so it is drawn at full accent
                // strength — the same line already visible under the header.
                Map.entry(StudioVars.COLOR_BORDER,          BORDER),
                Map.entry(StudioVars.COLOR_BORDER_EMPHASIS, BORDER_EMPHASIS),
                // Accent — amber, with black text on top of it.
                Map.entry(StudioVars.COLOR_ACCENT,          "#FF9900"),
                Map.entry(StudioVars.COLOR_ACCENT_EMPHASIS, "#FFB74D"),
                Map.entry(StudioVars.COLOR_ACCENT_ON,       "#000000"),
                // Spacing — the framework scale, unchanged.
                Map.entry(StudioVars.SPACE_1, "4px"),  Map.entry(StudioVars.SPACE_2, "8px"),
                Map.entry(StudioVars.SPACE_3, "12px"), Map.entry(StudioVars.SPACE_4, "16px"),
                Map.entry(StudioVars.SPACE_5, "20px"), Map.entry(StudioVars.SPACE_6, "24px"),
                Map.entry(StudioVars.SPACE_7, "32px"), Map.entry(StudioVars.SPACE_8, "40px"),
                // Radius — square, because terminals are.
                Map.entry(StudioVars.RADIUS_SM, "0px"),
                Map.entry(StudioVars.RADIUS_MD, "0px"),
                Map.entry(StudioVars.RADIUS_LG, "0px")
        );
    }

    /** Dark override + the shared structural CSS + the monospace overlay, in that order. */
    public record Globals() implements ThemeGlobals<FinDashTerminal> {

        public static final Globals INSTANCE = new Globals();

        @Override public FinDashTerminal theme() { return FinDashTerminal.INSTANCE; }

        @Override
        public String css() {
            return DARK_OVERRIDE + HomingDefault.STRUCTURAL_CSS + OVERLAY;
        }

        // Single dark identity: the @media block repeats the same values, so the
        // theme looks the same whatever the OS is set to.
        private static final String DARK_OVERRIDE = """
                :root { color-scheme: dark; }
                @media (prefers-color-scheme: dark) {
                    :root {
                        --color-surface:           #0A0A0A;
                        --color-surface-raised:    #141414;
                        --color-surface-recessed:  #000000;
                        --color-surface-inverted:  #1C1C1C;
                        --color-text-primary:            #FF9900;
                        --color-text-muted:              #B0741A;
                        --color-text-on-inverted:        #FFB84D;
                        --color-text-on-inverted-muted:  #C99A4A;
                        --color-text-link:               #4FC3F7;
                        --color-text-link-hover:         #81D4FA;
                        --color-border:           %s;
                        --color-border-emphasis:  %s;
                        --color-accent:           #FF9900;
                        --color-accent-emphasis:  #FFB74D;
                        --color-accent-on:        #000000;
                    }
                }
                """.formatted(BORDER, BORDER_EMPHASIS);

        // Tier-3 overlay. MUST follow STRUCTURAL_CSS: its `background:
        // var(--color-surface)` shorthand would otherwise clear what we set here.
        //
        // The split-pane dividers are the second half of the border story, and
        // they are NOT token-driven: SplitPaneModule.js hardcodes
        //   .hsp-divider{background:rgba(0,0,0,0.08)}   (:hover / .hsp-active → 0.22)
        // Black at 8% opacity is a hairline on white and nothing at all on a
        // near-black surface — so on any dark theme the panes lose the only
        // thing separating them. Retinted here to the same amber rules as every
        // other frame, with the emphasis colour on hover/drag so the grab
        // target announces itself.
        //
        // `!important` is load-bearing, not laziness. The server wraps this
        // stylesheet in `@layer theme`, and SplitPaneModule injects its rule at
        // runtime as UNLAYERED css. Unlayered normal declarations beat layered
        // ones outright — specificity is only compared within a layer — so no
        // selector written here can win by being more specific. An important
        // declaration reverses that precedence, and is the only lever available
        // from inside the layer. Remove it the moment the upstream default
        // becomes token-driven.
        private static final String OVERLAY = """
                html, body {
                    font-family: "Cascadia Mono", "Consolas", "SF Mono", "Liberation Mono", monospace;
                }
                .hsp-divider {
                    background: var(--color-border) !important;
                }
                .hsp-divider:hover,
                .hsp-divider.hsp-active {
                    background: var(--color-border-emphasis) !important;
                }
                """;
    }
}
