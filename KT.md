# Knowledge Transfer — Financial Risk Dashboard on the Homing Workspace

**Audience:** the downstream agent building `homing-risk-dashboard` — a set of demo
widgets for a financial risk‑management dashboard, served on the homing workspace,
with the RFC 0044 conformance test wired in.

**How to read this:** §1–§2 are orientation (read once). §3–§8 are build recipes.
§9 is the gotcha list — **read it before you write a line of build config**; every
item there cost real debugging time. §10 is a copy‑paste starter checklist.

> **Golden rule:** you are not working from a blank framework. `homing-doc-plus-demo`
> is a complete, working downstream that already does everything you need — serving,
> the Crate model, the conformance gate, a conformance studio, and a policy
> *extension*. **Copy its patterns and verify exact API signatures against its
> source.** Whenever this doc and the actual framework source disagree, the source
> wins — cite the file, don't trust prose (including this doc's) for signatures.

---

## 1. Orientation — the reference implementation

Everything below has a working twin in `Q:\repos\java\homing\homing-doc-plus-demo`,
module `homing-demo`. Read these first; they are your templates:

| Concern | Reference file (in `homing-demo/src/main/java/hue/captains/singapura/js/homing/demo/…`) |
|---|---|
| A served widget (DOM) | `es/game/MovingAnimalWidget.java`, `es/animation/DancingAnimalsWidget.java` |
| A headless logic module | `es/game/platformer/JumpPhysics.java`, `playground/AnimalsSecretaryModule.java` |
| The multi‑studio server | `studio/DemoStudioServer.java`, `studio/multi/MultiStudio.java` |
| **The Crate** (register for serving + conformance) | `conformance/HomingDemoCrate.java` |
| **Conformance config** (gate + studio share it) | `conformance/DemoConformance.java` |
| **Build‑time report export** | `conformance/DemoConformanceExport.java` |
| **The conformance studio server** | `conformance/DemoConformanceStudioServer.java` |
| **Policy extension** (own module type + rule set) | `conformance/ext/GameLoopModuleType.java`, `ext/RafGameLoopRule.java`, `ext/GameLoopConformance.java` |
| The conformance gate test | `src/test/.../conformance/DemoConformanceTest.java` |
| The baseline (grandfathered findings) | `src/main/resources/demo-conformance-baseline.txt` |

And in the framework repo `Q:\repos\java\homing\homing-ssjs-core`, the studio itself
(module `homing-conformance-studio`) — you *reuse* these as a dependency, but read
them to understand the shapes:

- `ConformanceStudio.java` / `ConformanceLandingCatalogue.java` — a thin `Studio` identity + landing.
- `ConformanceWorkspaceSpec.java` — a `WorkspaceSpec` (kind `"conformance"`) registering its widgets. **This is the direct template for your dashboard's WorkspaceSpec.**
- `ConformanceStudioFixtures.java` — the composition root: `(Umbrella, List<Crate>)` → routes + workspace registration.
- `ConformanceStudioServer.java` — `Bootstrap` + `Umbrella.Solo` + `DefaultRuntimeParams(port)`.
- `ConformanceReportWidget.java` / `ModuleTreeWidget.java` — real `WorkspaceWidget`s (fetch a GetAction, render DOM, talk on the party bus).

---

## 2. The homing mental model (must‑read)

Six concepts. Get these and the rest is mechanical.

1. **Served module (`EsModule<T>`).** Every piece of front‑end is a Java object that
   *emits* JS. A `DomModule<T>` renders DOM; a headless module (secretary / pure
   logic) emits JS with no DOM. The **served artifact** (the JS that reaches the
   browser) is what conformance inspects — not your Java. Cross‑module references
   are type‑safe via `Exportable` markers, not string imports.

2. **Widget (`WorkspaceWidget<Params, Self>`).** A workspace‑mounted UI unit. You
   implement `title()`, `paramsType()`, `bodyImports()` (typed JS deps), and
   `constructBodyJs()` (the body of the widget's factory function, returning
   `{root, setActive, partyDeregister}`). It fetches its data from a **GetAction**
   (an HTTP endpoint) and renders into `branch`‑created DOM.

3. **Workspace + WorkspaceSpec.** The dashboard shell is the framework's
   `GenericWorkspace` (split‑pane, tabs, layout persistence — all free). You supply
   a `WorkspaceSpec` (a *kind* string + a roster of `WidgetEntry`s). The user opens
   panes and picks your widgets. Widgets talk to each other over a **party bus**
   (a "secretary" re‑broadcasts events, e.g. "portfolio X selected" → every widget
   refreshes). Template: `ConformanceWorkspaceSpec` + `NavigatorSecretary`.

4. **Studio + server.** A `Studio<L0>` is a thin identity: a landing catalogue
   (`home()`) + a `StudioBrand`. `Fixtures` wire the workspace spec + your data
   GetActions. `Bootstrap<>(fixtures, new DefaultRuntimeParams(port)).start()` runs
   it. Compose several studios under one server with `Umbrella.Group`.

5. **Crate (梱包).** *One `Crate` per Maven module*, declaring **every** served
   module that module ships. It is the unit of (a) organization, (b) discovery
   *without a runtime classpath scan* (walk `requires()` to enumerate the world),
   and (c) conformance. `requires()` names the framework crates your modules import.
   **This constraint is load‑bearing — see §9.1.**

   > **Serving is crate‑gated (RFC 0044).** Historically `/module?class=…`
   > resolved a module purely by reflection and served *anything* on the classpath
   > — a module could reach the browser while escaping conformance. That leak is
   > now closed on both sides:
   > - **Runtime:** `EsModuleGetAction` takes a `servable` allow‑list (the
   >   registered crate closure). When set, it **refuses (404)** any class outside
   >   it. `ConformanceStudioFixtures` sets it automatically; for your own
   >   dashboard `Fixtures`, override `servableModuleClasses()` to return your
   >   served closure (§5). Leave it `null` only for a deliberately permissive
   >   server.
   > - **Build:** `CrateCoverage.check(closure, appAnchors)` fails the build if any
   >   served module in your Maven module(s) is in no crate. Add it to your gate
   >   (§7b).
   >
   > Net: a served module **must** be crated — you can't ship one that skips
   > conformance. (The in‑process render path conformance itself uses is never
   > gated.)

6. **Conformance.** A `JsRulePolicy` maps each module *type* → a `JsRuleSet`. The
   engine renders each crate module's served artifact, runs its rule set, and a
   `FindingGrader` grades findings against a committed **baseline** (grandfathered
   warnings) — new violations fail the build. The policy is **open for extension**:
   you can register your own module types + rule sets without touching the framework
   (`DefaultJsRulePolicy.INSTANCE.extendedWith(...)`). The **conformance studio**
   visualizes all of this.

---

## 3. Project scaffold (Maven)

A single Maven module is enough to start (`homing-risk-dashboard`); split later if
it grows. **One Maven module ⇒ one Crate** (§9.1).

**Java 21.** Copy the compiler/properties block and the dependency shape from
`homing-demo/pom.xml`. Minimum dependencies (all `groupId`
`io.github.captainssingapura-hue.homing.js`, version `${homing.core.version}`):

- `homing-studio-starter` (workspace + studio-base + server, transitively) — the serving stack.
- `homing-conformance-studio` — **compile scope**. Drags in the engine + rules; lets you both *gate* (test) and *serve a conformance studio* (main). This is what the demo does.
- `junit-jupiter` (test), `homing-ssjs-test-fixtures` (test, only if you write JS‑execution tests).

**Version — read carefully.** The RFC 0044 conformance‑studio features you rely on
(open `JsModuleType`, `extendedWith`, `ConformanceStudioFixtures`, the package‑nested
crate tree, per‑module rule sets) are **freshly built and may not be in a published
homing release yet.** Until they are:

- Build & install the framework locally: in `homing-ssjs-core`, run `mvn -o install`
  (skip tests with `-DskipTests` if you just need the jars). This publishes
  `…:LOCAL-SNAPSHOT` into your local `~/.m2`.
- Set `<homing.core.version>LOCAL-SNAPSHOT</homing.core.version>` in your pom
  (the demo does exactly this).
- Confirm the jar carries what you need, e.g.
  `jar tf ~/.m2/…/homing-conformance-studio-LOCAL-SNAPSHOT.jar | grep ReportCodecsModule.js`.

Once a release ships these, switch `homing.core.version` to that release number.

---

## 4. Anatomy of a served module & a widget

### 4a. A DOM widget (the workhorse)
Study `MovingAnimalWidget` / `ConformanceReportWidget`, then follow this shape:

```java
public final class VarGaugeWidget extends WorkspaceWidget<WorkspaceWidget._None, VarGaugeWidget> {
    public static final VarGaugeWidget INSTANCE = new VarGaugeWidget();
    private VarGaugeWidget() {}
    private record construct() implements WorkspaceWidget._Construct<_None, VarGaugeWidget> {}

    @Override protected _Construct<_None, VarGaugeWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Value at Risk"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(/* typed JS deps, e.g. a renderer module — see ModuleTreeWidget */);
    }

    @Override protected List<String> constructBodyJs() {
        return List.of(
            "    var root = branch.createElement('root', 'div');",
            "    // fetch('/risk/var').then(r => r.json()).then(render);",
            "    return { root: root, setActive: function(a){}, partyDeregister: function(){} };");
    }
}
```

Key facts (all verifiable in the reference widgets):
- The JS in `constructBodyJs()` is **the served artifact conformance checks** — so it must obey the rules (no CDN imports, DOM discipline, ≤250 effective lines, etc. — §7).
- Use `branch.createElement(id, tag)` for owned DOM; talk to peers via the party
  bus (`workspaceCtx.navParty`, `NodeSelected` / `OpenDoc` protocol — copy from
  `ModuleTreeWidget` + `ConformanceWorkspaceSpec`'s secretary).
- Data comes from a **GetAction** you register in your Fixtures (§5). Start with
  plain JSON; adopt the typed **polyglot codec** only if you want typed decode in
  the browser (that's the `ReportCodecManifest` / `ReportJsCodecGen` machinery —
  advanced; skip until you need it).

### 4b. A headless logic module
Risk calculators (VaR, exposure, stress) should be **headless** — a secretary or
pure‑logic module that emits JS with **no DOM access at all**. Template:
`AnimalsSecretaryModule` / `JumpPhysics`. These get the strict no‑DOM rule set,
which is exactly what you want for deterministic risk math.

### 4c. Companion resources
If a module's JS body lives in a co‑located `.js` file (not inline
`constructBodyJs`), or it ships `.svg`/`.css` assets, those resources live under
`src/main/resources/homing/{js,svg,css}/<the module's package path>/…`. **The
resource path mirrors the class package** — see §9.4.

---

## 5. The dashboard: WorkspaceSpec + studio + server

Your dashboard is *structurally the conformance studio* with your own spec, widgets,
and data endpoints. Copy these four files and rename:

1. **`RiskDashboardWorkspaceSpec`** (from `ConformanceWorkspaceSpec`): `kind()` →
   `"risk-dashboard"`; `widgetEntries()` → your widgets grouped
   (`WidgetGroup.of("Overview")`, `WidgetGroup.of("Detail")`) with
   `WidgetEntry.of(VarGaugeWidget.class, WidgetLabel.of("VaR")).withIcon(new WidgetIcon.Emoji("📉"))`.
   Register it once (the Fixtures constructor does this via `WorkspaceSpecRegistry`).

2. **`RiskDashboardStudio`** + **`RiskDashboardLandingCatalogue`** (from
   `ConformanceStudio` + `ConformanceLandingCatalogue`): a thin `Studio` identity;
   the landing's single leaf opens `GenericWorkspace` with
   `new GenericWorkspace.Params("risk-dashboard")`.

3. **`RiskDashboardFixtures`** (from `ConformanceStudioFixtures`): in
   `harnessGetActions()`, register your data endpoints
   (`actions.put("/risk/var", new VarGetAction(...))`, etc.), and register the
   workspace spec. `harnessApps()` adds `GenericWorkspace.INSTANCE`.

4. **`RiskDashboardServer`** (from `ConformanceStudioServer`): 
   `new Bootstrap<>(new RiskDashboardFixtures(new Umbrella.Solo<>(RiskDashboardStudio.INSTANCE), CRATES), new DefaultRuntimeParams(port)).start();`
   Pick a port that doesn't clash (demo uses 8082/8091; try **8100**).

Run it with (note: `classpathScope` is **not** required for `exec:java` — its
default `runtime` already includes your compile output + compile deps):
```
mvn -o -pl homing-risk-dashboard exec:java -Dexec.mainClass=…RiskDashboardServer
```
Landing at `/`; workspace at `/app?app=genericWorkspace&ws_kind=risk-dashboard`.

---

## 6. The Crate model (register for serving + conformance)

Copy `HomingDemoCrate` → **`RiskDashboardCrate`**. It declares **every** served
module in your Maven module (the `OrphanCheck` scans the whole module output and
fails on any you omit — §9.1). Don't hand‑transcribe: generate the list with the
framework's `CrateSeed` aid.

```java
// throwaway test in your module:
System.out.println(CrateSeed.suggest(VarGaugeWidget.class)); // prints paste‑ready imports + entries()
```

Then:
- Paste the generated `entries()` in.
- Declare any domain‑typed modules explicitly: `CrateEntry.of(VarModel.INSTANCE, RiskModuleType.RISK_MODEL)` (§7c).
- `requires()` the framework crates your modules import — start with the demo's set
  (`CoreJsCrate, ServerCrate, StudioBaseCrate, WorkspaceCrate, WorkspaceCodecsCrate,
  WorkspacePersistenceCrate, WorkspaceShellCrate, StudioWorkspaceCrate, LibsCrate`)
  and trim/extend using the studio's **Crate Conformance** pane, which lists any
  "illegal import (crate X not required)".

---

## 7. Conformance: gate, policy, extension, studio

### 7a. One config, shared by gate + studio
Copy `DemoConformance` → **`RiskConformance`**: `TOP_LEVEL = List.of(RiskDashboardCrate.INSTANCE)`,
a `baseline()` loader reading `/risk-conformance-baseline.txt`, and a
`grader(boolean)` (`FindingGrader.STRICT.withAllowlist(...).withBaseline(baseline()).allowingPreExisting(...)`).
Expose `POLICY` (your extended policy — §7c).

### 7b. The gate test
Copy `DemoConformanceTest`. Three assertions: (1) crate integrity
(`CrateConformance.evaluate(CrateClosure.of(TOP_LEVEL))` has no orphans / illegal
imports for your crate); (2) **coverage** — no served module escapes a crate
(`CrateCoverage.check(CrateClosure.of(TOP_LEVEL), List.of(RiskDashboardCrate.class))`
is empty; pass one anchor class per Maven module you own); (3) rule conformance
(`new ConformanceEngine(RiskConformance.POLICY, new ServedModuleRenderer()).checkCrates(TOP_LEVEL)`
graded → no errors). Pre‑existing findings go in the baseline as warnings; new ones
fail. Run once, then baseline whatever it surfaces (§9.2/§9.3 on the baseline file).

### 7c. Extend the policy for the risk domain (the showcase)
This is why the workspace + conformance combo is interesting for a downstream. Mirror
`ext/GameLoop*`:

- **`RiskModuleType`** — `enum implements JsModuleType { RISK_MODEL }`,
  `slug()="risk-model"`, `label()="Risk model"`. (Open interface = the unsealed
  extension branch; the framework keeps its exhaustive switch over
  `StandardJsModuleType`.)
- **A bespoke `JsRule`** — e.g. `DeterministicRiskRule` (id `"deterministic-risk"`):
  a risk model must not call `Math.random(` or `Date.now(` (risk numbers must be
  reproducible). Same `JsRule` contract as framework rules; `check(ServedModule)`
  scans `module.lines()`. Other good candidates: "no floating‑point money
  formatting — route through a typed Decimal", "no `setInterval` in a model".
- **`RiskConformance.POLICY`** = `DefaultJsRulePolicy.INSTANCE.extendedWith(Map.of(RiskModuleType.RISK_MODEL, RISK_RULES))`
  where `RISK_RULES` = a `JsRuleSet` that **reuses framework global rules**
  (`NoCdnImportRule.INSTANCE`, `MaxEffectiveLinesRule.INSTANCE`) **plus** your own.
- Declare your calculators as `RISK_MODEL` in the crate (§6). The conformance studio
  then shows them grouped under `risk-model` with your rules in the (foldable) rule
  set — a live demo of the extension.

> Note: the composite policy does **not** auto‑add the framework "global" rules to an
> extension type's set — fold in the ones you want explicitly, as above.

### 7d. The conformance studio + build‑time export
Copy `DemoConformanceExport` → **`RiskConformanceExport`** (uses the *policy‑taking*
engine ctor so the report honors your extended policy) and
`DemoConformanceStudioServer` → **`RiskConformanceStudioServer`**
(`ConformanceStudioFixtures(new Umbrella.Solo<>(ConformanceStudio.INSTANCE), RiskConformance.TOP_LEVEL)`,
own port e.g. **8101**). Wire the export into the pom at `process-classes` (copy the
`exec-maven-plugin` execution from `homing-demo/pom.xml`) so `report.json` lands in
`target/classes/conformance-report/` for the studio to read. You do **not** need a
codec‑gen step unless your crate re‑packs `ReportCodecsModule` (it won't).

---

## 8. Domain design — a concrete starting set

A believable risk dashboard (all fabricated demo data — no real feeds):

**Widgets (DOM, `consumer` type):**
- 📉 **VaR gauge** — portfolio Value‑at‑Risk vs limit, colour by breach.
- 🔥 **Exposure heatmap** — asset‑class × region grid.
- 📊 **P&L / drawdown chart** — time series.
- 📋 **Positions table** — sortable, with limit columns.
- 🚨 **Limit breaches** — alert list; publishes "select breach" on the party bus.
- 🧪 **Stress scenarios** — pick a scenario → the other widgets refresh (party bus).
- 🔗 **Correlation matrix** — asset correlations.

**Headless (`risk-model` extension type):**
- `VarModel`, `ExposureModel`, `StressEngine` — pure‑logic calculators, no DOM,
  held to no‑DOM + your `deterministic-risk` rule.

**Cross‑widget flow:** a "Portfolio / scenario selected" event on the party bus →
a secretary re‑broadcasts → every widget re‑fetches for that selection. Copy the
`NavigatorSecretary` + party wiring from `ConformanceWorkspaceSpec` /
`ModuleTreeWidget`.

Consult the **`dataviz` skill** before writing any chart/gauge/heatmap code — it
gives an accessible, theme‑aware palette and mark specs (the studio is theme‑aware:
light/dark + the named themes you'll see in the top bar).

---

## 9. Gotchas — read before building (each of these bit us)

1. **One Crate per Maven module.** `OrphanCheck` scans the crate class's *entire*
   Maven‑module build output for concrete `EsModule`s and flags any the crate omits.
   Two partial crates in one module each flag the other's modules as orphans. So:
   one complete crate listing every served module. Use `CrateSeed.suggest(...)` to
   enumerate; re‑run it whenever you add a module.

2. **Baseline lives in `src/main/resources`, NOT `src/test/resources`.** The
   build‑time export runs on the **compile** classpath; a test‑only baseline loads
   as empty there → every pre‑existing finding becomes a NEW error. Put
   `risk-conformance-baseline.txt` under `src/main/resources` (the gate test still
   sees it — test classpath includes main resources).

3. **Never regenerate the baseline by piping console output to a file on Windows.**
   The console mangles UTF‑8 (e.g. an em‑dash `—` in a rule message becomes a lone
   `0x97` byte → invalid UTF‑8 → `Files.readAllLines` throws → the **whole** baseline
   loads empty → "0 baselined"). Instead, have a probe write fingerprints via Java
   (`Files.writeString(..., UTF_8)`) or edit the file with a UTF‑8‑safe editor. Verify
   with `LC_ALL=C grep -c $'\x97' <file>` (must be 0). Also delete any stale
   `target/**/…-baseline.txt` that could shadow the source copy.

4. **Companion resources move with the class's package.** A module's served
   `.js`/`.svg`/`.css` resource path mirrors its Java package
   (`resources/homing/js/<pkg>/Name.js`). If you move/rename a module's package, move
   its resources too, or the renderer throws `cannot find homing/js/…/Name.js` at
   export time. (We hit exactly this reorganizing a package.)

5. **Cross‑package `Exportable` markers must be `public`.** Modules reference each
   other's exported constants via nested `record createXxx() implements
   Exportable._Constant<…>` markers. Default (package‑private) only works
   same‑package; if module A (package p1) references module B's marker (package p2),
   B's marker must be `public`. Widen them — it's consistent with the framework's own
   cross‑package markers.

6. **`-Dexec.classpathScope=compile` is unnecessary for running a server** (default
   `runtime` already includes compile output + compile deps). It *is* correct for the
   `process-classes` export exec in the pom (so it never sees test resources) — and
   the pom template already pins it.

7. **Git line endings.** `LF will be replaced by CRLF` warnings on commit are benign
   (autocrlf) — ignore them.

8. **Commit hygiene (user preference):** do **not** add a `Co-Authored-By: Claude`
   trailer to commits. Stage explicit paths, not `git add -A`, if the repo has stray
   untracked files.

---

## 10. Starter checklist (in order)

1. Ensure the framework is available: in `homing-ssjs-core`, `mvn -o install -DskipTests`; confirm `LOCAL-SNAPSHOT` in `~/.m2` carries `homing-conformance-studio`.
2. Scaffold `homing-risk-dashboard` (single module, Java 21). Copy the pom shape + `homing.core.version=LOCAL-SNAPSHOT` from `homing-demo/pom.xml`. Add `homing-studio-starter` + `homing-conformance-studio` (compile).
3. Write one trivial widget (`VarGaugeWidget`) with inline `constructBodyJs`. Build.
4. Write `RiskDashboardWorkspaceSpec` + `RiskDashboardStudio`/`LandingCatalogue` + `RiskDashboardFixtures` + `RiskDashboardServer` (copies of the `Conformance*` templates). Run the server; open `/app?app=genericWorkspace&ws_kind=risk-dashboard`; add your widget via the `+` picker.
5. Add the rest of the widgets + headless models (§8). Wire the party bus for selection.
6. Generate `RiskDashboardCrate` with `CrateSeed`; declare `RISK_MODEL` modules explicitly; set `requires()`.
7. Add the policy extension (`RiskModuleType`, `DeterministicRiskRule`, `RiskConformance.POLICY`) — §7c.
8. Add `RiskConformance` + `RiskDashboardConformanceTest` (gate). Run; move surfaced findings into `src/main/resources/risk-conformance-baseline.txt` (UTF‑8 safe — §9.3).
9. Add `RiskConformanceExport` + the `process-classes` exec in the pom; add `RiskConformanceStudioServer`. Launch it (own port); verify `/conformance-report` shows your modules grouped by type incl. `risk-model`, and the crate tree nests by package.
10. Green the full reactor (`mvn -o test`). Commit (no Co‑Authored‑By trailer).

**Definition of done:** the dashboard workspace renders your widgets on
`GenericWorkspace`; the conformance gate passes; the conformance studio shows your
crate, your modules grouped by type (including the `risk-model` extension type with
its rule set), and the package‑nested crate tree.

---

## 11. Where things live (quick reference)

- Framework source: `Q:\repos\java\homing\homing-ssjs-core` (build/install to get `LOCAL-SNAPSHOT`).
- Worked downstream example (**your primary reference**): `Q:\repos\java\homing\homing-doc-plus-demo`.
- Conformance studio module (reused as a dependency): `homing-ssjs-core/homing-conformance-studio`.
- The open policy + rules: `homing-ssjs-core/homing-conformance-rules` (`DefaultJsRulePolicy`, `StandardJsModuleType`, `JsRule`, `JsRuleSet`, `Baseline`, `FindingGrader`, `CrateSeed`), `homing-core` (`JsModuleType`, `Crate`, `CrateEntry`).
- The engine: `homing-ssjs-core/homing-conformance-engine` (`ConformanceEngine`, `ServedModuleRenderer`, `ModuleClassifier`).

## 12. When in doubt

- **Read the reference file, don't guess an API.** Signatures in this doc are a map,
  not the territory; the `homing-demo` source is the territory.
- Skills worth invoking: **`dataviz`** (any chart/gauge), and **`claude-code-guide`**
  only for Claude‑Code/SDK questions (not framework questions).
- If a conformance finding is a legitimate, intentional exception, add a documented
  `Allowance` (module class + rule id + reason) to `RiskConformance.ALLOWANCES` —
  don't weaken a rule.
