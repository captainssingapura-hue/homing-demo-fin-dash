package hue.captains.singapura.js.homing.findash.core.css;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssImportsFor;

import java.util.List;

/**
 * SVG chart internals — the volatility smile and anything that follows it.
 *
 * <p>SVG is styled through presentation attributes ({@code fill=},
 * {@code stroke=}) by default, which bypasses the theme exactly as an inline
 * style does and is invisible to {@code no-inline-style} because it is neither
 * a {@code .style} write nor {@code setAttribute('style')}. The
 * {@code no-presentation-attribute} rule closes that, and these classes are
 * what the chart uses instead — {@code fill} and {@code stroke} are ordinary
 * CSS properties on SVG elements, so a class works as well as an attribute and
 * carries tokens.</p>
 *
 * <p>Geometry ({@code x}, {@code y}, {@code d}, {@code viewBox}) stays on
 * attributes — that is structure, not styling, and has no class equivalent.</p>
 */
public record FdChartCss() implements CssGroup<FdChartCss> {

    public static final FdChartCss INSTANCE = new FdChartCss();

    public record fd_chart_axis() implements CssClass<FdChartCss> {
        @Override public String body() { return """
                stroke: var(--color-border);
                stroke-width: 1;
                fill: none;
                """;
        }
    }

    public record fd_chart_gridline() implements CssClass<FdChartCss> {
        @Override public String body() { return """
                stroke: var(--color-border);
                stroke-width: 1;
                opacity: 0.4;
                fill: none;
                """;
        }
    }

    /** The single series hue — the kit's categorical slot 1. */
    public record fd_chart_series() implements CssClass<FdChartCss> {
        @Override public String body() { return """
                stroke: var(--color-accent);
                stroke-width: 2;
                fill: none;
                """;
        }
    }

    public record fd_chart_series_point() implements CssClass<FdChartCss> {
        @Override public String body() { return """
                fill: var(--color-accent);
                stroke: none;
                """;
        }
    }

    /** Callouts — a pin risk marker, a barrier level. */
    public record fd_chart_annotation() implements CssClass<FdChartCss> {
        @Override public String body() { return """
                stroke: var(--color-status-warn, #9a6b1f);
                stroke-width: 1;
                stroke-dasharray: 3 2;
                fill: none;
                """;
        }
    }

    public record fd_chart_label() implements CssClass<FdChartCss> {
        @Override public String body() { return """
                fill: var(--color-text-muted);
                font-size: 11px;
                """;
        }
    }

    @Override
    public List<CssClass<FdChartCss>> cssClasses() {
        return List.of(
                new fd_chart_axis(),
                new fd_chart_gridline(),
                new fd_chart_series(),
                new fd_chart_series_point(),
                new fd_chart_annotation(),
                new fd_chart_label());
    }

    @Override
    public CssImportsFor<FdChartCss> cssImports() {
        return new CssImportsFor<>(this, List.of());
    }
}
