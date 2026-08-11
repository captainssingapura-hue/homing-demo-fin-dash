package hue.captains.singapura.js.homing.findash.conformance;

import hue.captains.singapura.js.homing.core.JsModuleType;

/**
 * RFC 0044 extension — the fin-dash's own JS module type: a headless
 * <b>risk model</b> (VaR, exposure, stress, pricing). The framework's
 * {@code StandardJsModuleType} set has no such role; because {@link JsModuleType}
 * is an open interface, we just implement it. This is the unsealed extension
 * branch — framework code keeps its exhaustive switch over the standard types;
 * this one dispatches through {@code CompositeJsRulePolicy}. Registered with a
 * rule set in {@link FinDashConformance}.
 */
public enum RiskModuleType implements JsModuleType {

    /** A headless risk calculator — deterministic, no DOM. */
    RISK_MODEL;

    @Override public String slug()  { return "risk-model"; }
    @Override public String label() { return "Risk model"; }
}
