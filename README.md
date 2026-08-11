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

| Module | Persona / role | Workspace kind |
|---|---|---|
| `fin-dash-core` | shared substrate: UI kit, grid, smile chart, desk secretary, headless models, demo data, RFC 0044 policy extension | — |
| `fin-dash-trader` | Trader / market-maker — **Pricer W1, Surface Manager W2, Risk Blotter W4 built** | `trader` |
| `fin-dash-etrading` | e-Trading supervisor (W3) | `etrading` |
| `fin-dash-sales` | Sales & structuring | `sales` |
| `fin-dash-marketdata` | Market data operations (W5 pattern) | `market-data` |
| `fin-dash-quant` | Quant / methodology | `quant` |
| `fin-dash-risk` | Market risk manager | `risk` |
| `fin-dash-governance` | Model validation (W6) | `governance` |
| `fin-dash-middleoffice` | Middle office | `middle-office` |
| `fin-dash-ipv` | Product control / IPV | `ipv` |
| `fin-dash-platform` | Platform operations (SRE, W5) | `platform` |
| `fin-dash-audit` | Audit & compliance | `audit` |
| `fin-dash-summary` | Desk head / management | `summary` |
| `fin-dash-studio` | the umbrella: landing (one tile per persona), servers, conformance config/export/gate | — |

Persona modules not yet built out ship their **home card** (mission, ring writes,
cadence, planned screens) so every workspace exists from day one; screens land
persona by persona.

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

Build the remaining persona screens (KT.md §5–§8, study §4–§13): quoting console
W3, feed & quality console, change console W6, platform console W5, risk views —
each in its own module, widgets declared in that module's crate (the gate fails
otherwise, by design), data feeds added to `FinDashFixtures.harnessGetActions()`.
