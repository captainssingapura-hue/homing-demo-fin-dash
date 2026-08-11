package hue.captains.singapura.js.homing.findash.core;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.findash.core.bus.DeskSecretaryModule;
import hue.captains.singapura.js.homing.findash.core.conformance.RiskModuleType;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashGridModule;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;
import hue.captains.singapura.js.homing.findash.core.kit.PersonaCardModule;
import hue.captains.singapura.js.homing.findash.core.kit.SmileChartModule;
import hue.captains.singapura.js.homing.findash.core.model.VarModel;

import java.util.List;

/**
 * The {@link Crate} for {@code fin-dash-core}: every served JS module this
 * Maven module ships — the shared UI primitives the persona workspaces import,
 * and the headless risk models. One crate per Maven module ({@code OrphanCheck}
 * scans this module's whole build output); every persona crate {@code
 * requires()} this one for its kit imports.
 *
 * <p>{@link VarModel} is declared as the downstream {@link
 * RiskModuleType#RISK_MODEL} extension type (the declaration wins over
 * structural inference), so the conformance studio shows it held to the
 * {@code risk-model} rule set — the RFC 0044 policy extension, made visible.</p>
 */
public final class FinDashCoreCrate implements Crate {

    public static final FinDashCoreCrate INSTANCE = new FinDashCoreCrate();

    private FinDashCoreCrate() {}

    @Override public String name() { return "fin-dash-core"; }

    @Override
    public List<Crate> requires() {
        // Core's own modules import nothing cross-crate today: the kit is a
        // pure DOM builder, the models import nothing. Grows only when a core
        // module's served JS gains a genuine cross-crate import.
        return List.of();
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(PersonaCardModule.INSTANCE, StandardJsModuleType.PRIMITIVE),
                CrateEntry.of(FinDashKitModule.INSTANCE, StandardJsModuleType.PRIMITIVE),
                CrateEntry.of(FinDashGridModule.INSTANCE, StandardJsModuleType.PRIMITIVE),
                CrateEntry.of(SmileChartModule.INSTANCE, StandardJsModuleType.PRIMITIVE),
                // The desk selection bus — headless, held to the SECRETARY (no-DOM) set:
                CrateEntry.of(DeskSecretaryModule.INSTANCE, StandardJsModuleType.SECRETARY),
                // The downstream extension type, made visible in the studio:
                CrateEntry.of(VarModel.INSTANCE, RiskModuleType.RISK_MODEL));
    }
}
