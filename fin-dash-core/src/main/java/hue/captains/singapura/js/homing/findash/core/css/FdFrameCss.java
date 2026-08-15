package hue.captains.singapura.js.homing.findash.core.css;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssImportsFor;

import java.util.List;

/**
 * Structure: the widget shell, scroll behaviour and the flex/grid frames.
 * Imported by all 39 widgets.
 *
 * <p>Every class here was derived from the inline styling it replaces — see
 * {@code docs/css-class-inventory.md} for the occurrence counts. Nothing was
 * invented; a shape that appeared once stayed inline rather than inflating the
 * vocabulary.</p>
 *
 * <p>Theme-token native: spacing routes through {@code --space-*} and colour
 * through {@code --color-*}, so there is no per-theme {@code CssGroupImpl} to
 * keep in step across the ten registered themes.</p>
 */
public record FdFrameCss() implements CssGroup<FdFrameCss> {

    public static final FdFrameCss INSTANCE = new FdFrameCss();

    /**
     * The widget shell. Merges the two strings that always co-occurred — the
     * typography base (27 sites) and the scroll frame (25 sites); they are one
     * concept that had been split by accident. Font size sits here so children
     * inherit the desk's base reading size.
     */
    public record fd_widget_root() implements CssClass<FdFrameCss> {
        @Override public String body() { return """
                height: 100%;
                overflow: auto;
                box-sizing: border-box;
                padding: var(--space-3, 12px);
                font-family: system-ui, sans-serif;
                font-size: 13px;
                color: var(--color-text-primary);
                """;
        }
    }

    /** The shell for widgets that own their own padding (grids, full-bleed tables). */
    public record fd_widget_root_flush() implements CssClass<FdFrameCss> {
        @Override public String body() { return """
                height: 100%;
                overflow: auto;
                box-sizing: border-box;
                padding: 0;
                font-family: system-ui, sans-serif;
                font-size: 13px;
                color: var(--color-text-primary);
                """;
        }
    }

    /** Prose pages (design doc, user guide) — roomier than a desk widget. */
    public record fd_doc_root() implements CssClass<FdFrameCss> {
        @Override public String body() { return """
                padding: var(--space-5, 20px);
                font-family: system-ui, sans-serif;
                font-size: 13px;
                color: var(--color-text-primary);
                """;
        }
    }

    /** Title + stamp + status strip across the top of a widget. */
    public record fd_header_row() implements CssClass<FdFrameCss> {
        @Override public String body() { return """
                display: flex;
                align-items: baseline;
                gap: var(--space-2, 8px);
                flex-wrap: wrap;
                """;
        }
    }

    /** A list/table-ish row of controls or facts. */
    public record fd_row() implements CssClass<FdFrameCss> {
        @Override public String body() { return """
                display: flex;
                gap: var(--space-2, 8px);
                align-items: center;
                padding: var(--space-1, 4px);
                """;
        }
    }

    /** Inline group — a label and its value, a meter and its number. */
    public record fd_cluster() implements CssClass<FdFrameCss> {
        @Override public String body() { return """
                display: inline-flex;
                gap: var(--space-1, 4px);
                align-items: center;
                """;
        }
    }

    /** Vertical rhythm for stacked blocks. */
    public record fd_stack() implements CssClass<FdFrameCss> {
        @Override public String body() { return """
                display: flex;
                flex-direction: column;
                gap: var(--space-2, 8px);
                """;
        }
    }

    /** Responsive tile field — summary cards, fact panels. */
    public record fd_grid_auto() implements CssClass<FdFrameCss> {
        @Override public String body() { return """
                display: grid;
                grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
                gap: var(--space-3, 12px);
                """;
        }
    }

    /** Pushes what follows to the far edge of a flex row. */
    public record fd_spacer() implements CssClass<FdFrameCss> {
        @Override public String body() { return "flex: 1;\n"; }
    }

    /** Separation between blocks within a widget. */
    public record fd_section() implements CssClass<FdFrameCss> {
        @Override public String body() { return "margin-top: var(--space-2, 8px);\n"; }
    }

    /** Toggled-off content. A class, so the toggle is typed rather than a style write. */
    public record fd_hidden() implements CssClass<FdFrameCss> {
        @Override public String body() { return "display: none;\n"; }
    }

    /** A scroll region that is not the widget shell — a grid body, a long list. */
    public record fd_scroll() implements CssClass<FdFrameCss> {
        @Override public String body() { return "overflow: auto;\n"; }
    }

    /**
     * Mount point for a rendering leaf — a WebGL canvas, a charting library's
     * subtree. {@code position: relative} is required by anything that absolutely
     * positions a tooltip or legend against its own container, which most
     * charting libraries do.
     *
     * <p>The element carrying this class is <b>branch-owned</b>; the library's
     * DOM lives inside it and is detached with it on dissolve. That containment
     * is what makes exempting the library from the DOM rules safe.</p>
     */
    public record fd_render_target() implements CssClass<FdFrameCss> {
        @Override public String body() { return """
                position: relative;
                width: 100%;
                min-height: 360px;
                flex: 1;
                """;
        }
    }

    /** Label/value pairs in columns — the greeks block, fact tables. */
    public record fd_grid_pairs() implements CssClass<FdFrameCss> {
        @Override public String body() { return """
                display: grid;
                grid-template-columns: auto auto auto auto;
                gap: 2px var(--space-4, 16px);
                max-width: 420px;
                """;
        }
    }

    /** A row whose two ends are pushed apart — ladder rungs, totals lines. */
    public record fd_split_row() implements CssClass<FdFrameCss> {
        @Override public String body() { return """
                display: flex;
                justify-content: space-between;
                """;
        }
    }

    @Override
    public List<CssClass<FdFrameCss>> cssClasses() {
        return List.of(
                new fd_widget_root(),
                new fd_widget_root_flush(),
                new fd_doc_root(),
                new fd_header_row(),
                new fd_row(),
                new fd_cluster(),
                new fd_stack(),
                new fd_grid_auto(),
                new fd_spacer(),
                new fd_section(),
                new fd_hidden(),
                new fd_scroll(),
                new fd_render_target(),
                new fd_grid_pairs(),
                new fd_split_row());
    }

    @Override
    public CssImportsFor<FdFrameCss> cssImports() {
        return new CssImportsFor<>(this, List.of());
    }
}
