package hue.captains.singapura.js.homing.findash.studio.sweep;

import hue.captains.singapura.js.homing.conformance.engine.ServedModuleRenderer;
import hue.captains.singapura.js.homing.core.EsModule;
import org.graalvm.polyglot.Context;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Loads a served module — the exact text the browser would receive from
 * {@code /module?class=X}, rendered in-process by the conformance engine's
 * {@link ServedModuleRenderer} — into a GraalJS {@link Context}, resolving
 * its {@code import … from "/module?class=Y"} lines depth-first.
 *
 * <p>GraalJS can evaluate ES modules, but only with a file-system-shaped
 * loader; the served graph is name-shaped. So each module is evaluated as a
 * script wrapped in an IIFE: imports become locals bound from a registry of
 * already-loaded exports, the trailing {@code export {…}} becomes an entry in
 * that registry, and nothing else a module declares at top level leaks. That
 * is the same scoping the browser gives a module, which is the point — the
 * sweep runs the artifact, not a transcription of it.</p>
 */
public final class ServedModuleLoader {

    private static final Pattern IMPORT = Pattern.compile(
            "^\\s*import\\s*\\{([^}]*)\\}\\s*from\\s*\"/module\\?class=([^\"]+)\";?\\s*$");
    private static final Pattern EXPORT = Pattern.compile("^\\s*export\\s*\\{([^}]*)\\};?\\s*$");

    private final ServedModuleRenderer renderer = new ServedModuleRenderer();
    private final Set<String> loaded = new HashSet<>();

    /** Loads {@code className} and everything it imports; idempotent per context. */
    public void load(Context js, String className) {
        if (!loaded.add(className)) return;
        String text = renderer.render(instanceOf(className)).text();
        var binds   = new ArrayList<String>();
        var body    = new ArrayList<String>();
        String exports = "";
        for (String line : text.split("\\r?\\n")) {
            Matcher im = IMPORT.matcher(line);
            if (im.matches()) {
                String dep = im.group(2);
                load(js, dep);
                for (String spec : im.group(1).split(",")) {
                    spec = spec.trim();
                    if (spec.isEmpty()) continue;
                    String[] as = spec.split("\\s+as\\s+");
                    String from = as[0].trim(), to = as.length > 1 ? as[1].trim() : from;
                    binds.add("var " + to + " = globalThis.__mod[\"" + dep + "\"]." + from + ";");
                }
                continue;
            }
            Matcher ex = EXPORT.matcher(line);
            if (ex.matches()) { exports = ex.group(1); continue; }
            // A generated CSS module opens with a top-level `await _css.loadCss(…)`
            // — legal in a module, not in the script an IIFE is. The load is a
            // <link> the stub never resolves, and nothing after it needs it to;
            // column-0 only, so an await inside an async function is untouched.
            if (line.startsWith("await ")) line = line.substring("await ".length());
            body.add(line);
        }
        var names = new ArrayList<String>();
        for (String n : exports.split(",")) if (!n.isBlank()) names.add(n.trim());
        var script = new StringBuilder("(function () {\n");
        binds.forEach(b -> script.append(b).append('\n'));
        body.forEach(l -> script.append(l).append('\n'));
        script.append("globalThis.__mod[\"").append(className).append("\"] = {");
        for (int i = 0; i < names.size(); i++) {
            if (i > 0) script.append(", ");
            script.append(names.get(i)).append(": ").append(names.get(i));
        }
        script.append("};\n})();\n");
        try {
            js.eval("js", script.toString());
        } catch (RuntimeException e) {
            // Say which module, and show enough of the script to see why: an
            // "Unnamed:3:0" from the engine is not a diagnosis.
            String[] ls = script.toString().split("\n");
            var head = new StringBuilder();
            for (int i = 0; i < Math.min(ls.length, 12); i++) head.append(i + 1).append(": ").append(ls[i]).append('\n');
            throw new IllegalStateException("evaluating " + className + " failed: " + e.getMessage() + "\n" + head, e);
        }
    }

    /** The same resolution the server's {@code /module} endpoint performs. */
    static EsModule<?> instanceOf(String className) {
        try {
            Class<?> c = Class.forName(className);
            return (EsModule<?>) c.getField("INSTANCE").get(null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("not a served module with an INSTANCE: " + className, e);
        }
    }

    public List<String> loadedClasses() { return List.copyOf(loaded); }
}
