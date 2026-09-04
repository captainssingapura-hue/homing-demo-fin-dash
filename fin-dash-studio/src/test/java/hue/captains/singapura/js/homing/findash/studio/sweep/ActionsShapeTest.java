package hue.captains.singapura.js.homing.findash.studio.sweep;

import hue.captains.singapura.js.homing.findash.studio.FinDashFixtures;
import hue.captains.singapura.js.homing.findash.studio.FinDashStudio;
import hue.captains.singapura.js.homing.findash.studio.conformance.FinDashConformance;
import hue.captains.singapura.js.homing.studio.base.Umbrella;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Every desk action answers, in-process, with a JSON object — the same
 * routing the {@code fetch} stub in the widget sweep uses, asserted on its own
 * so a broken action is named as an action and not as "a load failed" inside
 * whichever widget happened to call it first.
 *
 * <p>Actions are pure functions of the deterministic book; until this test
 * existed, twenty-one of them had no test at all.</p>
 */
class ActionsShapeTest {

    @TestFactory
    Stream<DynamicTest> everyActionAnswers() {
        var umbrella = new Umbrella.Solo<>(FinDashStudio.INSTANCE);
        var fixtures = new FinDashFixtures(umbrella, FinDashConformance.TOP_LEVEL);
        var host = new WidgetMountSweepTest.Host();
        return fixtures.harnessGetActions().keySet().stream()
                .filter(path -> path.startsWith("/fx/"))
                .map(path -> DynamicTest.dynamicTest(path, () -> {
                    Object answer = host.execute(org.graalvm.polyglot.Value.asValue(path));
                    String body = String.valueOf(answer);
                    assertFalse(body.startsWith("!"), path + " answered status " + body.substring(1));
                    JsonObject json = new JsonObject(body);
                    assertFalse(json.isEmpty(), path + " answered an empty object");
                    assertTrue(json.fieldNames().stream().noneMatch(String::isEmpty), path + " has an unnamed field");
                }));
    }

    /** The fetch stub's routing table is the server's, not a copy of it. */
    @org.junit.jupiter.api.Test
    void stubRoutesEveryDeskAction() {
        var umbrella = new Umbrella.Solo<>(FinDashStudio.INSTANCE);
        Map<String, ?> actions = new FinDashFixtures(umbrella, FinDashConformance.TOP_LEVEL).harnessGetActions();
        long desk = actions.keySet().stream().filter(p -> p.startsWith("/fx/")).count();
        assertTrue(desk >= 20, "expected the desk's actions to be routed, found " + desk);
    }
}
