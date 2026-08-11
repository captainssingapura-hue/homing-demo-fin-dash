package hue.captains.singapura.js.homing.findash.middleoffice;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.findash.core.FinDashCoreCrate;

import java.util.List;

/**
 * The {@link Crate} for {@code fin-dash-middleoffice}: every served JS module this
 * Maven module ships. {@code requires()} {@link FinDashCoreCrate} because the
 * widgets import the shared kit ({@code PersonaCardModule}) — a genuine
 * cross-crate edge; add framework crates only when a widget's served JS gains
 * a real import from them.
 */
public final class MiddleOfficeCrate implements Crate {

    public static final MiddleOfficeCrate INSTANCE = new MiddleOfficeCrate();

    private MiddleOfficeCrate() {}

    @Override public String name() { return "fin-dash-middleoffice"; }

    @Override
    public List<Crate> requires() {
        return List.of(FinDashCoreCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(MiddleOfficeHomeWidget.INSTANCE),
                CrateEntry.of(LifecycleWidget.INSTANCE));
    }
}
