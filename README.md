# homing-demo-fin-dash

Demo project for building financial dashboards with the homing workspace — an
**FX Options Desk** on the homing `GenericWorkspace`, with RFC 0044 conformance,
structured as **one Maven module (one Crate, one workspace kind) per human actor**
in the UI Requirements Study.

> Build from **[KT.md](KT.md)** (knowledge-transfer guide) and the docs in
> [`docs/`](docs/):
> - [`docs/fx-options-ui-study.html`](docs/fx-options-ui-study.html) — the UI Requirements Study (**the build target**: personas, screens, wireframes W1–W6, principles P1–P5).
> - [`docs/fx-options-engine-architecture.html`](docs/fx-options-engine-architecture.html) — the pricing & risk engine architecture (domain context).
> - [`docs/demo-data-requirements.md`](docs/demo-data-requirements.md) — requirements for the consolidated, UI-agnostic demo dataset (the substrate for cross-workspace widget connection).
> - [`docs/adding-a-theme.md`](docs/adding-a-theme.md) — how to add a custom studio theme (worked example: a Bloomberg-terminal look), incl. the downstream wiring the framework skill omits.
> - [`docs/upstream-blockers.md`](docs/upstream-blockers.md) · [`docs/defect-app-refs-ambiguous.md`](docs/defect-app-refs-ambiguous.md) — framework issues this demo found, with evidence and suggested fixes.
> - [`docs/backlog.md`](docs/backlog.md) · [`docs/debt-plan.md`](docs/debt-plan.md) — what this demo owes itself: the JavaScript-in-Java tax and a reference-implementation debt register ranked by whether a practice would be copied; and the plan paying it down, step by step, with status.
> - [`docs/relation-grid-risk-ladder.md`](docs/relation-grid-risk-ladder.md) · [`docs/virtualization-pseudo-requirement.md`](docs/virtualization-pseudo-requirement.md) · [`docs/react-rewrite-cost.md`](docs/react-rewrite-cost.md) · [`docs/homologous-relations.md`](docs/homologous-relations.md) — the studies: building the risk ladder as RFC 0050's first consumer, why virtualization is a pseudo-requirement, what the desk would cost cold-start in React, and the grid facility the ladder's 98 structural lines argue for.
>
> `docs/` is the single source: the studio serves these files from the jar via a
> build-time copy (no second committed copy), under **Documentation** on the landing.

## Quick start

Needs **JDK 21+** and **Maven 3.9+**. Nothing else — the homing framework
resolves from Maven Central.

```bash
mvn clean install
```

```bash
mvn -pl fin-dash-studio exec:java -Dexec.mainClass=hue.captains.singapura.js.homing.findash.studio.FinDashServer
```

Then open <http://localhost:8100/>.

## Modules

| Module | Persona / role | Workspace kind | Screens |
|---|---|---|---|
| `fin-dash-core` | shared substrate: UI kit, grid, smile chart, desk secretary, headless models, demo data, RFC 0044 policy extension | — | — |
| `fin-dash-data` | the consolidated demo dataset plus its ontology markers — the gate that keeps types honest across workspaces | — | — |
| `fin-dash-book` | shared anchors: Portfolio view + Trade Blotter, listed in five workspaces | — | Portfolio, Trade Blotter |
| `fin-dash-viz` | vendored three.js, served verbatim as a `BUNDLED_EXTERNAL` module — the rendering-leaf pattern | — | Vol Surface 3D |
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
| `fin-dash-ontology` | for the builders: the type catalogue and which widgets require each type | `data-ontology` | Data Types |
| `fin-dash-studio` | the umbrella: landing, workspace intros, servers, conformance config/export/gate | — | — |

Every persona also keeps its **home card** (mission, ring writes, cadence,
planned screens). One demo story runs through all screens: broker C's
jump-filtered volBF quarantine degrades the USDJPY surface, stretches the reval
budget, auto-widens quoting, ages a Ring-3 override, and surfaces in risk,
audit, and the management rollup.

## Build & test

```bash
mvn clean install            # build + install (single-module `-pl` runs need this)
mvn test                     # conformance gate: crate integrity + coverage + rules
```

The `process-classes` phase of `fin-dash-studio` exports the conformance report;
the test phase runs the gate (`FinDashConformanceTest`).

Add `-o` (offline) to any of these once the dependencies are cached — it is
faster and the build needs no network after the first run. The first build on a
fresh clone must be online.

The framework version is the `homing.core.version` property in the root `pom.xml`.
Point it at `LOCAL-SNAPSHOT` to build against a framework working tree instead of
the release — needed when validating an unreleased core change downstream, and it
requires `mvn install` in `homing-ssjs-core` first.

## Run

The desk — port **8100** (`-Ddashboard.port=`):

```bash
mvn -pl fin-dash-studio exec:java -Dexec.mainClass=hue.captains.singapura.js.homing.findash.studio.FinDashServer
```

The landing has **Documentation** and a single **Workspace** door. Start with
*Documentation → Workspace Intros*: one page per participant, saying what that
desk owns, what its screens are for, and what to try. Or go straight in:
`/app?app=genericWorkspace&ws_kind=trader`.

Driving a workspace:

- Panes start empty — the **`+`** picker adds widgets, and only the ones that
  persona's spec declares (a widget outside a registered crate is refused by the
  server).
- **Click** a pane to select it; **double-click** to *enter* it. Only entering
  gives the widget the keyboard, so travel keys never arm on a stray click.
- **Switch persona in place** by clicking the workspace title — the type list is
  in the control panel. That is why there is one workspace door rather than
  thirteen: the kind is app state, not navigation.
- In the middle-office workspace, enter the **Trade Blotter** and walk it with
  **↑/↓** — the Lifecycle view follows every step. In the trader workspace, click
  a blotter row and watch the pricer and surface manager follow over the desk
  party bus.

The conformance studio (crate tree, modules by type, report; `VarModel` under
the `risk-model` extension type) — port **8101** (`-Dconformance.port=`):

```bash
mvn -pl fin-dash-studio exec:java -Dexec.mainClass=hue.captains.singapura.js.homing.findash.studio.conformance.FinDashConformanceStudioServer
```

## Next steps

Every persona has its primary screens; from here the demo deepens rather than
widens: real ticking data (the demo feeds are static snapshots), the polyglot
codec for typed decode, per-widget as-of time travel (P4), theming beyond the
light palette, and the replay lab / champion-challenger screens (study §7).
New widgets follow the recipe in CLAUDE.md — widget + spec entry + crate entry
+ feed in `FinDashFixtures.harnessGetActions()` (the gate fails otherwise, by
design).
