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
 * The cells of the {@link RiskLadderWidget} — RFC 0050 hands cell ownership to
 * the application, so this is where a desk cell is actually written.
 *
 * <p>{@code RelationGrid} calls {@code cellFactory(column, value, meta)} and
 * then {@code cell.render(el, value)} with an element minted on the grid's own
 * cells branch. Everything visual is therefore ours: the type scale, the
 * subtotal's weight, and the P2 rule that a breach or a stale reading is a
 * <em>state</em> carrying a mark, never a colour on its own.</p>
 *
 * <p>The one thing a cell does not get is its row. The facade calls
 * {@code ensure(pk, col, factory, value)} with no {@code meta}, so a cell cannot
 * ask which tenor it belongs to or what that tenor's tolerance is. The adapter
 * decides instead and ships the verdict inside the value
 * ({@code {kind, text, n, state}}); the cell renders a judgement already made.
 * That split is the right one — the adapter is the seam onto the domain.</p>
 *
 * <p>Body in the co-located {@code RiskLadderCellModule.js}.</p>
 */
public record RiskLadderCellModule() implements DomModule<RiskLadderCellModule> {

    public static final RiskLadderCellModule INSTANCE = new RiskLadderCellModule();

    /** Exported: {@code ladderCell(column, value)} → a RelationGrid cell. */
    public record ladderCell() implements Exportable._Constant<RiskLadderCellModule> {}

    @Override
    public ImportsFor<RiskLadderCellModule> imports() {
        return ImportsFor.<RiskLadderCellModule>builder()
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
                        new FdDataCss.fd_row_indent()),
                        FdDataCss.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<RiskLadderCellModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new ladderCell()));
    }
}
