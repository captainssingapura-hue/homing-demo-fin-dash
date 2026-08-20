package hue.captains.singapura.js.homing.findash.audit;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.findash.core.FinDashCoreCrate;

import java.util.List;

/**
 * The {@link Crate} for {@code fin-dash-audit}: every served JS module this
 * Maven module ships. {@code requires()} {@link FinDashCoreCrate} because the
 * widgets import the shared kit ({@code PersonaCardModule}) — a genuine
 * cross-crate edge; add framework crates only when a widget's served JS gains
 * a real import from them.
 */
public final class AuditCrate implements Crate {

    public static final AuditCrate INSTANCE = new AuditCrate();

    private AuditCrate() {}

    @Override public String name() { return "fin-dash-audit"; }

    @Override
    public List<Crate> requires() {
        return List.of(FinDashCoreCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(AuditHomeWidget.INSTANCE),
                CrateEntry.of(AuditExplorerWidget.INSTANCE));
    }
}
