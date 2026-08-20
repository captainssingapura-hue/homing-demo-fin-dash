package hue.captains.singapura.js.homing.findash.core.css;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssImportsFor;

import java.util.List;

/**
 * Typography: five density steps, plus emphasis kept deliberately off the size
 * axis. Imported by all 39 widgets.
 *
 * <p>The widgets previously carried <b>ten</b> font sizes (10.5 / 11 / 11.5 /
 * 12 / 12.5 / 13 / 14 / 18 / 20 / 22). None of them encoded anything: 11px
 * almost always paired with {@code text-muted} and 11.5px with
 * {@code text-primary}, so the real variable was <em>colour</em>, not size;
 * 12.5px was the table-row size and 12px the control size, a distinction
 * expressed as a difference nobody can perceive; and 18/20/22 was three sizes
 * across six total uses.</p>
 *
 * <p>What does carry meaning is <b>density</b> — a blotter of forty rows
 * genuinely needs tighter text than a pricer with six fields — and
 * <b>emphasis</b>, which lives in {@link fd_strong}, {@link fd_strongest} and
 * {@link fd_muted} so it can never drift back into the size scale.</p>
 */
public record FdTextCss() implements CssGroup<FdTextCss> {

    public static final FdTextCss INSTANCE = new FdTextCss();

    // ---- density scale --------------------------------------------------

    /** Hero number — a risk total or P&amp;L readable at a glance (UI study P2). */
    public record fd_display() implements CssClass<FdTextCss> {
        @Override public String body() { return """
                font-size: 20px;
                font-weight: 700;
                """;
        }
    }

    /** Widget identity. The one size that was already used consistently (25 sites). */
    public record fd_title() implements CssClass<FdTextCss> {
        @Override public String body() { return """
                font-weight: 700;
                font-size: 14px;
                letter-spacing: 0.3px;
                """;
        }
    }

    /** Default reading text. */
    public record fd_body() implements CssClass<FdTextCss> {
        @Override public String body() { return "font-size: 13px;\n"; }
    }

    /** Table and blotter rows — the density tier that earns its place. */
    public record fd_dense() implements CssClass<FdTextCss> {
        @Override public String body() { return "font-size: 12px;\n"; }
    }

    /** Labels, lineage stamps, footnotes. */
    public record fd_caption() implements CssClass<FdTextCss> {
        @Override public String body() { return "font-size: 11px;\n"; }
    }

    // ---- emphasis, orthogonal to size -----------------------------------

    /** Heading for a block within a widget. */
    public record fd_section_title() implements CssClass<FdTextCss> {
        @Override public String body() { return """
                font-weight: 600;
                letter-spacing: 0.2px;
                """;
        }
    }

    public record fd_strong() implements CssClass<FdTextCss> {
        @Override public String body() { return "font-weight: 600;\n"; }
    }

    public record fd_strongest() implements CssClass<FdTextCss> {
        @Override public String body() { return "font-weight: 700;\n"; }
    }

    /** Secondary text. This is what 11px was really expressing. */
    public record fd_muted() implements CssClass<FdTextCss> {
        @Override public String body() { return "color: var(--color-text-muted);\n"; }
    }

    /**
     * Tabular figures. On a desk, columns of numbers must align on the digit —
     * proportional numerals make a blotter unreadable at a glance.
     */
    public record fd_num() implements CssClass<FdTextCss> {
        @Override public String body() { return "font-variant-numeric: tabular-nums;\n"; }
    }

    /**
     * Monospace — deal tickets and identifiers, where character alignment is
     * what makes two strings comparable at a glance. Replaces the kit's old
     * {@code fdk.tokens.mono} string, which was a font stack passed around as
     * data rather than a styling decision.
     */
    public record fd_mono() implements CssClass<FdTextCss> {
        @Override public String body() { return """
                font-family: ui-monospace, Menlo, Consolas, monospace;
                """;
        }
    }

    @Override
    public List<CssClass<FdTextCss>> cssClasses() {
        return List.of(
                new fd_display(),
                new fd_title(),
                new fd_body(),
                new fd_dense(),
                new fd_caption(),
                new fd_section_title(),
                new fd_strong(),
                new fd_strongest(),
                new fd_muted(),
                new fd_num(),
                new fd_mono());
    }

    @Override
    public CssImportsFor<FdTextCss> cssImports() {
        return new CssImportsFor<>(this, List.of());
    }
}
