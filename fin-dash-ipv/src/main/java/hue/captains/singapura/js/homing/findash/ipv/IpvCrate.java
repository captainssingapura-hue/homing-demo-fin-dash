package hue.captains.singapura.js.homing.findash.ipv;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.findash.core.FinDashCoreCrate;

import java.util.List;

/**
 * The {@link Crate} for {@code fin-dash-ipv}: every served JS module this
 * Maven module ships. {@code requires()} {@link FinDashCoreCrate} because the
 * widgets import the shared kit ({@code PersonaCardModule}) — a genuine
 * cross-crate edge; add framework crates only when a widget's served JS gains
 * a real import from them.
 */
public final class IpvCrate implements Crate {

    public static final IpvCrate INSTANCE = new IpvCrate();

    private IpvCrate() {}

    @Override public String name() { return "fin-dash-ipv"; }

    @Override
    public List<Crate> requires() {
        return List.of(FinDashCoreCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(IpvHomeWidget.INSTANCE),
                CrateEntry.of(PnlExplainWidget.INSTANCE));
    }
}
