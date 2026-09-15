package hue.captains.singapura.js.homing.findash.trader;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;

import java.util.List;

/**
 * The ladder's relations (RFC 0050 · Episode 2): one factory making the three
 * shapes a ladder stacks — a detail relation per pair, a one-row subtotal per
 * pair, a one-row book total — every one over the same seven columns, which is
 * what the group checks once at construction. Which relation a row is in is
 * its kind; there is no {@code kind} field and nothing is parsed from a pk.
 *
 * <p>{@code verdictOf} is the Episode 1 ladder's {@code ladderCellValue}
 * minus its {@code folded} argument: the caret it carried is the fence's now.
 * Body in the co-located {@code LadderRelationModule.js}.</p>
 */
public record LadderRelationModule() implements DomModule<LadderRelationModule> {

    public static final LadderRelationModule INSTANCE = new LadderRelationModule();

    /** Exported: the seven columns, frozen. */
    public record LADDER_COLUMNS() implements Exportable._Constant<LadderRelationModule> {}
    /** Exported: {@code createLadderRelation(feed, {pair?, kind, branch})}. */
    public record createLadderRelation() implements Exportable._Constant<LadderRelationModule> {}

    @Override
    public ImportsFor<LadderRelationModule> imports() {
        return ImportsFor.<LadderRelationModule>builder()
                .add(new ModuleImports<>(List.of(new FinDashKitModule.fdk()), FinDashKitModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new LadderCellModule.LadderCell()), LadderCellModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<LadderRelationModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new LADDER_COLUMNS(), new createLadderRelation()));
    }
}
