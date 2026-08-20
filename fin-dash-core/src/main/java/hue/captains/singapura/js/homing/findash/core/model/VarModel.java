package hue.captains.singapura.js.homing.findash.core.model;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * A headless risk model (RFC 0044 extension demo) — declared {@link
 * hue.captains.singapura.js.homing.findash.core.conformance.RiskModuleType#RISK_MODEL}
 * in {@code FinDashCoreCrate}. It emits pure, deterministic JS (no DOM, no clock, no
 * randomness) — the served body lives in the co-located resource {@code
 * VarModel.js}. This is the "risk math is headless; views are consumers" split
 * from the UI study (P5), made structural: the widget calls {@code varOf(...)},
 * the model computes.
 *
 * <p>Placeholder maths — a downstream replaces the body with the real engine's
 * calculations (or, more likely, a thin client over the engine's endpoints).</p>
 */
public record VarModel() implements EsModule<VarModel> {

    public static final VarModel INSTANCE = new VarModel();

    /** Exported function: {@code varOf(portfolio, scenario)} → VaR figure. */
    public record varOf() implements Exportable._Constant<VarModel> {}

    @Override
    public ImportsFor<VarModel> imports() {
        return ImportsFor.<VarModel>builder().build();
    }

    @Override
    public ExportsOf<VarModel> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new varOf()));
    }
}
