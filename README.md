# homing-demo-fin-dash

Demo project for building financial dashboards with the homing workspace — an
**FX Options Desk** on the homing `GenericWorkspace`, with RFC 0044 conformance,
structured as **one Maven module (one Crate, one workspace kind) per human actor**
in the UI Requirements Study.

> Build from **[KT.md](KT.md)** (knowledge-transfer guide) and the design docs in
> [`docs/`](docs/):
> - [`docs/fx-options-ui-study.html`](docs/fx-options-ui-study.html) — the UI Requirements Study (**the build target**: personas, screens, wireframes W1–W6, principles P1–P5).
> - [`docs/fx-options-engine-architecture.html`](docs/fx-options-engine-architecture.html) — the pricing & risk engine architecture (domain context).

## Modules

| Module | Persona / role | Workspace kind | Screens |
|---|---|---|---|
| `fin-dash-core` | shared substrate: UI kit, grid, smile chart, desk secretary, headless models, demo data, RFC 0044 policy extension | — | — |
| `fin-dash-trader` | Trader / market-maker | `trader` | Pricer (W1), Surface Manager (W2), Risk Blotter (W4), Barrier Watch, Expiry/Pins |
| `fin-dash-etrading` | e-Trading supervisor | `etrading` | Quoting Console (W3), RFQ Tape |
| `fin-dash-sales` | Sales & structuring | `sales` | Client Pricer (margin on top, indicative/firm) |
| `fin-dash-marketdata` | Market data operations | `market-data` | Feed & Quality (W5 pattern), Override Inventory |
| `fin-dash-quant` | Quant / methodology | `quant` | Calibration Lab |
| `fin-dash-risk` | Market risk manager | `risk` | Risk Views (limits), Scenario Workbench, Concentrations |
| `fin-dash-governance` | Model validation | `governance` | Change Console (W6), Model Inventory |
| `fin-dash-middleoffice` | Middle office | `middle-office` | Lifecycle Workstation + Breaks |
| `fin-dash-ipv` | Product control / IPV | `ipv` | P&L Explain / IPV workbench / marks sign-off |
| `fin-dash-platform` | Platform operations (SRE) | `platform` | Platform Console (W5), Epoch Flow |
| `fin-dash-audit` | Audit & compliance | `audit` | Audit Explorer (cross-journal, time-travel) |
| `fin-dash-summary` | Desk head / management | `summary` | Summary Dashboard (every tile a projection) |
| `fin-dash-studio` | the umbrella: landing (one tile per persona), servers, conformance config/export/gate | — | — |

Every persona also keeps its **home card** (mission, ring writes, cadence,
planned screens). One demo story runs through all screens: broker C's
jump-filtered volBF quarantine degrades the USDJPY surface, stretches the reval
budget, auto-widens quoting, ages a Ring-3 override, and surfaces in risk,
audit, and the management rollup.

## Prerequisite

The homing framework as `LOCAL-SNAPSHOT` in `~/.m2` (carries the RFC 0044
conformance studio):

```bash
cd ../../homing-ssjs-core && mvn -o install -DskipTests
```

## Build & test

```bash
mvn -o install -DskipTests   # build + install (single-module `-pl` runs need this)
mvn -o test                  # conformance gate: crate integrity + coverage + rules
```

The `process-classes` phase of `fin-dash-studio` exports the conformance report;
the test phase runs the gate (`FinDashConformanceTest`).

## Run

The desk (landing with every persona workspace) — port **8100**
(`-Ddashboard.port=`):

```bash
mvn -o -pl fin-dash-studio exec:java -Dexec.mainClass=hue.captains.singapura.js.homing.findash.studio.FinDashServer
```

Open `/` and pick a persona, or go straight to a workspace:
`/app?app=genericWorkspace&ws_kind=trader` (use the `+` picker to add widgets;
in the trader workspace, click a blotter row and watch the pricer + surface
manager follow the selection over the desk party bus).

The conformance studio (crate tree, modules by type, report; `VarModel` under
the `risk-model` extension type) — port **8101** (`-Dconformance.port=`):

```bash
mvn -o -pl fin-dash-studio exec:java -Dexec.mainClass=hue.captains.singapura.js.homing.findash.studio.conformance.FinDashConformanceStudioServer
```

## Next steps

Every persona has its primary screens; from here the demo deepens rather than
widens: real ticking data (the demo feeds are static snapshots), the polyglot
codec for typed decode, per-widget as-of time travel (P4), theming beyond the
light palette, and the replay lab / champion-challenger screens (study §7).
New widgets follow the recipe in CLAUDE.md — widget + spec entry + crate entry
+ feed in `FinDashFixtures.harnessGetActions()` (the gate fails otherwise, by
design).
