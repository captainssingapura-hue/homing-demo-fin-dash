package hue.captains.singapura.js.homing.findash.studio;

import hue.captains.singapura.js.homing.conformance.rules.CrateClosure;
import hue.captains.singapura.js.homing.conformance.studio.TopLevelCrates;
import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.studio.base.DefaultFixtures;
import hue.captains.singapura.js.homing.studio.base.Fixtures;
import hue.captains.singapura.js.homing.studio.base.Umbrella;
import hue.captains.singapura.js.homing.workspace.shell.GenericWorkspace;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceSpecRegistry;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.ontology.ValueObject;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Fixtures for the fin-dash dashboard studio: {@link DefaultFixtures} plus the
 * workspace shell and the {@code "risk-dashboard"} spec, parameterized by the
 * demo's own crates. Mirrors the framework's {@code ConformanceStudioFixtures}.
 *
 * <p>Register data GET actions in {@link #harnessGetActions()} as the real
 * widgets land (e.g. {@code /risk/var}, {@code /risk/positions}); the placeholder
 * needs none. The {@link #servableModuleClasses()} override arms the runtime
 * crate-gate — the server refuses to serve any module not in a registered crate,
 * so a widget can't reach the browser while escaping conformance.</p>
 *
 * @param umbrella the studio identity (landing + brand)
 * @param topLevel the fin-dash's own crates
 */
public record RiskDashboardFixtures(Umbrella<RiskDashboardStudio> umbrella, List<Crate> topLevel)
        implements Fixtures<RiskDashboardStudio>, ValueObject {

    public RiskDashboardFixtures {
        Objects.requireNonNull(umbrella, "umbrella");
        topLevel = List.copyOf(Objects.requireNonNull(topLevel, "topLevel"));
        if (WorkspaceSpecRegistry.INSTANCE.get(RiskDashboardWorkspaceSpec.INSTANCE.kind()).isEmpty()) {
            WorkspaceSpecRegistry.INSTANCE.register(RiskDashboardWorkspaceSpec.INSTANCE);
        }
    }

    private DefaultFixtures<RiskDashboardStudio> defaults() {
        return new DefaultFixtures<>(umbrella);
    }

    @Override
    public List<AppModule<?, ?>> harnessApps() {
        var apps = new ArrayList<>(defaults().harnessApps());
        apps.add(GenericWorkspace.INSTANCE);
        return List.copyOf(apps);
    }

    @Override
    public Map<String, GetAction<RoutingContext, ?, ?, ?>> harnessGetActions() {
        // Add the dashboard's data feeds here as widgets need them, e.g.:
        //   var actions = new LinkedHashMap<>(defaults().harnessGetActions());
        //   actions.put("/risk/var", new VarGetAction(...));
        //   return Map.copyOf(actions);
        return defaults().harnessGetActions();
    }

    @Override
    public NodeChrome chromeFor(Umbrella<RiskDashboardStudio> node) {
        return defaults().chromeFor(node);
    }

    /**
     * The runtime crate-gate allow-list: the closure of the fin-dash's own crates
     * plus the framework serving stack ({@link TopLevelCrates#ALL}) the studio
     * always mounts. A module outside this closure is in no registered crate, so
     * {@code /module} refuses to serve it.
     */
    @Override
    public Set<String> servableModuleClasses() {
        var roots = new ArrayList<Crate>(topLevel);
        roots.addAll(TopLevelCrates.ALL);
        var classes = new HashSet<String>();
        for (Crate c : CrateClosure.of(roots)) {
            for (CrateEntry e : c.entries()) classes.add(e.moduleClass());
        }
        return classes;
    }
}
