package hue.captains.singapura.js.homing.findash.trader;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;

import java.util.List;

/**
 * The book, as the ladder's relations read it (RFC 0050 · Episode 2). One
 * poll of {@code /fx/book} through {@code fdk.load}, one re-judged snapshot,
 * many subscribers — the seam onto the desk. Knows nothing of grids, cells or
 * fences. Body in the co-located {@code LadderFeedModule.js}.
 */
public record LadderFeedModule() implements DomModule<LadderFeedModule> {

    public static final LadderFeedModule INSTANCE = new LadderFeedModule();

    /** Exported: {@code createLadderFeed({branch, host, everyMs?})}. */
    public record createLadderFeed() implements Exportable._Constant<LadderFeedModule> {}

    @Override
    public ImportsFor<LadderFeedModule> imports() {
        return ImportsFor.<LadderFeedModule>builder()
                .add(new ModuleImports<>(List.of(new FinDashKitModule.fdk()), FinDashKitModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<LadderFeedModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new createLadderFeed()));
    }
}
