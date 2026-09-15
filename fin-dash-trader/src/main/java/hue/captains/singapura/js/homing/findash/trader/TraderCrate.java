package hue.captains.singapura.js.homing.findash.trader;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.findash.core.FinDashCoreCrate;
import hue.captains.singapura.js.homing.relgrid.group.RelGridGroupCrate;
import hue.captains.singapura.js.homing.relgrid.protocol.RelGridProtocolCrate;

import java.util.List;

/**
 * The {@link Crate} for {@code fin-dash-trader}: every served JS module this
 * Maven module ships. {@code requires()} {@link FinDashCoreCrate} because the
 * widgets import the shared kit ({@code PersonaCardModule}) — a genuine
 * cross-crate edge; add framework crates only when a widget's served JS gains
 * a real import from them.
 */
public final class TraderCrate implements Crate {

    public static final TraderCrate INSTANCE = new TraderCrate();

    private TraderCrate() {}

    @Override public String name() { return "fin-dash-trader"; }

    @Override
    public List<Crate> requires() {
        // The Relation Grid reactor (RFC 0050 · Episode 2), for the ladder: the
        // group crate, and the protocol crate directly because the fence module
        // news up its values — a direct import needs a direct require.
        return List.of(FinDashCoreCrate.INSTANCE,
                       RelGridGroupCrate.INSTANCE, RelGridProtocolCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(TraderHomeWidget.INSTANCE),
                CrateEntry.of(PricerWidget.INSTANCE),
                CrateEntry.of(SurfaceManagerWidget.INSTANCE),
                CrateEntry.of(RiskBlotterWidget.INSTANCE),
                // The ladder and its four domain modules: feed, relations, cell, fences.
                CrateEntry.of(RiskLadderWidget.INSTANCE),
                CrateEntry.of(LadderFeedModule.INSTANCE),
                CrateEntry.of(LadderRelationModule.INSTANCE),
                CrateEntry.of(LadderCellModule.INSTANCE),
                CrateEntry.of(LadderFenceModule.INSTANCE),
                CrateEntry.of(BarrierWatchWidget.INSTANCE),
                CrateEntry.of(ExpiryClustersWidget.INSTANCE));
    }
}
