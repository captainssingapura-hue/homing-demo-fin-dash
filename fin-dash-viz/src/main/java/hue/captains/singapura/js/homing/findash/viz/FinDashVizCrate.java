package hue.captains.singapura.js.homing.findash.viz;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.findash.core.FinDashCoreCrate;

import java.util.List;

/**
 * The {@link Crate} for {@code fin-dash-viz}: the heavyweight visualisation
 * module. Kept separate from {@code fin-dash-core} so the ~600KB Three.js
 * bundle is not on the path of every workspace that only wants a chip and a
 * grid.
 *
 * <p>The two library entries classify as {@code BUNDLED_EXTERNAL} structurally —
 * {@code ModuleClassifier} infers it from {@code BundledExternalModule}, so
 * neither needs a declared type. That is the intended route for third-party
 * code: exempt by category, not by exception. The widget itself is declared
 * nothing, which means {@code CONSUMER} — the full discipline — as it should
 * be.</p>
 */
public final class FinDashVizCrate implements Crate {

    public static final FinDashVizCrate INSTANCE = new FinDashVizCrate();

    private FinDashVizCrate() {}

    @Override public String name() { return "fin-dash-viz"; }

    @Override
    public List<Crate> requires() {
        return List.of(FinDashCoreCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(ThreeJs.INSTANCE),
                CrateEntry.of(VolSurfaceLib.INSTANCE),
                CrateEntry.of(VolSurfaceWidget.INSTANCE));
    }
}
