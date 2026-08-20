# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

`homing-demo-fin-dash` is an FX-options desk demo on the homing framework's
`GenericWorkspace` shell, with RFC 0044 conformance wired in end-to-end — a
**multi-module reactor with one Maven module (= one Crate = one workspace kind) per
human actor** in the UI study: `fin-dash-core` (shared kit/models/data + the policy
extension), twelve persona modules (`fin-dash-trader`, `-etrading`, `-sales`,
`-marketdata`, `-quant`, `-risk`, `-governance`, `-middleoffice`, `-ipv`,
`-platform`, `-audit`, `-summary`), and `fin-dash-studio` (umbrella landing +
servers + conformance config/export/gate). The trader workspace (Pricer W1,
Surface Manager W2, Risk Blotter W4, desk party bus) is built; other personas
ship a home card until their screens land.

Three documents drive the work, in this order:

- **[KT.md](KT.md)** — the full knowledge-transfer guide (§3–§8 are build recipes, §9 is the
  gotcha list). Read §9 before touching build config.
- **[docs/fx-options-ui-study.html](docs/fx-options-ui-study.html)** — the build target:
  screens/wireframes W1–W6 (Pricer, Surface manager, Risk blotter, Quoting console, Feed &
  quality console, Change console) and cross-cutting principles P1–P5.
- **[docs/fx-options-engine-architecture.html](docs/fx-options-engine-architecture.html)** —
  domain context: data model, Greeks, two-speed risk, quality flags.

## Commands

Prerequisite — the homing framework must be installed as `LOCAL-SNAPSHOT` in `~/.m2`
(it carries the RFC 0044 conformance studio this project depends on):

```bash
cd ../../homing-ssjs-core && mvn -o install -DskipTests
```

Build + gate. `mvn -o install -DskipTests` first whenever you'll run a single module with
`-pl` (sibling jars resolve from `~/.m2`, not the reactor). The `process-classes` phase of
`fin-dash-studio` exports the conformance report; `mvn -o test` runs the gate:

```bash
mvn -o install -DskipTests
mvn -o test
```

Run one test method:

```bash
mvn -o test -pl fin-dash-studio -Dtest=FinDashConformanceTest#servedModulesAreConformant
```

Run the desk (landing `/` with one tile per persona; workspaces at
`/app?app=genericWorkspace&ws_kind=trader|etrading|sales|market-data|quant|risk|governance|middle-office|ipv|platform|audit|summary`).
Port 8100, override with `-Ddashboard.port=`:

```bash
mvn -o -pl fin-dash-studio exec:java -Dexec.mainClass=hue.captains.singapura.js.homing.findash.studio.FinDashServer
```

Run the conformance studio (crate tree over all 13 crates, modules by type, report).
Port 8101, override with `-Dconformance.port=`:

```bash
mvn -o -pl fin-dash-studio exec:java -Dexec.mainClass=hue.captains.singapura.js.homing.findash.studio.conformance.FinDashConformanceStudioServer
```

`-Dconformance.allowPreExisting=false` on the test run promotes baselined warnings back to
errors.

## Architecture

**Front-end is emitted Java, not authored JS.** Every UI unit is an `EsModule<T>` — a Java
object that emits JavaScript. `WorkspaceWidget<Params, Self>` subclasses render DOM (the JS
body is `constructBodyJs()`, returning `{root, setActive, partyDeregister}`); headless
modules (risk math) emit JS with no DOM at all. **Conformance inspects the served artifact —
the emitted JS — not your Java.**

**The shell is reused, not written.** `GenericWorkspace` gives split panes, tabs, layout
persistence, and the party bus for free. Each persona module supplies one `WorkspaceSpec`
(its kind, e.g. `"trader"`) listing widget entries and party declarations; `FinDashFixtures`
registers them all. The trader spec declares two parties: the generic `navParty` and the
fin-dash `deskParty` (`DeskSecretaryModule` — `InstrumentSelected` → rebroadcast
`InstrumentChanged`; that's how blotter clicks drive the pricer and surface manager).

**Serving is crate-gated, on both sides.** Every Maven module has exactly one Crate
(`FinDashCoreCrate`, `TraderCrate`, …) declaring every served module it ships;
`FinDashConformance.TOP_LEVEL` lists them all. That list is load-bearing three times over:

1. `FinDashFixtures.servableModuleClasses()` turns the crate closure into the runtime
   allow-list — `/module` returns 404 for anything outside it, so a widget cannot reach the
   browser while escaping conformance.
2. `CrateCoverage` (gate test, one anchor per module) fails the build if a served module in
   any fin-dash Maven module is in no crate.
3. `CrateConformance` orphan/illegal-import checks run against every crate.

So **adding a widget or model without adding it to its module's crate fails the build — by
design.** Keep persona-crate `requires()` minimal: `FinDashCoreCrate` (the kit import) plus
whatever the gate names as an illegal import — not the whole framework set.

**Conformance config is shared by the gate and the studio.** `FinDashConformance` holds
`TOP_LEVEL`, `POLICY`, `ALLOWANCES`, `baseline()`, and `grader(...)` so
`FinDashConformanceTest` (build-fail gate) and `FinDashConformanceExport` (report written to
`target/classes/conformance-report/` at `process-classes`) grade identically. Never grade
through a differently-configured engine.

**The policy is extended, not forked.** `RiskModuleType.RISK_MODEL` is a downstream
`JsModuleType`; `DeterministicRiskRule` (`Math.random(` / `Date.now(` banned — risk numbers
must be replayable) is a downstream `JsRule`; `FinDashConformance.POLICY` is
`DefaultJsRulePolicy.INSTANCE.extendedWith(RISK_MODEL → rule set)`. Note the composite policy
does **not** auto-inherit framework global rules into an extension type's set — the globals
(`NoCdnImportRule`, `MaxEffectiveLinesRule`, `NoDomAccessRule`) are folded in explicitly.

### Adding a widget to a persona module (touches five files)

1. `<persona>/XxxWidget.java` — extend `WorkspaceWidget`, singleton `INSTANCE` + private
   `record construct()`; import shared primitives via `bodyImports()`
   (`FinDashKitModule.fdk`, `FinDashGridModule.fdGrid`, `SmileChartModule.smileChart` —
   see `PricerWidget` / `RiskBlotterWidget` for the full pattern incl. party wiring).
2. The persona's `XxxWorkspaceSpec.widgetEntries()` — `WidgetEntry.of(..., WidgetLabel)`
   with icon/group.
3. `fin-dash-studio` `FinDashFixtures.harnessGetActions()` — the widget's data endpoint
   (e.g. `/fx/price`); the GetAction class lives in the persona module
   (`trader/data/PriceGetAction` is the template), demo data in `core/data/DeskData`.
4. The persona's `XxxCrate.entries()` — declare it (`CrateEntry.of(INSTANCE)`, or with an
   explicit type: `StandardJsModuleType.PRIMITIVE`/`SECRETARY`, `RiskModuleType.RISK_MODEL`
   for headless models; the declaration wins over structural inference).
5. `XxxCrate.requires()` — add any crate the new module's served JS imports from, or the
   crate conformance check reports "illegal import".

Risk calculators go headless (`core/model/`, `RISK_MODEL`), never in a widget: UIs are
consumers, not calculators (UI study P5). Widgets must honour P1 (a visible lineage stamp
via `fdk.stamp` — slice / epoch / model) and P2 (loud degraded state via `fdk.chip` — icon +
label, never color alone). Consult the dataviz skill before new chart/meter primitives; the
kit's tokens are the validated reference palette.

## Project-specific pitfalls

- **One Crate per Maven module.** `OrphanCheck` scans the crate class's entire build output;
  two partial crates in one module each flag the other's modules as orphans.
- **The baseline lives in `fin-dash-studio/src/main/resources/risk-conformance-baseline.txt`,
  not `src/test/resources`.** The `process-classes` export runs on the compile classpath — a
  test-only baseline loads empty there and every pre-existing finding becomes a new error.
- **`mvn -o -pl fin-dash-studio exec:java` needs `mvn -o install -DskipTests` first** —
  single-module runs resolve sibling jars from `~/.m2`, not the reactor.
- **`textContent = ''` is a conformance error** (`no-dom-destruction` — wholesale wipe).
  Clear owned DOM with a `removeChild` loop; set status lines to a real message instead.
- **Never regenerate the baseline by piping console output to a file on Windows.** The console
  mangles UTF-8 (an em-dash in a rule message becomes a lone `0x97`) → `Files.readAllLines`
  throws → the whole baseline loads empty. Write it from Java with
  `Files.writeString(..., UTF_8)` or a UTF-8-safe editor; verify with
  `LC_ALL=C grep -c $'\x97' <file>` (must be 0), and delete stale copies under `target/`.
- **A module's companion resources mirror its Java package**:
  `src/main/resources/homing/js/<package path>/Name.js`. Move the resource whenever you move
  the class, or the renderer throws at export time.
- **Cross-package `Exportable` marker records must be `public`** — package-private only works
  same-package.
- `-Dexec.classpathScope=compile` is unnecessary for running a server (default `runtime`
  already covers it); it *is* correct for the `process-classes` export exec, which the pom
  already pins.
- A legitimate exception belongs in `FinDashConformance.ALLOWANCES` (module class + rule id +
  reason) — do not weaken a rule.

## Conventions

- **Commits: no `Co-Authored-By: Claude` trailer** (stated user preference, KT.md §9.8). Stage
  explicit paths rather than `git add -A`.
- `LF will be replaced by CRLF` warnings on commit are benign.
- Consult the **`dataviz` skill** before writing any chart, gauge, or heatmap code — the studio
  is theme-aware (light/dark plus named themes).

## Reference implementations

Signatures in KT.md are a map, not the territory — read the source when unsure:

- `Q:\repos\java\homing\homing-doc-plus-demo` (module `homing-demo`) — the complete working
  downstream this scaffold was copied from; primary reference for widgets, the crate model,
  the gate, and policy extension.
- `Q:\repos\java\homing\homing-ssjs-core` — the framework. `homing-conformance-studio`
  (`ConformanceWorkspaceSpec`, `ConformanceStudioFixtures`, `ModuleTreeWidget`) is the direct
  template for this repo's studio classes; `homing-conformance-rules` holds
  `DefaultJsRulePolicy`, `JsRule`, `Baseline`, `FindingGrader`, `CrateSeed`.
