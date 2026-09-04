package hue.captains.singapura.js.homing.findash.studio.conformance;

import hue.captains.singapura.js.homing.conformance.rules.Allowance;
import hue.captains.singapura.js.homing.conformance.rules.Baseline;
import hue.captains.singapura.js.homing.conformance.rules.DefaultJsRulePolicy;
import hue.captains.singapura.js.homing.conformance.rules.FindingGrader;
import hue.captains.singapura.js.homing.conformance.rules.JsRule;
import hue.captains.singapura.js.homing.conformance.rules.JsRulePolicy;
import hue.captains.singapura.js.homing.conformance.rules.JsRuleSet;
import hue.captains.singapura.js.homing.conformance.rules.MaxEffectiveLinesRule;
import hue.captains.singapura.js.homing.conformance.rules.NoCdnImportRule;
import hue.captains.singapura.js.homing.conformance.rules.NoDomAccessRule;
import hue.captains.singapura.js.homing.conformance.rules.RuleSetId;
import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.JsModuleType;
import hue.captains.singapura.js.homing.findash.audit.AuditCrate;
import hue.captains.singapura.js.homing.findash.book.BookCrate;
import hue.captains.singapura.js.homing.findash.core.FinDashCoreCrate;
import hue.captains.singapura.js.homing.findash.core.conformance.DeterministicRiskRule;
import hue.captains.singapura.js.homing.findash.core.conformance.NoEmptyCatchRule;
import hue.captains.singapura.js.homing.findash.core.conformance.RiskModuleType;
import hue.captains.singapura.js.homing.findash.etrading.ETradingCrate;
import hue.captains.singapura.js.homing.findash.governance.GovernanceCrate;
import hue.captains.singapura.js.homing.findash.ipv.IpvCrate;
import hue.captains.singapura.js.homing.findash.marketdata.MarketDataCrate;
import hue.captains.singapura.js.homing.findash.ontology.OntologyCrate;
import hue.captains.singapura.js.homing.findash.middleoffice.MiddleOfficeCrate;
import hue.captains.singapura.js.homing.findash.platform.PlatformCrate;
import hue.captains.singapura.js.homing.findash.quant.QuantCrate;
import hue.captains.singapura.js.homing.findash.viz.FinDashVizCrate;
import hue.captains.singapura.js.homing.findash.risk.RiskCrate;
import hue.captains.singapura.js.homing.findash.sales.SalesCrate;
import hue.captains.singapura.js.homing.findash.summary.SummaryCrate;
import hue.captains.singapura.js.homing.findash.trader.TraderCrate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The fin-dash's conformance configuration, in one place so the build-fail gate
 * ({@code FinDashConformanceTest}) and the studio's report export ({@link
 * FinDashConformanceExport}) grade identically. Mirrors the framework's
 * {@code HomingConformance}, with the downstream twist: {@link #POLICY} is the
 * <b>extended</b> policy — the framework rules plus a {@code risk-model} rule set
 * for the {@link RiskModuleType#RISK_MODEL} extension type.
 */
public final class FinDashConformance {

    private FinDashConformance() {}

    /**
     * The fin-dash's own crates — one per Maven module (core + one per persona
     * workspace) — what the gate and the studio browse + grade.
     */
    public static final List<Crate> TOP_LEVEL = List.of(
            FinDashCoreCrate.INSTANCE,
            BookCrate.INSTANCE,
            FinDashVizCrate.INSTANCE,
            TraderCrate.INSTANCE,
            ETradingCrate.INSTANCE,
            SalesCrate.INSTANCE,
            MarketDataCrate.INSTANCE,
            QuantCrate.INSTANCE,
            RiskCrate.INSTANCE,
            GovernanceCrate.INSTANCE,
            MiddleOfficeCrate.INSTANCE,
            IpvCrate.INSTANCE,
            PlatformCrate.INSTANCE,
            AuditCrate.INSTANCE,
            SummaryCrate.INSTANCE,
            OntologyCrate.INSTANCE);

    /**
     * The rule set for a {@code RISK_MODEL} module: the framework globals
     * (no CDN imports, effective-line cap) + strict no-DOM (a risk model is
     * headless) + the fin-dash's own determinism rule.
     */
    private static final JsRuleSet RISK_MODEL_RULES = new JsRuleSet(
            new RuleSetId("risk-model"), "Risk model",
            List.of(NoCdnImportRule.INSTANCE,
                    MaxEffectiveLinesRule.INSTANCE,
                    NoDomAccessRule.INSTANCE,
                    DeterministicRiskRule.INSTANCE));

    /** The framework policy, extended with the fin-dash's {@code risk-model} type → rule set. */
    private static final JsRulePolicy TYPED_POLICY = DefaultJsRulePolicy.INSTANCE.extendedWith(
            Map.<JsModuleType, JsRuleSet>of(RiskModuleType.RISK_MODEL, RISK_MODEL_RULES));

    /**
     * The desk-wide layer: rules that hold for <em>every</em> served fin-dash
     * module regardless of type. {@link JsRulePolicy} is one method, so a
     * downstream global rule is a policy that delegates per type and appends.
     * A type whose framework rule set is empty (a bundled external) stays
     * empty — exemption is the framework's decision and is not overridden.
     */
    private static final List<JsRule> DESK_WIDE = List.of(NoEmptyCatchRule.INSTANCE);

    public static final JsRulePolicy POLICY = type -> {
        JsRuleSet base = TYPED_POLICY.rulesFor(type);
        if (base.rules().isEmpty()) return base;
        var rules = new ArrayList<JsRule>(base.rules());
        for (JsRule r : DESK_WIDE) if (!rules.contains(r)) rules.add(r);
        return new JsRuleSet(base.id(), base.title(), rules);
    };

    /** Documented, intentional exceptions (none today). */
    public static final List<Allowance> ALLOWANCES = List.of();

    /** The committed baseline of grandfathered pre-existing violations. */
    public static Baseline baseline() {
        try (InputStream in = FinDashConformance.class.getResourceAsStream("/risk-conformance-baseline.txt")) {
            if (in == null) return Baseline.EMPTY;
            var lines = new ArrayList<String>();
            try (var r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                for (String line; (line = r.readLine()) != null; ) lines.add(line);
            }
            return Baseline.of(lines);
        } catch (IOException e) {
            throw new UncheckedIOException("failed to load fin-dash conformance baseline", e);
        }
    }

    /** The framework-strict grader plus the fin-dash's allowances + baseline. */
    public static FindingGrader grader(boolean allowPreExisting) {
        return FindingGrader.STRICT
                .withAllowlist(ALLOWANCES)
                .withBaseline(baseline())
                .allowingPreExisting(allowPreExisting);
    }
}
