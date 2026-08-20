package hue.captains.singapura.js.homing.findash.ontology;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.js.CoreJsCrate;
import hue.captains.singapura.js.homing.findash.core.FinDashCoreCrate;

import java.util.List;

/**
 * The {@link Crate} for {@code fin-dash-ontology}: the two widgets of the Data
 * Ontology workspace. Requires the fin-dash kit and {@code CoreJsCrate} (the
 * type tree imports the framework's {@code TreeRendererModule}).
 */
public final class OntologyCrate implements Crate {

    public static final OntologyCrate INSTANCE = new OntologyCrate();

    private OntologyCrate() {}

    @Override public String name() { return "fin-dash-ontology"; }

    @Override
    public List<Crate> requires() {
        return List.of(FinDashCoreCrate.INSTANCE, CoreJsCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(DataTypeTreeWidget.INSTANCE),
                CrateEntry.of(DataTypeUsageWidget.INSTANCE));
    }
}
