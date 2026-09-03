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
                new fd_total_row(),
                new fd_grand_row(),
                new fd_meter(),
                new fd_meter_fill(),
                new fd_kv_row(),
                new fd_kv_label(),
                new fd_kv_value(),
                new fd_stamp());
    }

    @Override
    public CssImportsFor<FdDataCss> cssImports() {
        return new CssImportsFor<>(this, List.of());
    }
}
