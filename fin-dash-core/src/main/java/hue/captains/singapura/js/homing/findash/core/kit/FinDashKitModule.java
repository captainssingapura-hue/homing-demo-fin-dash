package hue.captains.singapura.js.homing.findash.core.kit;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * The fin-dash UI kit — theme tokens, element/format helpers, and the two
 * cross-cutting patterns every widget carries (UI study P1 + P2): the status
 * <b>chip</b> (state visible, icon + label, never color alone) and the lineage
 * <b>stamp</b> (every number explains itself), plus the severity {@code meter}.
 * Palette: the validated dataviz reference instance; status colors are
 * reserved and never used as series colors.
 *
 * <p>Pure DOM builders behind one {@code fdk} object — no branch access, no
 * lookups, no HTML literals. Body in the co-located {@code FinDashKitModule.js}.</p>
 */
public record FinDashKitModule() implements DomModule<FinDashKitModule> {

    public static final FinDashKitModule INSTANCE = new FinDashKitModule();

    /** The exported {@code fdk} object: tokens, el, fmt, chip, stamp, meter, kv. */
    public record fdk() implements Exportable._Constant<FinDashKitModule> {}

    @Override
    public ImportsFor<FinDashKitModule> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<FinDashKitModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new fdk()));
    }
}
