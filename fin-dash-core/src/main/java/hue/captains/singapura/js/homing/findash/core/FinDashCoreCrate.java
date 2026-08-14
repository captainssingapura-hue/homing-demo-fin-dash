package hue.captains.singapura.js.homing.findash.core;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import hue.captains.singapura.js.homing.findash.core.bus.DeskSecretaryModule;
import hue.captains.singapura.js.homing.findash.core.conformance.RiskModuleType;
import hue.captains.singapura.js.homing.findash.core.css.FdChartCss;
import hue.captains.singapura.js.homing.findash.core.css.FdControlCss;
import hue.captains.singapura.js.homing.findash.core.css.FdDataCss;
import hue.captains.singapura.js.homing.findash.core.css.FdFrameCss;
import hue.captains.singapura.js.homing.findash.core.css.FdStatusCss;
import hue.captains.singapura.js.homing.findash.core.css.FdSurfaceCss;
import hue.captains.singapura.js.homing.findash.core.css.FdTextCss;
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
                // Declared CONSUMER by omission (the default, full-discipline
                // baseline). These four were previously declared PRIMITIVE, which
                // is for STRUCTURAL primitives — SplitPane, MultiTabPane, the
                // things that own and mutate layout DOM. A chip factory, a data
                // grid, a persona card and an SVG chart are content builders:
                // consumer view code. The declaration was an opt-out of the CSS
                // discipline that would otherwise have applied by default, which
                // is why the kit's styling stayed invisible to the gate while
                // every widget importing it inherited that styling.
                CrateEntry.of(PersonaCardModule.INSTANCE),
                CrateEntry.of(FinDashKitModule.INSTANCE),
                CrateEntry.of(FinDashGridModule.INSTANCE),
                CrateEntry.of(SmileChartModule.INSTANCE),
                // The typed CSS vocabulary — seven groups by concern, so a widget
                // importing charts does not drag in form controls. Theme-token
                // native: no per-theme CssGroupImpl to keep in step across the
                // ten registered themes. See docs/css-class-inventory.md.
                CrateEntry.of(FdFrameCss.INSTANCE),
                CrateEntry.of(FdTextCss.INSTANCE),
                CrateEntry.of(FdSurfaceCss.INSTANCE),
                CrateEntry.of(FdStatusCss.INSTANCE),
                CrateEntry.of(FdControlCss.INSTANCE),
                CrateEntry.of(FdDataCss.INSTANCE),
                CrateEntry.of(FdChartCss.INSTANCE),
                // The desk selection bus — headless, held to the SECRETARY (no-DOM) set:
                CrateEntry.of(DeskSecretaryModule.INSTANCE, StandardJsModuleType.SECRETARY),
                // The downstream extension type, made visible in the studio:
                CrateEntry.of(VarModel.INSTANCE, RiskModuleType.RISK_MODEL));
    }
}
