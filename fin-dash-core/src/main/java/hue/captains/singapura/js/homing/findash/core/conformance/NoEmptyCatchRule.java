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
 * The fin-dash's own rule: <b>no empty catch</b> in served code.
 *
 * <p>{@code catch (e) {}} teaches that a failure may be discarded. In a
 * reference implementation that lesson is copied before it is questioned —
 * the desk had twenty-five of them, all wrapping calls that cannot throw on
 * a legitimate path ({@code party.leave} is idempotent; {@code destroy()}
 * on a built grid has no throwing branch) and all hiding what the shell's
 * own lifecycle {@code try/catch} would otherwise have logged. The fix is
 * never the catch: either the guard makes the throw impossible, or the
 * failure is a real state and gets a real handler.</p>
 *
 * <p>Applied to every served fin-dash module type through
 * {@code FinDashConformance}'s policy, not to one type — the practice is not
 * specific to widgets.</p>
 */
public record NoEmptyCatchRule() implements JsRule {

    public static final NoEmptyCatchRule INSTANCE = new NoEmptyCatchRule();

    // `catch (e) {}` and `catch {}` on one line, any whitespace inside the braces.
    private static final Pattern ONE_LINE = Pattern.compile("\\bcatch\\s*(\\([^)]*\\))?\\s*\\{\\s*\\}");
    // A catch that opens on one line …
    private static final Pattern OPENS    = Pattern.compile("\\bcatch\\s*(\\([^)]*\\))?\\s*\\{\\s*$");
    // … and closes on the next with nothing between.
    private static final Pattern CLOSES   = Pattern.compile("^\\s*\\}");

    @Override public RuleId      id()     { return new RuleId("no-empty-catch"); }
    @Override public String      intent() { return "No empty catch block in served code — make the throw impossible with a guard, or handle the failure; never discard it."; }
    @Override public DoctrineRef basis()  { return new DoctrineRef("failures-are-not-discarded"); }

    @Override
    public List<Finding> check(ServedModule module) {
        var findings = new ArrayList<Finding>();
        // Comments stripped (positions preserved) so prose about the pattern
        // does not count; only code does.
        List<String> lines = JsText.stripComments(module.lines());
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            boolean hit = ONE_LINE.matcher(line).find()
                    || (OPENS.matcher(line).find()
                        && i + 1 < lines.size()
                        && CLOSES.matcher(lines.get(i + 1)).find());
            if (hit) {
                findings.add(new Finding(module.moduleClass(), id(),
                        "empty catch discards a failure (guard the call, or handle it): " + line.trim(), i));
            }
        }
        return List.copyOf(findings);
    }
}
