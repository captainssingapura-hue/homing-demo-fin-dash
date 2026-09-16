package hue.captains.singapura.js.homing.findash.core.css;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;

import java.util.List;

/**
 * Semantic state — the desk's health vocabulary.
 *
 * <p><b>All 64 {@code no-literal-color} findings resolve here.</b> Every colour
 * the widgets had baked as hex was one of five states, repeated across 39
 * widgets that each picked their own literal. Naming the five states collapses
 * them to one definition apiece.</p>
 *
 * <p>Each body references a {@link FinDashVars} token with the previous literal
 * as the {@code var()} fallback — the idiom the framework's own demo groups
 * use. Nothing binds those tokens yet, so rendering is unchanged today; when a
 * theme binds them, every widget follows without being touched.</p>
 *
 * <p><b>Colour is never the only signal</b> (UI study P2). A chip carries an
 * icon and a label as well; these classes supply the tint, not the meaning.</p>
 */
public record FdStatusCss() implements CssGroup<FdStatusCss> {

    public static final FdStatusCss INSTANCE = new FdStatusCss();

    // ---- foreground -----------------------------------------------------

    public record fd_status_good() implements CssClass<FdStatusCss> {
        @Override public String body() { return "color: var(--color-status-good, #006300);\n"; }
    }

    public record fd_status_warn() implements CssClass<FdStatusCss> {
        @Override public String body() { return "color: var(--color-status-warn, #9a6b1f);\n"; }
    }

    public record fd_status_serious() implements CssClass<FdStatusCss> {
        @Override public String body() { return "color: var(--color-status-serious, #a8502a);\n"; }
    }

    public record fd_status_critical() implements CssClass<FdStatusCss> {
        @Override public String body() { return "color: var(--color-status-critical, #a32e2e);\n"; }
    }

    public record fd_status_neutral() implements CssClass<FdStatusCss> {
        @Override public String body() { return "color: var(--color-status-neutral, #52514e);\n"; }
    }

    // ---- background tints -----------------------------------------------

    public record fd_status_good_bg() implements CssClass<FdStatusCss> {
        @Override public String body() { return "background: var(--color-status-good-bg, #e6f4e6);\n"; }
    }

    public record fd_status_warn_bg() implements CssClass<FdStatusCss> {
        @Override public String body() { return "background: var(--color-status-warn-bg, #fdf3dc);\n"; }
    }

    public record fd_status_serious_bg() implements CssClass<FdStatusCss> {
        @Override public String body() { return "background: var(--color-status-serious-bg, #fceee8);\n"; }
    }

    public record fd_status_critical_bg() implements CssClass<FdStatusCss> {
        @Override public String body() { return "background: var(--color-status-critical-bg, #f9e4e4);\n"; }
    }

    public record fd_status_neutral_bg() implements CssClass<FdStatusCss> {
        @Override public String body() { return "background: var(--color-status-neutral-bg, #efefec);\n"; }
    }

    // ---- the chip -------------------------------------------------------

    /** Shape only; pair with a {@code fd-chip-*} variant for the state tint. */
    public record fd_chip() implements CssClass<FdStatusCss> {
        @Override public String body() { return """
                display: inline-block;
                font-size: 11px;
                font-weight: 600;
                border-radius: 999px;
                padding: 2px 10px;
                white-space: nowrap;
                """;
        }
    }

    public record fd_chip_good() implements CssClass<FdStatusCss> {
        @Override public String body() { return """
                color: var(--color-status-good, #006300);
                background: var(--color-status-good-bg, #e6f4e6);
                """;
        }
    }

    public record fd_chip_warn() implements CssClass<FdStatusCss> {
        @Override public String body() { return """
                color: var(--color-status-warn, #9a6b1f);
                background: var(--color-status-warn-bg, #fdf3dc);
                """;
        }
    }

    public record fd_chip_serious() implements CssClass<FdStatusCss> {
        @Override public String body() { return """
                color: var(--color-status-serious, #a8502a);
                background: var(--color-status-serious-bg, #fceee8);
                """;
        }
    }

    public record fd_chip_critical() implements CssClass<FdStatusCss> {
        @Override public String body() { return """
                color: var(--color-status-critical, #a32e2e);
                background: var(--color-status-critical-bg, #f9e4e4);
                """;
        }
    }

    public record fd_chip_neutral() implements CssClass<FdStatusCss> {
        @Override public String body() { return """
                color: var(--color-status-neutral, #52514e);
                background: var(--color-status-neutral-bg, #efefec);
                """;
        }
    }

    /**
     * Load/fetch failure text. The single most repeated literal in the repo
     * (30 sites of {@code #a8502a}) — every widget's catch block wrote its own.
     */
    public record fd_error_text() implements CssClass<FdStatusCss> {
        @Override public String body() { return "color: var(--color-status-serious, #a8502a);\n"; }
    }

    @Override
    public List<CssClass<FdStatusCss>> cssClasses() {
        return List.of(
                new fd_status_good(),
                new fd_status_warn(),
                new fd_status_serious(),
                new fd_status_critical(),
                new fd_status_neutral(),
                new fd_status_good_bg(),
                new fd_status_warn_bg(),
                new fd_status_serious_bg(),
                new fd_status_critical_bg(),
                new fd_status_neutral_bg(),
                new fd_chip(),
                new fd_chip_good(),
                new fd_chip_warn(),
                new fd_chip_serious(),
                new fd_chip_critical(),
                new fd_chip_neutral(),
                new fd_error_text());
    }
}
