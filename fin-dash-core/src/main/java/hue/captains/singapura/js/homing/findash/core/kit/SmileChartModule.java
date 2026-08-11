package hue.captains.singapura.js.homing.findash.core.kit;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * The W2 smile chart: fitted curve vs raw market quotes per pillar in delta
 * space, to the dataviz mark specs (2px round fit line, ≥8px market dots with
 * a surface ring, hairline gridlines, legend for the two series; overridden
 * pillars in the warning status color with icon + label). Pure SVG builder
 * ({@code createElementNS}); body in the co-located {@code SmileChartModule.js}.
 */
public record SmileChartModule() implements DomModule<SmileChartModule> {

    public static final SmileChartModule INSTANCE = new SmileChartModule();

    /** Exported function: {@code smileChart(opts)} → {@code <svg>} element. */
    public record smileChart() implements Exportable._Constant<SmileChartModule> {}

    @Override
    public ImportsFor<SmileChartModule> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<SmileChartModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new smileChart()));
    }
}
