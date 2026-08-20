package hue.captains.singapura.js.homing.findash.core.kit;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.css.FdControlCss;
import hue.captains.singapura.js.homing.findash.core.css.FdDataCss;
import hue.captains.singapura.js.homing.findash.core.css.FdFrameCss;
import hue.captains.singapura.js.homing.findash.core.css.FdSurfaceCss;

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
        return ImportsFor.<FinDashGridModule>builder()
                .add(new ModuleImports<>(List.of(new FdFrameCss.fd_scroll()),
                        FdFrameCss.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new FdDataCss.fd_table(),
                        new FdDataCss.fd_th(),
                        new FdDataCss.fd_td(),
                        new FdDataCss.fd_td_num(),
                        new FdDataCss.fd_cell_compact(),
                        new FdDataCss.fd_row_group(),
                        new FdDataCss.fd_row_indent()),
                        FdDataCss.INSTANCE))
                .add(new ModuleImports<>(List.of(new FdSurfaceCss.fd_rule()),
                        FdSurfaceCss.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new FdControlCss.fd_clickable(),
                        new FdControlCss.fd_clickable_hover(),
                        new FdControlCss.fd_selected()),
                        FdControlCss.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<FinDashGridModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new fdGrid()));
    }
}
