package hue.captains.singapura.js.homing.findash.trader;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;

import java.util.List;

/**
 * The rows of the {@link RiskLadderWidget}, and the value of every cell in
 * them — split out when the widget crossed the 250 effective-line gate.
 *
 * <p>The seam is not arbitrary. Everything here is the <b>shape of the
 * table</b>: which rows exist, in what order, and what each cell of each row
 * means. Nothing here touches the DOM, the grid, the party bus or the widget's
 * view state; what stays behind is the widget proper — loading, the adapter,
 * focus and folding, the chips, the party.</p>
 *
 * <p>Worth naming for anyone copying this: all of it exists because a grid
 * displays exactly one relation while the ladder needs several — a detail
 * block per pair, a subtotal closing each, the book closing the table. That is
 * the case made in {@code docs/homologous-relations.md}. If the facility
 * proposed there lands, most of this module stops existing rather than moving
 * somewhere else. Second in the demo to be split for the line gate, after
 * {@code TradeRowModule}.</p>
 *
 * <p>Body in the co-located {@code RiskLadderRowsModule.js}.</p>
 */
public record RiskLadderRowsModule() implements DomModule<RiskLadderRowsModule> {

    public static final RiskLadderRowsModule INSTANCE = new RiskLadderRowsModule();

    /** Exported: {@code ladderRows(bookJson)} → the ordered row records. */
    public record ladderRows() implements Exportable._Constant<RiskLadderRowsModule> {}

    /** Exported: {@code ladderCellValue(row, column, folded)} → a cell value. */
    public record ladderCellValue() implements Exportable._Constant<RiskLadderRowsModule> {}

    @Override
    public ImportsFor<RiskLadderRowsModule> imports() {
        // Only the kit, for compact number formatting. No CSS: this module
        // decides what a cell MEANS; RiskLadderCellModule decides how it looks.
        return ImportsFor.<RiskLadderRowsModule>builder()
                .add(new ModuleImports<>(List.of(new FinDashKitModule.fdk()),
                        FinDashKitModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<RiskLadderRowsModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new ladderRows(), new ladderCellValue()));
    }
}
