package hue.captains.singapura.js.homing.findash.core.css;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssImportsFor;

import java.util.List;

/**
 * Data display — tables, meters, and the two patterns every fin-dash widget
 * carries: the key/value fact row and the lineage stamp (UI study P1, every
 * number can explain itself).
 *
 * <p>Imported by the blotters, the grid primitive and the fact panels.</p>
 */
public record FdDataCss() implements CssGroup<FdDataCss> {

    public static final FdDataCss INSTANCE = new FdDataCss();

    // ---- tables ---------------------------------------------------------

    public record fd_table() implements CssClass<FdDataCss> {
        @Override public String body() { return """
                border-collapse: collapse;
                width: 100%;
                """;
        }
    }

    /**
     * Header cell. Sticky so the column names survive a 30-row scroll — the
     * blotter's whole point is reading a lot of rows without losing the header.
     */
    public record fd_th() implements CssClass<FdDataCss> {
        @Override public String body() { return """
                text-align: left;
                font-weight: 600;
                font-size: 11px;
                color: var(--color-text-muted);
                white-space: nowrap;
                padding: var(--space-1, 4px) var(--space-2, 8px);
                border-bottom: 1px solid var(--color-border);
                position: sticky;
                top: 0;
                background: var(--color-surface);
                """;
        }
    }

    public record fd_td() implements CssClass<FdDataCss> {
        @Override public String body() { return """
                font-size: 12px;
                white-space: nowrap;
                padding: var(--space-1, 4px) var(--space-2, 8px);
                border-bottom: 1px solid var(--color-border);
                """;
        }
    }

    /** Tighter cell padding for dense blotters ({@code compact: true}). */
    public record fd_cell_compact() implements CssClass<FdDataCss> {
        @Override public String body() { return "padding: 2px var(--space-2, 8px);\n"; }
    }

    /**
     * Numeric cell. Right-aligned and tabular so the digits line up — on a
     * blotter, a column you cannot scan is a column you cannot trade from.
     */
    public record fd_td_num() implements CssClass<FdDataCss> {
        @Override public String body() { return """
                text-align: right;
                font-variant-numeric: tabular-nums;
                """;
        }
    }

    /** Group header row — the pair in a pair ▸ tenor bucketing. */
    public record fd_row_group() implements CssClass<FdDataCss> {
        @Override public String body() { return "font-weight: 700;\n"; }
    }

    /** Child row under a group header. */
    public record fd_row_indent() implements CssClass<FdDataCss> {
        @Override public String body() { return "padding-left: var(--space-3, 12px);\n"; }
    }

    /**
     * A section-header cell — the row that OPENS a block, as against
     * {@link fd_total_row} which closes it. Worn by every cell of the row,
     * including the ones that carry nothing, because that is what draws an
     * unbroken strip across a grid that owns its own {@code <tr>}.
     *
     * <p>This one is a <b>divider</b>, so it is the loudest of the three, and
     * it borrows Minesweeper's vocabulary: the header is an <b>unopened
     * cell</b> — a raised tile with a 2px highlight along its top edge and a
     * 2px shadow along its bottom — while the rows beneath it are the opened
     * ones, flat on the grid's own hairlines. Both edges are <b>inset
     * shadows</b> rather than borders, the same reasoning the grid itself uses
     * for its sticky header, so the bevel stays out of the box model.</p>
     *
     * <p>The tile does <em>not</em> press when its block folds. An inverted
     * bevel for the folded state was tried and read as a stray rule under the
     * bar; the caret in the label carries the fold, and the row keeps the same
     * face, height and depth either way.</p>
     *
     * <p>The face is a little <b>ink mixed into the raised surface</b>, not the
     * raised surface itself. {@code surface-raised} is defined relative to
     * {@code surface} and in some themes the two are a hair apart — white on
     * near-white in the default light theme — which left the band invisible
     * wherever it was needed most. The text colour, by contrast, is guaranteed
     * to read against the surface in <em>every</em> theme, so a small share of
     * it is a step off the page that stays a step off the page. It is declared
     * once, as a custom property, so both bevel edges derive from the colour
     * they sit on.</p>
     *
     * <p>The edges mix toward {@code white} and {@code black} — absolutes, on
     * purpose. A bevel is about <b>light</b>, not ink, and light falls from the
     * top in a dark theme exactly as in a light one; a highlight built from
     * theme tokens would flip into a shadow the moment the tokens did. The two
     * keywords are neither hex nor {@code rgb()}, which is what the
     * literal-colour rule guards against.</p>
     *
     * <p>Top and bottom only. The row is <em>one</em> control — a double-click
     * or Enter anywhere along it folds the block — so it gets one bevel, and
     * the table's own edges close the left and right.</p>
     *
     * <p>The explicit {@code line-height}/{@code min-height} pair is load
     * bearing. A cell element with no text collapses to zero height, so a
     * header whose other columns are void would paint its fill on the label
     * alone and leave the strip broken. Fixing both to the same value makes an
     * empty cell exactly as tall as a written one.</p>
     *
     * <p>Unlike {@link fd_total_row} the fill is <b>not</b> translucent, so a
     * grid's selection tint does not show through here. That is the accepted
     * cost: a section header is chrome rather than data — nothing on it is
     * editable or worth copying — and the cursor is drawn as an outline, which
     * paints over the fill regardless.</p>
     */
    public record fd_section_row() implements CssClass<FdDataCss> {
        @Override public String body() {
            return """
                   --fd-bevel-face: color-mix(in srgb, var(--color-text-primary) 12%,
                                                        var(--color-surface-raised));
                   line-height: 18px;
                   min-height: 18px;
                   background: var(--fd-bevel-face);
                   color: var(--color-text-primary);
                   font-weight: 700;
                   letter-spacing: 0.04em;
                   box-shadow: inset 0  2px 0 color-mix(in srgb, white 60%, var(--fd-bevel-face)),
                               inset 0 -2px 0 color-mix(in srgb, black 35%, var(--fd-bevel-face));
                   """;
        }
    }

    /**
     * A subtotal cell — one per cell of an aggregate row, which is what draws a
     * band across a grid that owns its own {@code <tr>}.
     *
     * <p>The rule sits on <b>top</b>: a subtotal closes the block above it, so
     * the line belongs between the last leaf and the total, not under it. The
     * fill is deliberately translucent — a grid may tint the cell behind this
     * one to show selection, and an opaque band would swallow it.</p>
     */
    public record fd_total_row() implements CssClass<FdDataCss> {
        @Override public String body() {
            return """
                   border-top: 1px solid var(--color-border);
                   background: color-mix(in srgb, var(--color-surface-raised) 60%, transparent);
                   font-weight: 600;
                   """;
        }
    }

    /**
     * A grand-total cell — the same idea one step louder, so the book total is
     * not mistaken for one more subtotal at a glance.
     */
    public record fd_grand_row() implements CssClass<FdDataCss> {
        @Override public String body() {
            return """
                   border-top: 2px solid var(--color-text-muted);
                   background: color-mix(in srgb, var(--color-accent) 8%, transparent);
                   font-weight: 700;
                   """;
        }
    }

    // ---- meter ----------------------------------------------------------

    /** The track. Pair with a {@code fd-status-*-bg} for severity. */
    public record fd_meter() implements CssClass<FdDataCss> {
        @Override public String body() { return """
                display: inline-block;
                height: 6px;
                border-radius: 3px;
                vertical-align: middle;
                """;
        }
    }

    /**
     * The fill. Its width is live data, so it cannot be a static class: the
     * widget sets {@code --fd-meter-frac} with
     * {@code style.setProperty('--fd-meter-frac', f)} and this rule consumes
     * it. That is the custom-property escape hatch {@code no-inline-style}
     * explicitly permits, and the only place in the vocabulary that needs it.
     */
    public record fd_meter_fill() implements CssClass<FdDataCss> {
        @Override public String body() { return """
                display: block;
                height: 6px;
                border-radius: 3px;
                width: calc(var(--fd-meter-frac, 0) * 100%);
                """;
        }
    }

    // ---- fact rows and lineage ------------------------------------------

    public record fd_kv_row() implements CssClass<FdDataCss> {
        @Override public String body() { return """
                display: flex;
                gap: var(--space-2, 8px);
                line-height: 1.6;
                """;
        }
    }

    public record fd_kv_label() implements CssClass<FdDataCss> {
        @Override public String body() { return """
                color: var(--color-text-muted);
                min-width: 110px;
                """;
        }
    }

    public record fd_kv_value() implements CssClass<FdDataCss> {
        @Override public String body() { return "color: var(--color-text-primary);\n"; }
    }

    /** P1 — slice / surface epoch / model, always visible on a number. */
    public record fd_stamp() implements CssClass<FdDataCss> {
        @Override public String body() { return """
                color: var(--color-text-muted);
                font-size: 11px;
                white-space: nowrap;
                """;
        }
    }

    /**
     * A FENCE — the slot a Relation Grid group offers between two of its
     * tables, filled by the domain (RFC 0050 · Episode 2). The group draws
     * nothing in it and reads nothing back, so its look is entirely the desk's:
     * a pair caption wears {@link fd_section_row}'s bevel on top of this, so the
     * caption reads as an unopened cell and the tables under it as opened ones.
     * This class is only the geometry — a row that lays its controls out in a
     * line and takes the group's width.
     */
    public record fd_fence() implements CssClass<FdDataCss> {
        @Override public String body() { return """
                display: flex;
                align-items: center;
                gap: 6px;
                padding: 0 6px;
                box-sizing: border-box;
                width: 100%;
                """;
        }
    }

    /**
     * The fence's fold toggle. Drawn as a bare glyph, not a button: the bevel
     * behind it is already the affordance, and a second raised surface inside
     * a raised surface reads as a mistake. It is the fence's FIRST control, so
     * Enter on the fence stop presses it.
     */
    public record fd_fence_toggle() implements CssClass<FdDataCss> {
        @Override public String body() { return """
                appearance: none;
                border: 0;
                background: transparent;
                color: inherit;
                font: inherit;
                line-height: inherit;
                padding: 0 2px;
                cursor: pointer;
                """;
        }
    }

    /** The fence's caption text; the bevel and weight are the row's. */
    public record fd_fence_name() implements CssClass<FdDataCss> {
        @Override public String body() { return """
                flex: 1;
                white-space: nowrap;
                """;
        }
    }

    @Override
    public List<CssClass<FdDataCss>> cssClasses() {
        return List.of(
                new fd_table(),
                new fd_th(),
                new fd_td(),
                new fd_cell_compact(),
                new fd_td_num(),
                new fd_row_group(),
                new fd_row_indent(),
                new fd_section_row(),
                new fd_total_row(),
                new fd_grand_row(),
                new fd_meter(),
                new fd_meter_fill(),
                new fd_kv_row(),
                new fd_kv_label(),
                new fd_kv_value(),
                new fd_stamp(),
                new fd_fence(),
                new fd_fence_toggle(),
                new fd_fence_name());
    }

    @Override
    public CssImportsFor<FdDataCss> cssImports() {
        return new CssImportsFor<>(this, List.of());
    }
}
