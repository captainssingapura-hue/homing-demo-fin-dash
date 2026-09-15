package hue.captains.singapura.js.homing.findash.trader;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.grid.RelationGridCrate;
import hue.captains.singapura.js.homing.findash.core.FinDashCoreCrate;
import hue.captains.singapura.js.homing.relgrid.group.RelGridGroupCrate;

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
        // Both grids for now: Episode 1 (RelationGrid) under the ladder that
        // ships, Episode 2 (the group) under the ladder being rebuilt beside it.
        return List.of(FinDashCoreCrate.INSTANCE, RelationGridCrate.INSTANCE,
                       RelGridGroupCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(TraderHomeWidget.INSTANCE),
                CrateEntry.of(PricerWidget.INSTANCE),
                CrateEntry.of(SurfaceManagerWidget.INSTANCE),
                CrateEntry.of(RiskBlotterWidget.INSTANCE),
                CrateEntry.of(RiskLadderWidget.INSTANCE),
                CrateEntry.of(RiskLadderCellModule.INSTANCE),
                CrateEntry.of(RiskLadderRowsModule.INSTANCE),
                CrateEntry.of(BarrierWatchWidget.INSTANCE),
                CrateEntry.of(ExpiryClustersWidget.INSTANCE));
    }
}
