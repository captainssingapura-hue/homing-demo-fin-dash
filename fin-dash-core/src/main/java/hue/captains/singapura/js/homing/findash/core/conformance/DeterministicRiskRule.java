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
 * RFC 0044 extension — the fin-dash's own conformance rule: served code must
 * be <b>reproducible</b>. Nothing ambient — no {@code Math.random()}, no
 * wall-clock {@code Date.now()}; time and randomness enter as explicit inputs.
 *
 * <p>It began as a rule for {@link RiskModuleType#RISK_MODEL} modules alone: a
 * risk number that depends on ambient randomness cannot be replayed,
 * reconciled, or IPV'd — the whole platform rests on "two consumers at the
 * same as-of see the same number" (UI study P5). Then the desk was found
 * minting every party actor identity with {@code Math.random()}, in twenty
 * widgets, so that no actor could be addressed or persisted and no bus
 * interaction replayed — the same failure, one layer up. The rule now holds
 * desk-wide: it sits in the RISK_MODEL set and in the desk-wide layer of
 * {@code FinDashConformance}'s policy.</p>
 *
 * <p>A downstream rule is just a {@link JsRule}, the same contract the framework
 * rules implement.</p>
 */
public record DeterministicRiskRule() implements JsRule {

    public static final DeterministicRiskRule INSTANCE = new DeterministicRiskRule();

    @Override public RuleId      id()     { return new RuleId("deterministic-risk"); }
    @Override public String      intent() { return "Served code must be reproducible — no Math.random() or wall-clock Date.now(); time and randomness are explicit inputs, and an identity comes from the workspace, not from chance."; }
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
                        "non-deterministic call (pass time/randomness as inputs; take an identity from the workspace): " + line.trim(), i));
            }
        }
        return List.copyOf(findings);
    }
}
