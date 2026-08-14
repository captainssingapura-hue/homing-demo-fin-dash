package hue.captains.singapura.js.homing.findash.core.css;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssImportsFor;

import java.util.List;

/**
 * Surfaces and separators — the panels a widget lays its content on and the
 * rules that divide them.
 *
 * <p>Every colour here is already a framework token, so this group needed no
 * new ones: the widgets were consistent about surfaces even while they were
 * inconsistent about nearly everything else.</p>
 */
public record FdSurfaceCss() implements CssGroup<FdSurfaceCss> {

    public static final FdSurfaceCss INSTANCE = new FdSurfaceCss();

    /** A raised block — totals strips, fact panels. */
    public record fd_panel() implements CssClass<FdSurfaceCss> {
        @Override public String body() { return """
                background: var(--color-surface-raised);
                border-radius: var(--radius-md);
                padding: var(--space-2, 8px);
                """;
        }
    }

    /** An outlined block. */
    public record fd_card() implements CssClass<FdSurfaceCss> {
        @Override public String body() { return """
                border: 1px solid var(--color-border);
                border-radius: var(--radius-md);
                """;
        }
    }

    /** Recessed area — input wells, code blocks. */
    public record fd_inset() implements CssClass<FdSurfaceCss> {
        @Override public String body() { return "background: var(--color-surface);\n"; }
    }

    /** Row separator. */
    public record fd_rule() implements CssClass<FdSurfaceCss> {
        @Override public String body() { return "border-bottom: 1px solid var(--color-border);\n"; }
    }

    /** Separator above a footer or totals row. */
    public record fd_rule_top() implements CssClass<FdSurfaceCss> {
        @Override public String body() { return "border-top: 1px solid var(--color-border);\n"; }
    }

    /** Header underline — heavier, to close a table head. */
    public record fd_rule_strong() implements CssClass<FdSurfaceCss> {
        @Override public String body() { return "border-bottom: 2px solid var(--color-border);\n"; }
    }

    @Override
    public List<CssClass<FdSurfaceCss>> cssClasses() {
        return List.of(
                new fd_panel(),
                new fd_card(),
                new fd_inset(),
                new fd_rule(),
                new fd_rule_top(),
                new fd_rule_strong());
    }

    @Override
    public CssImportsFor<FdSurfaceCss> cssImports() {
        return new CssImportsFor<>(this, List.of());
    }
}
