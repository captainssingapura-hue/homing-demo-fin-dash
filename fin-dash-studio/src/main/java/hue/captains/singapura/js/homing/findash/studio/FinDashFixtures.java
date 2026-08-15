package hue.captains.singapura.js.homing.findash.studio;

import hue.captains.singapura.js.homing.conformance.rules.CrateClosure;
import hue.captains.singapura.js.homing.conformance.studio.TopLevelCrates;
import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.findash.audit.AuditWorkspaceSpec;
import hue.captains.singapura.js.homing.findash.audit.data.AuditGetAction;
import hue.captains.singapura.js.homing.findash.book.data.PortfoliosGetAction;
import hue.captains.singapura.js.homing.findash.core.theme.FinDashThemes;
import hue.captains.singapura.js.homing.server.ThemeRegistry;
import hue.captains.singapura.js.homing.findash.book.data.PositionsGetAction;
import hue.captains.singapura.js.homing.findash.book.data.TradeLifecycleGetAction;
import hue.captains.singapura.js.homing.findash.book.data.TradesGetAction;
import hue.captains.singapura.js.homing.findash.ipv.data.PnlGetAction;
import hue.captains.singapura.js.homing.findash.middleoffice.data.LifecycleGetAction;
import hue.captains.singapura.js.homing.findash.quant.data.CalibrationGetAction;
import hue.captains.singapura.js.homing.findash.risk.data.RiskGetAction;
import hue.captains.singapura.js.homing.findash.summary.data.SummaryGetAction;
import hue.captains.singapura.js.homing.findash.etrading.ETradingWorkspaceSpec;
import hue.captains.singapura.js.homing.findash.etrading.data.QuotingGetAction;
import hue.captains.singapura.js.homing.findash.governance.GovernanceWorkspaceSpec;
import hue.captains.singapura.js.homing.findash.governance.data.ChangesGetAction;
import hue.captains.singapura.js.homing.findash.ipv.IpvWorkspaceSpec;
import hue.captains.singapura.js.homing.findash.marketdata.MarketDataWorkspaceSpec;
import hue.captains.singapura.js.homing.findash.marketdata.data.FeedsGetAction;
import hue.captains.singapura.js.homing.findash.marketdata.data.OverridesGetAction;
import hue.captains.singapura.js.homing.findash.ontology.OntologyWorkspaceSpec;
import hue.captains.singapura.js.homing.findash.ontology.data.DataTypesGetAction;
import hue.captains.singapura.js.homing.findash.middleoffice.MiddleOfficeWorkspaceSpec;
import hue.captains.singapura.js.homing.findash.platform.PlatformWorkspaceSpec;
import hue.captains.singapura.js.homing.findash.platform.data.PlatformGetAction;
import hue.captains.singapura.js.homing.findash.quant.QuantWorkspaceSpec;
import hue.captains.singapura.js.homing.findash.risk.RiskWorkspaceSpec;
import hue.captains.singapura.js.homing.findash.sales.SalesWorkspaceSpec;
import hue.captains.singapura.js.homing.findash.summary.SummaryWorkspaceSpec;
import hue.captains.singapura.js.homing.findash.trader.TraderWorkspaceSpec;
import hue.captains.singapura.js.homing.findash.trader.data.BarriersGetAction;
import hue.captains.singapura.js.homing.findash.trader.data.BookGetAction;
import hue.captains.singapura.js.homing.findash.trader.data.ExpiriesGetAction;
import hue.captains.singapura.js.homing.findash.trader.data.PriceGetAction;
import hue.captains.singapura.js.homing.findash.trader.data.SurfaceGetAction;
import hue.captains.singapura.js.homing.studio.base.DefaultFixtures;
import hue.captains.singapura.js.homing.studio.base.Fixtures;
import hue.captains.singapura.js.homing.studio.base.Umbrella;
import hue.captains.singapura.js.homing.workspace.shell.GenericWorkspace;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceSpec;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceSpecRegistry;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.ontology.ValueObject;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Fixtures for the fin-dash umbrella studio: {@link DefaultFixtures} plus the
 * workspace shell and <b>every persona workspace spec</b> — one workspace kind
 * per human actor in the UI study, registered idempotently. Mirrors the
 * framework's {@code ConformanceStudioFixtures}.
 *
 * <p>Register data GET actions in {@link #harnessGetActions()} as the real
 * widgets land (e.g. {@code /fx/price}, {@code /fx/surface}, {@code /fx/book});
 * the home cards need none. The {@link #servableModuleClasses()} override arms
 * the runtime crate-gate — the server refuses to serve any module not in a
 * registered crate, so a widget can't reach the browser while escaping
 * conformance.</p>
 *
 * @param umbrella the studio identity (landing + brand)
 * @param topLevel the fin-dash's own crates (core + one per persona module)
 */
public record FinDashFixtures(Umbrella<FinDashStudio> umbrella, List<Crate> topLevel)
        implements Fixtures<FinDashStudio>, ValueObject {

    /** Every persona workspace — one spec (one kind) per human actor. */
    private static final List<WorkspaceSpec> SPECS = List.of(
            TraderWorkspaceSpec.INSTANCE,
            ETradingWorkspaceSpec.INSTANCE,
            SalesWorkspaceSpec.INSTANCE,
            MarketDataWorkspaceSpec.INSTANCE,
            QuantWorkspaceSpec.INSTANCE,
            RiskWorkspaceSpec.INSTANCE,
            GovernanceWorkspaceSpec.INSTANCE,
            MiddleOfficeWorkspaceSpec.INSTANCE,
            IpvWorkspaceSpec.INSTANCE,
            PlatformWorkspaceSpec.INSTANCE,
            AuditWorkspaceSpec.INSTANCE,
            SummaryWorkspaceSpec.INSTANCE,
            OntologyWorkspaceSpec.INSTANCE);

    public FinDashFixtures {
        Objects.requireNonNull(umbrella, "umbrella");
        topLevel = List.copyOf(Objects.requireNonNull(topLevel, "topLevel"));
        for (WorkspaceSpec spec : SPECS) {
            if (WorkspaceSpecRegistry.INSTANCE.get(spec.kind()).isEmpty()) {
                WorkspaceSpecRegistry.INSTANCE.register(spec);
            }
        }
    }

    private DefaultFixtures<FinDashStudio> defaults() {
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
        var actions = new LinkedHashMap<>(defaults().harnessGetActions());
        // Trader (W1/W2/W4) feeds — deterministic demo data (P5: the UI consumes).
        actions.put("/fx/price", new PriceGetAction());
        actions.put("/fx/surface", new SurfaceGetAction());
        actions.put("/fx/book", new BookGetAction());
        actions.put("/fx/barriers", new BarriersGetAction());
        actions.put("/fx/expiries", new ExpiriesGetAction());
        // e-Trading (W3) feed.
        actions.put("/fx/quoting", new QuotingGetAction());
        // Market-data ops feeds.
        actions.put("/fx/feeds", new FeedsGetAction());
        actions.put("/fx/overrides", new OverridesGetAction());
        // Governance (W6) feed.
        actions.put("/fx/changes", new ChangesGetAction());
        // Platform SRE (W5) feed.
        actions.put("/fx/platform", new PlatformGetAction());
        // Risk, MO, IPV, audit, quant, summary feeds.
        actions.put("/fx/risk", new RiskGetAction());
        actions.put("/fx/lifecycle", new LifecycleGetAction());
        actions.put("/fx/pnl", new PnlGetAction());
        actions.put("/fx/audit", new AuditGetAction());
        actions.put("/fx/calibration", new CalibrationGetAction());
        actions.put("/fx/summary", new SummaryGetAction());
        // Shared book feeds (portfolio hierarchy + positions + trade journal).
        actions.put("/fx/portfolios", new PortfoliosGetAction());
        actions.put("/fx/positions", new PositionsGetAction());
        actions.put("/fx/trades", new TradesGetAction());
        // One trade's simulated post-execution life; seeded from the trade itself.
        actions.put("/fx/trade-lifecycle", new TradeLifecycleGetAction());
        // The ontology catalogue — the demo's own data model, browsable.
        actions.put("/fx/data-types", new DataTypesGetAction());
        return Map.copyOf(actions);
    }

    @Override
    public NodeChrome chromeFor(Umbrella<FinDashStudio> node) {
        return defaults().chromeFor(node);
    }

    /**
     * The framework's themes plus fin-dash's own (docs/adding-a-theme.md §3).
     * Without this override the theme compiles and registers but is never
     * reachable — the server keeps serving the default registry.
     *
     * <p>{@code defaultTheme()} is deliberately NOT overridden: the desk opens
     * on Default and the Terminal look is one pick away in the header. Flip it
     * by returning {@code FinDashTerminal.INSTANCE} from a {@code defaultTheme()}
     * override here.</p>
     */
    @Override
    public ThemeRegistry themeRegistry() {
        return FinDashThemes.INSTANCE;
    }

    /**
     * The runtime crate-gate allow-list: the closure of the fin-dash's own
     * crates plus the framework serving stack ({@link TopLevelCrates#ALL}) the
     * studio always mounts. A module outside this closure is in no registered
     * crate, so {@code /module} refuses to serve it.
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
