package hue.captains.singapura.js.homing.findash.governance;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.findash.core.FinDashCoreCrate;

import java.util.List;

/**
 * The {@link Crate} for {@code fin-dash-governance}: every served JS module this
 * Maven module ships. {@code requires()} {@link FinDashCoreCrate} because the
 * widgets import the shared kit ({@code PersonaCardModule}) — a genuine
 * cross-crate edge; add framework crates only when a widget's served JS gains
 * a real import from them.
 */
public final class GovernanceCrate implements Crate {

    public static final GovernanceCrate INSTANCE = new GovernanceCrate();

    private GovernanceCrate() {}

    @Override public String name() { return "fin-dash-governance"; }

    @Override
    public List<Crate> requires() {
        return List.of(FinDashCoreCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(GovernanceHomeWidget.INSTANCE),
                CrateEntry.of(ChangeConsoleWidget.INSTANCE),
                CrateEntry.of(ModelInventoryWidget.INSTANCE));
    }
}
