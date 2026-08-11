package hue.captains.singapura.js.homing.findash.core.kit;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * The dense-blotter grid the trader / MO screens are built on (UI study NFR:
 * 30+ rows without scrolling): column-spec driven, tabular numerals on numeric
 * columns, group rows, selection highlight, and a per-row click callback for
 * party-bus publication. Pure DOM builder; body in the co-located
 * {@code FinDashGridModule.js}.
 */
public record FinDashGridModule() implements DomModule<FinDashGridModule> {

    public static final FinDashGridModule INSTANCE = new FinDashGridModule();

    /** Exported function: {@code fdGrid(opts)} → {@code { root, setRows }}. */
    public record fdGrid() implements Exportable._Constant<FinDashGridModule> {}

    @Override
    public ImportsFor<FinDashGridModule> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<FinDashGridModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new fdGrid()));
    }
}
