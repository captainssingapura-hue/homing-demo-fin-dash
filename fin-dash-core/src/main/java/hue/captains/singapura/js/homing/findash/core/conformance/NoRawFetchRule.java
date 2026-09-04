package hue.captains.singapura.js.homing.findash.core.conformance;

import hue.captains.singapura.js.homing.conformance.rules.DoctrineRef;
import hue.captains.singapura.js.homing.conformance.rules.Finding;
import hue.captains.singapura.js.homing.conformance.rules.JsRule;
import hue.captains.singapura.js.homing.conformance.rules.JsText;
import hue.captains.singapura.js.homing.conformance.rules.RuleId;
import hue.captains.singapura.js.homing.conformance.rules.ServedModule;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The fin-dash's own rule: a widget does not call {@code fetch()} itself.
 * It calls {@code fdk.load(url, into, onData)}, which is the desk's one HTTP
 * policy, one JSON policy, one failure rendering and one stale-response
 * guard.
 *
 * <p>The desk had thirty-two fetch sites in twenty-nine widgets, each a copy
 * of the same six lines; the stale-response guard that a rapid re-selection
 * needs existed in exactly one of them. A copy per widget is a policy per
 * widget, and a reference implementation is copied per widget again. The
 * kit module is the one place the call belongs and is exempt by name.</p>
 */
public record NoRawFetchRule() implements JsRule {

    public static final NoRawFetchRule INSTANCE = new NoRawFetchRule();

    private static final Pattern FETCH = Pattern.compile("(?<![\\w.])fetch\\s*\\(");

    @Override public RuleId      id()     { return new RuleId("no-raw-fetch"); }
    @Override public String      intent() { return "A widget loads data through fdk.load(url, into, onData) — one HTTP policy, one failure rendering, one stale-response guard — never a fetch() of its own."; }
    @Override public DoctrineRef basis()  { return new DoctrineRef("one-data-path"); }

    @Override
    public List<Finding> check(ServedModule module) {
        if (String.valueOf(module.moduleClass()).endsWith("FinDashKitModule")) return List.of();
        var findings = new ArrayList<Finding>();
        List<String> lines = JsText.stripComments(module.lines());
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (FETCH.matcher(line).find()) {
                findings.add(new Finding(module.moduleClass(), id(),
                        "raw fetch() in a widget (use fdk.load): " + line.trim(), i));
            }
        }
        return List.copyOf(findings);
    }
}
