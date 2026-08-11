# homing-demo-fin-dash

Demo project for building financial dashboards with the homing workspace — a
**Financial Risk-Management Dashboard** for the FX options stack, served on the
homing `GenericWorkspace` with RFC 0044 conformance.

> **This is a scaffold.** It compiles, serves, and passes conformance today, but
> ships one placeholder widget. Build the real UI from here — start with
> **[KT.md](KT.md)** (the full knowledge-transfer guide), then the companion design
> docs in [`docs/`](docs/):
> - [`docs/fx-options-ui-study.html`](docs/fx-options-ui-study.html) — the UI Requirements Study (**build from this**: screens, personas, wireframes W1–W6, principles P1–P5).
> - [`docs/fx-options-engine-architecture.html`](docs/fx-options-engine-architecture.html) — the pricing & risk engine architecture (domain context: data model, Greeks, two-speed risk, quality flags).

## Prerequisite

The homing framework must be available as `LOCAL-SNAPSHOT` in your `~/.m2` (it
carries the RFC 0044 conformance-studio this project depends on):

```bash
cd ../../homing-ssjs-core && mvn -o install -DskipTests
```

Switch `homing.core.version` in `pom.xml` to a published release once one ships RFC 0044.

## Build & test

```bash
mvn -o test
```

The `process-classes` phase exports the conformance report; the test phase runs
the gate (`FinDashConformanceTest`): crate integrity + coverage + rule conformance.

## Run

Dashboard (the product) — landing at `/`, workspace at
`/app?app=genericWorkspace&ws_kind=risk-dashboard` (open the `+` picker to add widgets):

```bash
mvn -o exec:java -Dexec.mainClass=hue.captains.singapura.js.homing.findash.studio.RiskDashboardServer
```

Conformance studio (governs the modules) — browse the crate, modules by type,
and the report; `VarModel` shows under the `risk-model` type with its rule set:

```bash
mvn -o exec:java -Dexec.mainClass=hue.captains.singapura.js.homing.findash.conformance.FinDashConformanceStudioServer
```

Ports default to **8100** (dashboard) and **8101** (conformance studio); override
with `-Ddashboard.port=` / `-Dconformance.port=`.

## What's wired (the scaffold)

| Area | Class(es) |
|---|---|
| Dashboard studio | `studio/RiskDashboard{Studio,LandingCatalogue,WorkspaceSpec,Fixtures,Server}` |
| Placeholder widget | `widget/RiskDashboardPlaceholderWidget` (CONSUMER; shows the P1 lineage + P2 status patterns) |
| Headless risk model | `model/VarModel` (+ `VarModel.js`) — declared `RISK_MODEL` |
| Policy extension | `conformance/RiskModuleType` (`risk-model` type) + `conformance/DeterministicRiskRule` |
| Conformance config | `conformance/FinDashConformance` (extended `POLICY`, crate closure, baseline, grader) |
| Crate | `conformance/FinDashCrate` (every served module — one crate per Maven module) |
| Build-time export | `conformance/FinDashConformanceExport` (wired at `process-classes`) |
| Conformance studio | `conformance/FinDashConformanceStudioServer` |
| Gate test | `test/.../FinDashConformanceTest` (integrity + coverage + rules) |
| Baseline | `src/main/resources/risk-conformance-baseline.txt` (empty — scaffold is clean) |

The serving layer enforces the **runtime crate-gate** (`RiskDashboardFixtures.servableModuleClasses()`):
a module not in a registered crate is refused at `/module`, so nothing reaches the
browser while escaping conformance.

## Next steps

See **[KT.md](KT.md)** §5–§8: add the trader widgets (Pricer W1, Surface Manager
W2, Risk Blotter W4) to `RiskDashboardWorkspaceSpec`, add their data GET actions
to `RiskDashboardFixtures`, add headless `RISK_MODEL` models, and declare every
new served module in `FinDashCrate` (the gate fails otherwise — by design).
