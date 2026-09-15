package hue.captains.singapura.js.homing.findash.core.css;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;

import java.util.List;

/**
 * Interactive affordances — buttons, inputs, and the selection/hover states
 * that make a row feel clickable.
 *
 * <p>No new tokens: the accent blue the widgets had baked as {@code #2a78d6}
 * (as a button background, and as an {@code outline-color}) is exactly
 * {@code --color-accent}, which the framework already defines. It was a
 * literal only because nobody had named it at the point of use.</p>
 */
public record FdControlCss() implements CssGroup<FdControlCss> {

    public static final FdControlCss INSTANCE = new FdControlCss();

    /** Primary action. */
    public record fd_btn() implements CssClass<FdControlCss> {
        @Override public String body() { return """
                padding: var(--space-1, 4px) var(--space-3, 12px);
                font-size: 12px;
                font-weight: 600;
                border: none;
                border-radius: var(--radius-md);
                cursor: pointer;
                background: var(--color-accent);
                color: var(--color-accent-on);
                """;
        }
    }

    /**
     * Destructive action — the master kill. Deliberately larger and louder than
     * {@link fd_btn}: on a quoting desk this is the one control that must be
     * findable and hittable without hesitation (UI study P2).
     */
    public record fd_btn_danger() implements CssClass<FdControlCss> {
        @Override public String body() { return """
                padding: var(--space-2, 8px) var(--space-4, 16px);
                font-size: 13px;
                font-weight: 700;
                letter-spacing: 0.5px;
                border: none;
                border-radius: var(--radius-md);
                cursor: pointer;
                background: var(--color-status-critical, #a32e2e);
                color: var(--color-surface);
                """;
        }
    }

    /** Secondary action — same metrics, outlined instead of filled. */
    public record fd_btn_ghost() implements CssClass<FdControlCss> {
        @Override public String body() { return """
                padding: var(--space-1, 4px) var(--space-3, 12px);
                font-size: 12px;
                font-weight: 600;
                border: 1px solid var(--color-border);
                border-radius: var(--radius-md);
                cursor: pointer;
                background: transparent;
                color: var(--color-text-primary);
                """;
        }
    }

    /** Text input / select. */
    public record fd_input() implements CssClass<FdControlCss> {
        @Override public String body() { return """
                padding: var(--space-1, 4px) var(--space-2, 8px);
                border: 1px solid var(--color-border);
                border-radius: var(--radius-md);
                background: var(--color-surface);
                color: var(--color-text-primary);
                """;
        }
    }

    /** Marks a row or tile as actionable. */
    public record fd_clickable() implements CssClass<FdControlCss> {
        @Override public String body() { return "cursor: pointer;\n"; }
    }

    /**
     * Hover affordance for an actionable row. Rendered as
     * {@code .fd-clickable-hover:hover} via {@link #pseudoState()}, so the
     * state lives in the stylesheet rather than in mouse-event style writes.
     */
    public record fd_clickable_hover() implements CssClass<FdControlCss> {
        @Override public String pseudoState() { return ":hover"; }
        @Override public String body() { return "background: var(--color-surface-raised);\n"; }
    }

    /** The current selection — the widget the party bus is following. */
    public record fd_selected() implements CssClass<FdControlCss> {
        @Override public String body() { return """
                border-color: var(--color-accent);
                background: var(--color-surface-raised);
                """;
        }
    }

    /** Keyboard focus. Visible by default — the portfolio tree is keyboard-driven. */
    public record fd_focus_ring() implements CssClass<FdControlCss> {
        @Override public String pseudoState() { return ":focus-visible"; }
        @Override public String body() { return """
                outline: 2px solid var(--color-accent);
                outline-offset: 1px;
                """;
        }
    }

    public record fd_tab() implements CssClass<FdControlCss> {
        @Override public String body() { return """
                padding: var(--space-1, 4px) var(--space-3, 12px);
                font-size: 12px;
                cursor: pointer;
                border-bottom: 2px solid transparent;
                """;
        }
    }

    public record fd_tab_active() implements CssClass<FdControlCss> {
        @Override public String body() { return """
                font-weight: 600;
                border-bottom-color: var(--color-accent);
                """;
        }
    }

    @Override
    public List<CssClass<FdControlCss>> cssClasses() {
        return List.of(
                new fd_btn(),
                new fd_btn_danger(),
                new fd_btn_ghost(),
                new fd_input(),
                new fd_clickable(),
                new fd_clickable_hover(),
                new fd_selected(),
                new fd_focus_ring(),
                new fd_tab(),
                new fd_tab_active());
    }
}
