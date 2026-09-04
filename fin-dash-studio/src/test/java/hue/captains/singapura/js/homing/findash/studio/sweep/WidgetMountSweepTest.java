package hue.captains.singapura.js.homing.findash.studio.sweep;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.findash.studio.FinDashFixtures;
import hue.captains.singapura.js.homing.findash.studio.FinDashStudio;
import hue.captains.singapura.js.homing.findash.studio.conformance.FinDashConformance;
import hue.captains.singapura.js.homing.studio.base.Umbrella;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;
import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.Param;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.RecordComponent;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * The runtime sweep, automated: <b>mount every widget</b>.
 *
 * <p>For each {@link WorkspaceWidget} in every top-level crate, in a fresh
 * GraalJS context: evaluate the browser stub, load the widget's <em>served</em>
 * module and everything it imports, mint a DomOpsParty branch the way the
 * shell does, and call {@code construct(branch, {}, workspaceCtx)} with a
 * recording party and {@code fetch} routed to the desk's real actions. Then
 * {@code setActive(true)}, {@code setActive(false)}, {@code partyDeregister()},
 * {@code branch.dissolve()}.</p>
 *
 * <p>What it asserts is what the manual sweep looked for: the controller has a
 * root that is not the framework's "Widget failed to construct" element; the
 * console recorded no error; every actor that joined the party left it; and
 * every interval the widget set was cleared by teardown. The defects the manual
 * sweep found this month — a fixed element name minted once per row, an
 * undissolved branch that threw on the second render, a stale response painting
 * over a newer one — are all construct-time or teardown-time, and this is the
 * gate that would have caught them.</p>
 *
 * <p>Widgets listed in {@link #SKIP} are rendering leaves the stub cannot
 * host, each with the reason beside it.</p>
 */
class WidgetMountSweepTest {

    /** Widgets the stub cannot host, and why. */
    private static final Map<String, String> SKIP = Map.of(
            "VolSurfaceWidget", "WebGL: three.js needs a canvas context the stub does not provide");

    private static final String STUB = readResource("/sweep/BrowserStub.js");
    private static final String DOM_OPS_PARTY = "hue.captains.singapura.js.homing.core.js.DomOpsPartyModule";

    @TestFactory
    Stream<DynamicTest> mountEveryWidget() {
        var widgets = new ArrayList<EsModule<?>>();
        for (Crate crate : FinDashConformance.TOP_LEVEL) {
            for (CrateEntry entry : crate.entries()) {
                if (entry.module() instanceof WorkspaceWidget<?, ?>) widgets.add(entry.module());
            }
        }
        assertTrue(widgets.size() >= 40, "expected the desk's widgets, found " + widgets.size());
        return widgets.stream().map(w -> DynamicTest.dynamicTest(
                w.getClass().getSimpleName(), () -> mount(w)));
    }

    private void mount(EsModule<?> widget) {
        String simple = widget.getClass().getSimpleName();
        if (SKIP.containsKey(simple)) return;   // reason recorded beside the name
        String className = widget.getClass().getName();
        try (Context js = Context.newBuilder("js")
                .allowAllAccess(false)
                .option("js.ecmascript-version", "2022")
                .build()) {
            js.getBindings("js").putMember("__host", new Host());
            js.eval("js", STUB);
            var loader = new ServedModuleLoader();
            loader.load(js, DOM_OPS_PARTY);
            loader.load(js, className);
            Value result = mountAndReport(js, className);
            var problems = problemsIn(result);
            if (!problems.isEmpty()) fail(simple + ":\n  " + String.join("\n  ", problems));
        }
    }

    /**
     * Two evaluations, not one. GraalJS runs the promise jobs an evaluation
     * queued when that evaluation returns — so a widget's construct-time
     * {@code fdk.load} has not painted when {@code construct} returns, and a
     * report taken in the same evaluation would see only the synchronous
     * half. Mount; return to Java (the jobs run); then exercise, tear down and
     * report.
     */
    private static Value mountAndReport(Context js, String className) {
        js.eval("js", """
                (function () {
                  globalThis.__joined = []; globalThis.__left = [];
                  var party = {
                    joinActor: function (a) { __joined.push(a.id); },
                    leave:     function (id) { __left.push(id); },
                    tellFrom:  function () {}
                  };
                  var dom = globalThis.__mod["%s"];
                  var root = dom.domOpsParty || new dom.DomOpsParty('root');
                  globalThis.__b = root.createBranch('w-sweep');
                  __b.activate({ toString: function () { return 'sweep'; } });
                  globalThis.__ctl = globalThis.__mod["%s"].construct(__b, {}, { deskParty: party });
                })()
                """.formatted(DOM_OPS_PARTY, className));
        return js.eval("js", """
                (function () {
                  var ctl = globalThis.__ctl;
                  var text = ctl && ctl.root ? String(ctl.root.textContent) : '';
                  var errorLines = 0;
                  (function walk(n) {
                    if (!n) return;
                    if (n.classList && n.classList.contains('fd-error-text')) errorLines++;
                    for (var i = 0; n.children && i < n.children.length; i++) walk(n.children[i]);
                  })(ctl && ctl.root);
                  if (ctl && typeof ctl.setActive === 'function') { ctl.setActive(true); ctl.setActive(false); }
                  if (ctl && typeof ctl.partyDeregister === 'function') ctl.partyDeregister();
                  __b.dissolve();
                  return {
                    hasRoot: !!(ctl && ctl.root),
                    failed: text.indexOf('Widget failed to construct') >= 0 ? text : '',
                    loadFailed: text.indexOf('load failed') >= 0 ? text.slice(text.indexOf('load failed') - 40, text.indexOf('load failed') + 80) : '',
                    errorLines: errorLines,
                    errors: __console.errors.slice(),
                    fetches: __fetches.map(function (f) { return f.status + ' ' + f.url; }),
                    badFetches: __fetches.filter(function (f) { return f.status !== 200; }).map(function (f) { return f.status + ' ' + f.url; }),
                    joined: __joined.length, left: __left.length,
                    intervalsLeft: __timers.intervals.length
                  };
                })()
                """);
    }

    private static List<String> problemsIn(Value result) {
        var problems = new ArrayList<String>();
        if (!result.getMember("hasRoot").asBoolean()) problems.add("construct returned no root");
        String failed = result.getMember("failed").asString();
        if (!failed.isEmpty()) problems.add(failed);
        String loadFailed = result.getMember("loadFailed").asString();
        if (!loadFailed.isEmpty()) problems.add("a load failed after mount: …" + loadFailed.trim() + "…");
        long errorLines = result.getMember("errorLines").asLong();
        if (errorLines != 0) problems.add(errorLines + " fd-error-text line(s) rendered");
        Value bad = result.getMember("badFetches");
        for (long i = 0; i < bad.getArraySize(); i++) problems.add("fetch answered " + bad.getArrayElement(i).asString());
        Value errs = result.getMember("errors");
        for (long i = 0; i < errs.getArraySize(); i++) problems.add("console.error: " + errs.getArrayElement(i).asString());
        long joined = result.getMember("joined").asLong(), left = result.getMember("left").asLong();
        if (joined != left) problems.add("actors joined " + joined + ", left " + left);
        long intervals = result.getMember("intervalsLeft").asLong();
        if (intervals != 0) problems.add(intervals + " interval(s) still scheduled after teardown");
        return problems;
    }

    /**
     * The sweep must be able to fail. A synthetic widget that mints the same
     * element name twice on one branch — the exact defect the manual sweep
     * found in two widgets this month — has to be reported, or a green sweep
     * means nothing.
     */
    @org.junit.jupiter.api.Test
    void canaryDuplicateNameIsCaught() {
        try (Context js = Context.newBuilder("js")
                .allowAllAccess(false)
                .option("js.ecmascript-version", "2022")
                .build()) {
            js.getBindings("js").putMember("__host", new Host());
            js.eval("js", STUB);
            new ServedModuleLoader().load(js, DOM_OPS_PARTY);
            js.eval("js", """
                    globalThis.__mod["canary"] = { construct: function (branch, params, ctx) {
                      try {
                        var root = branch.createElement('root', 'div');
                        for (var i = 0; i < 2; i++) root.appendChild(branch.createElement('row', 'div'));
                        return { root: root, setActive: function () {} };
                      } catch (e) {
                        console.error('WorkspaceWidget construct failed:', e);
                        var err = branch.createElement('__widgetError', 'div');
                        err.textContent = 'Widget failed to construct: ' + e.message;
                        return { root: err, setActive: function () {} };
                      }
                    } };
                    """);
            var problems = problemsIn(mountAndReport(js, "canary"));
            assertTrue(problems.stream().anyMatch(p -> p.contains("Widget failed to construct")),
                    "the sweep did not report a duplicate element name: " + problems);
        }
    }

    /** {@code fetch} → the desk's actions, exactly as the server would route them. */
    static final class Host implements ProxyExecutable {
        private final Map<String, GetAction<?, ?, ?, ?>> actions = new LinkedHashMap<>();

        Host() {
            var umbrella = new Umbrella.Solo<>(FinDashStudio.INSTANCE);   // as FinDashServer builds it
            var fixtures = new FinDashFixtures(umbrella, FinDashConformance.TOP_LEVEL);
            fixtures.harnessGetActions().forEach(actions::put);
        }

        @Override
        public Object execute(Value... args) {
            String url = args[0].asString();
            int q = url.indexOf('?');
            String path = q < 0 ? url : url.substring(0, q);
            var params = new LinkedHashMap<String, String>();
            if (q >= 0) for (String kv : url.substring(q + 1).split("&")) {
                int e = kv.indexOf('=');
                String k = e < 0 ? kv : kv.substring(0, e), v = e < 0 ? "" : kv.substring(e + 1);
                params.put(URLDecoder.decode(k, StandardCharsets.UTF_8), URLDecoder.decode(v, StandardCharsets.UTF_8));
            }
            GetAction<?, ?, ?, ?> action = actions.get(path);
            if (action == null) return "!404";
            try {
                return invoke(action, params);
            } catch (Exception e) {
                return "!500";
            }
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        private static String invoke(GetAction action, Map<String, String> params) throws Exception {
            Object query = null;
            for (Class<?> nested : action.getClass().getDeclaredClasses()) {
                if (nested.isRecord() && nested.getSimpleName().equals("Query")) {
                    RecordComponent[] cs = nested.getRecordComponents();
                    Class<?>[] types = new Class<?>[cs.length];
                    Object[] vals = new Object[cs.length];
                    for (int i = 0; i < cs.length; i++) { types[i] = cs[i].getType(); vals[i] = params.get(cs[i].getName()); }
                    query = nested.getDeclaredConstructor(types).newInstance(vals);
                }
            }
            if (query == null) query = new EmptyParam.NoQuery();
            Object content = action.execute((Param._QueryString) query, new EmptyParam.NoHeaders()).get();
            return (String) content.getClass().getMethod("body").invoke(content);
        }
    }

    private static String readResource(String path) {
        try (InputStream in = WidgetMountSweepTest.class.getResourceAsStream(path)) {
            if (in == null) throw new IllegalStateException("missing test resource " + path);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
