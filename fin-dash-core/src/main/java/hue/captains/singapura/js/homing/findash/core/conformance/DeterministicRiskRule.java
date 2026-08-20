package hue.captains.singapura.js.homing.findash.core.conformance;

import hue.captains.singapura.js.homing.conformance.rules.DoctrineRef;
import hue.captains.singapura.js.homing.conformance.rules.Finding;
import hue.captains.singapura.js.homing.conformance.rules.JsRule;
import hue.captains.singapura.js.homing.conformance.rules.JsText;
import hue.captains.singapura.js.homing.conformance.rules.RuleId;
import hue.captains.singapura.js.homing.conformance.rules.ServedModule;

import java.util.ArrayList;
import java.util.List;

/**
 * RFC 0044 extension — the fin-dash's own conformance rule: a {@link
 * RiskModuleType#RISK_MODEL} module must be <b>reproducible</b>. A risk number
 * that depends on {@code Math.random()} or wall-clock {@code Date.now()} cannot
 * be replayed, reconciled, or IPV'd — the whole platform rests on "two consumers
 * at the same as-of see the same number" (UI study P5), which non-determinism
 * breaks. Time and randomness must enter a risk model as explicit inputs
 * (the {@code MarketSlice} / scenario), never ambiently.
 *
 * <p>A downstream rule is just a {@link JsRule}, the same contract the framework
 * rules implement. Pair it in the RISK_MODEL rule set with the no-DOM rule and
 * the global rules — see {@link FinDashConformance}.</p>
 */
public record DeterministicRiskRule() implements JsRule {

    public static final DeterministicRiskRule INSTANCE = new DeterministicRiskRule();

    @Override public RuleId      id()     { return new RuleId("deterministic-risk"); }
    @Override public String      intent() { return "A risk model must be reproducible — no Math.random() or wall-clock Date.now(); time and randomness are explicit inputs."; }
    @Override public DoctrineRef basis()  { return new DoctrineRef("reproducible-risk"); }

    @Override
    public List<Finding> check(ServedModule module) {
        var findings = new ArrayList<Finding>();
        // Strip comments first (positions preserved) so a comment mentioning these
        // tokens doesn't false-positive — only real calls count.
        List<String> lines = JsText.stripComments(module.lines());
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.contains("Math.random(") || line.contains("Date.now(")) {
                findings.add(new Finding(module.moduleClass(), id(),
                        "non-deterministic call in a risk model (pass time/randomness as inputs): " + line.trim(), i));
            }
        }
        return List.copyOf(findings);
    }
}
