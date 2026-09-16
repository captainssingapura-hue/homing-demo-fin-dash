package hue.captains.singapura.js.homing.findash.trader;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.css.FdDataCss;
import hue.captains.singapura.js.homing.findash.core.css.FdStatusCss;
import hue.captains.singapura.js.homing.findash.core.css.FdTextCss;

import java.util.List;

/**
 * A ladder cell on the Relation Grid reactor (RFC 0050 · Episode 2): a
 * verdict that draws itself. Read-only half of {@code RelGridCellContract}
 * only — {@code cellElement()}, {@code onSelect()}, {@code dispose()} — plus
 * {@code set(verdict)}, which is ours: the relation repaints its cells from
 * the feed and the grid is never told.
 *
 * <p>What the Episode 1 cell had and this does not: a void cell class for the
 * section row, a band drawn by every cell in a row agreeing, and a copy guard
 * blanking aggregates — each of which existed because one table was holding
 * several relations ({@code docs/relation-grid-risk-ladder.md} §5). Body in
 * the co-located {@code LadderCellModule.js}.</p>
 */
public record LadderCellModule() implements DomModule<LadderCellModule> {

    public static final LadderCellModule INSTANCE = new LadderCellModule();

    /** Exported: {@code new LadderCell({branch, column, value})}. */
    public record LadderCell() implements Exportable._Constant<LadderCellModule> {}

    @Override
    public ImportsFor<LadderCellModule> imports() {
        return ImportsFor.<LadderCellModule>builder()
                .add(new ModuleImports<>(List.of(
                        new FdTextCss.fd_dense(),
                        new FdTextCss.fd_muted(),
                        new FdTextCss.fd_num(),
                        new FdTextCss.fd_strongest()),
                        FdTextCss.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new FdStatusCss.fd_status_serious(),
                        new FdStatusCss.fd_status_warn()),
                        FdStatusCss.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new FdDataCss.fd_cell_compact(),
                        new FdDataCss.fd_row_indent(),
                        new FdDataCss.fd_total_row(),
                        new FdDataCss.fd_grand_row()),
                        FdDataCss.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<LadderCellModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new LadderCell()));
    }
}
