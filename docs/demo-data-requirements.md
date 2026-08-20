# Demo Data Requirements — the Consolidated Desk Dataset

**Status:** agreed 2026-08-11 (scoping decisions §8) · **Owner:** fin-dash ·
**Supersedes:** the per-feed fragments in `DeskData` (which stay until the new
set lands, then feeds re-point).

## 0. Why

The headline capability this demo exists to show is **connection**: select a
thing in any widget — a portfolio node, a position, a trade, a pair, an epoch,
a model — and every other widget, *in any workspace*, resolves that same thing
from its own angle. That only works if there is **one dataset** underneath:
a closed graph of identified entities, where every feed is a projection and
every join key is shared.

Today's `DeskData` fails this in four ways the requirements below are written
to fix:

1. **Fragmented** — each feed has its own records; coherence (the USDJPY
   degradation story) is hand-synchronised prose repeated in ~6 places.
2. **Presentation baked in** — formatted strings (`"€40M"`, `"0.06→0.12"`),
   display severities, and UI copy (`"budget 92% — wave queued"`) live in the
   data. A non-widget consumer (test, export, future API) can't use it.
3. **Authored aggregates** — blotter bucket rows, desk totals, and counts are
   hand-written beside the positions they should derive from; nothing enforces
   they agree.
4. **Stringly lineage** — stamps like `"C204 · S513/87 · VV-2.3"` are display
   strings, so "everything priced off this epoch" (the P1 reverse query) can't
   actually be evaluated.

## 1. Scope

- **In scope:** the domain dataset (reference data, market state, book, journals,
  governance artifacts), its identity/join contract, derivation rules, the
  narrative it must encode, packaging, and integrity tests.
- **Out of scope (for now):** live ticking / streaming mutation, multiple as-of
  snapshots (time travel), real market data, persistence. The design must not
  *preclude* these — see R8 and §7 — but the first deliverable is one static,
  deterministic snapshot.

## 2. Core requirements

**R1 — Single source of truth.** Every fact appears exactly once. Anything
derivable is **derived, never authored**: bucket rows, totals, utilizations,
counts, concentration views, expiry clusters, the audit tape. If two screens
can disagree, the data model is wrong (this is P5 applied to the dataset).

**R2 — Identity and referential integrity.** Every entity has a stable,
human-scrutable id (`pf-eur-exo`, `T-4471`, `S513.87`, `VV-2.3`). Every
cross-reference is by id. No dangling references — enforced by a build-time
integrity test, same spirit as the crate gate.

**R3 — UI-agnostic.**
- Quantities are typed values: `{amount, currency}` pairs, numeric vols/greeks,
  ISO dates/instants with explicit zones, fractions in [0,1]. Never formatted
  strings.
- States are **domain enums** (`QuoteState.QUARANTINED`, `SurfaceStatus.STALE`,
  `QuotingState.AUTO_WIDENED`), never display words like `"warn"`/`"serious"`.
  Severity-for-chips is a UI mapping owned by the kit.
- No UI copy in data. Human-readable *reasons* are allowed where the domain
  genuinely records them (a reason is journal content, not presentation), but
  layout hints, emoji, arrows, and composed sentences are not.
- The dataset must be consumable by a non-UI client (a test, a JSON export, a
  future replay harness) without loss.

**R4 — Deterministic and self-contained.** No clock, no randomness, no
external references. The scenario is pinned to one as-of instant
(`2026-08-11T06:32:07Z` = 14:32:07 SGT). All timestamps are explicit and
relative to that day (journal history may reach back days). Everything any
feed serves is reachable from the dataset root.

**R5 — Lineage as structure (P1).** Every priced/derived figure carries
structured lineage: `{sliceId, surfaceEpochRef {pair, epoch, subVersion},
curveSetId, modelRef {id, version}}`. Display stamps are rendered UI-side.
Consequence: the reverse queries become real joins — *everything priced by
model X vY*, *everything derived from slice C204*, *every consumer of epoch
S498* must be answerable from the data alone.

**R6 — The join contract (the connection feature).** The following selection
keys are first-class, and every consumer-facing projection must be filterable
by (or carry enough refs to test membership against) each applicable key:

| Key | Resolves to |
|---|---|
| `pairId` | surface, quotes, quoting state, positions, trades, barriers, epochs |
| `portfolioNodeId` (any level) | leaf-portfolio set → positions, trades, limits, P&L |
| `positionId` | its trade(s), greeks, barrier features, lifecycle items |
| `tradeId` | position, amendments, breaks, RFQ origin, audit entries |
| `sliceId` / `surfaceEpochRef` | everything priced/quoted with it |
| `modelRef` | trades/positions priced, selection-matrix rows, change packages |
| `actorId` | every journal entry by that actor |
| `scenarioId` | scenario definition + results |
| `eventId` (calendar) | event vols, vega concentration, expiry clusters |
| `sourceId` (vendor/broker) | quotes, quality events, composite memberships |

Party-bus messages carry **ids only** (plus resolved leaf-id sets for
hierarchy selections, per the established pattern) — never display text.

**R7 — One narrative, expressed as state + events.** The dataset encodes one
coherent trading day whose causal chains emerge from entity states and journal
entries — not from annotation strings. Required chains (each must be fully
traversable by joins):

1. **Degradation chain:** broker-C volBF quote → `QUARANTINED(jumpFilter)` →
   USDJPY surface epoch `STALE(waitingOn: quoteId)` → reval budget elevated on
   USDJPY positions → quoting `AUTO_WIDENED(rule QW-3)` → an RFQ `LOST` priced
   off the stale epoch → change package #414 proposing the tolerance fix.
2. **Barrier chain:** a no-touch position within its monitoring band → budget
   tightening on that position → barrier-density concentration → lifecycle
   watch item — all referencing the same position/instrument.
3. **Amendment chain:** confirm-mismatch break → journaled amendment on the
   trade (four-eyes) → re-priced position → P&L attribution `amendments` term.
4. **Promotion chain:** challenger model → validation doc → change package
   #412 (semantic diff referencing SelectionMatrix rows by id, replay impact
   referencing position ids) → approval journal entries.
5. **Pin/expiry chain:** expiry-day positions → cut-time cluster → pin
   candidate near spot → risk pin-exposure limit row.

Additionally: **every persona has a reason to look** — at least one journal
entry or state in each persona's domain participates in some chain.

**R8 — Journal-first.** Every action that happened *is a journal entry*:
`{entryId, journalId, instant, actorId, kind, payload refs, reason?, approval
refs?}`. Trades, amendments, overrides (with expiry), quoting state changes
(automatic AND manual, same journal), quarantines/releases, package approvals,
marks sign-off, ops actions. The audit tape is `union(all journals)` —
derived, not authored. Current state must be consistent with the journal
history (the integrity test replays simple invariants). This is what later
makes time travel (P4) and ticking a data extension, not a redesign.

## 3. The ontology — what exists, and in what mode of being

The dataset is declared with **jOntology** (`hue.captains.singapura.tao.ontology`),
the marker-interface library the homing framework itself is written against.
The markers are not decoration: they are the machine-readable statement of each
object's nature, and the enforcer checks them (§3.6).

### 3.1 The central claim: nothing here is `Mutable`

The dataset is pinned to one as-of (R4) and journal-first (R8). Therefore
**no object in `fin-dash-data` is `Mutable`** — not one. Every object is an
immutable fact. What looks like mutation in the domain is *another fact*:

| Domain "change" | What actually exists |
|---|---|
| a trade is amended | the `TradeBooked` fact **and** a later `TradeAmended` fact |
| an override expires | an `OverrideRecord` fact with an expiry instant; "live" is a predicate over the as-of |
| a quote is quarantined | a `QuoteQuarantined` journal fact; the quote's state is read from the fold |
| quoting auto-widens | a `QuotingStateChanged` fact carrying the rule that fired |
| a breach is acknowledged | a `BreachAcknowledged` fact; "unacknowledged" is the absence of one |

**Current state is a derivation, never a stored object.** This is what makes
time travel (P4) a change of *which facts you fold*, not a change of type —
and what makes the whole dataset trivially cacheable and shareable.

### 3.2 The modes of being in use

| Marker | Enforced contract | Used here for |
|---|---|---|
| `ValueObject` | ≥1 final field · overrides `equals`/`hashCode` · transitively immutable | **every fact, identity, and quantity** — the entire dataset |
| `FunctionalObject` | immutable; may hold immutable configuration | the dataset root — functions parameterised by the facts they close over |
| `StatelessFunctionalObject` | zero instance fields; pure functions | derivations, queries, pricing, the bulk generator |
| `Stateless` | zero instance fields | (subsumed by the above here) |
| `Mutable` | — | **forbidden in this module** (gate rule) |

Java records satisfy `ValueObject` for free (final fields, generated
`equals`/`hashCode`). Two consequences of the enforced contract shape the
design, and both are wanted:

- **No static methods on immutable types.** Behaviour cannot hide in
  `static` helpers; it must live on a `FunctionalObject` singleton. This is
  precisely the discipline today's `DeskData` violates (`parse`, `price`,
  `reverseQuery`, `round2` are all statics) and the reason its behaviour and
  its facts are tangled.
- **Transitive immutability.** Field types must themselves be immutable, so
  quantities and ids may not be bare `double`/`String` at boundaries where a
  unit or a referent is meant (see §3.3 strata 0–1).

### 3.3 The strata — the type catalogue

Each stratum may reference only strata below it. Every type is a
`ValueObject` unless marked otherwise.

**Stratum 0 — Identity.** `PairId`, `TenorId`, `PortfolioNodeId`,
`PositionId`, `TradeId`, `InstrumentId`, `RfqId`, `JournalEntryId`, `SliceId`,
`CurveSetId`, `EpochRef` (pair + epoch + subVersion), `ModelRef` (id +
version), `ActorId`, `SourceId`, `ScenarioId`, `EventId`, `RuleId`,
`PackageId`, `LimitId`, `BreakId`.
*Rule:* an identity is a **value**, never a bare `String` at a boundary. This
is what makes R6's join contract type-checked rather than hopeful.

**Stratum 1 — Quantity.** `Money` (amount + currency), `Notional`, `Vol`,
`Greek` (value + unit), `Pips`, `Fraction` (0..1, for budgets/utilisation),
`Percent`, `AsOf` (instant + zone), `CutTime` (named cut + zone),
`Residual`/`Tolerance`. No formatted strings anywhere (R3); formatting is the
kit's job.

**Stratum 2 — Reference data** (Ring 1–2; changes only through governance):
`CurrencyPair` (premium ccy, delta convention, pip size, cuts), `Tenor`,
`Model` (version, status, validation-doc ref), `SelectionMatrixRow`
(instrument class × purpose → `ModelRef`), `QuoteClass` (staleness
tolerances), `CalendarEvent`, `PortfolioNode` (the 3-level tree),
`LimitDef`, `ScenarioDef` (incl. governance state, so DRAFT is data),
`Actor` (persona-typed), `DataSource`, `AutomationRule` (QW-3, QA-1 …).

**Stratum 3 — Market state at the as-of:** `MarketSlice`, `CurveSetEpoch`,
`SurfaceEpoch` (status enum + `waitingOn` ref), `PillarQuote` (source × pair ×
tenor × kind → value + state + reason), `FittedSmile` (points, residual,
tolerance, gate margins), `SpotRate`.

**Stratum 4 — Book:** `Instrument` (structured: type enum, strike, optional
`Barrier` {type, level, monitoring}, expiry + cut, notionals), `Trade`
(instrument, portfolio leaf, actor, lineage, optional `RfqId`), `Position`
(instrument, contributing `TradeId`s, greeks, reval budget, freshness),
`Rfq`.

**Stratum 5 — Journals (the history).** `JournalEntry` = `{JournalEntryId,
JournalId, Instant, ActorId, payload}` where the payload is a **sealed**
interface: `TradeBooked`, `TradeAmended`, `MarkOverridden`, `QuoteQuarantined`,
`QuoteReleased`, `QuotingStateChanged`, `PackageApproved`, `MarksSignedOff`,
`OpsActionTaken`, `BreachAcknowledged`.
*Why sealed:* the audit tape and every fold switch exhaustively over the
payload kinds — the compiler refuses a renderer that forgets one. (Same
sealed-vs-open reasoning the framework applies to `StandardJsModuleType`
versus the open `JsModuleType`.)

**Stratum 6 — Derived projections.** Produced, never stored: `BucketRow`,
`DeskTotals`, `LimitUtilization`, `Breach`, `ConcentrationBand`,
`ExpiryCluster`, `PinCandidate`, `PnlAttribution`, `ScenarioResult`,
`AuditTape`, `OverrideInventoryRow`, `EpochFlowState`, `LifecycleItem`.
They are `ValueObject`s *returned by* stratum-7 functions; nothing in strata
0–5 may reference them (R1).

**Stratum 7 — Behaviour** (the only non-`ValueObject` types):

| Type | Marker | Role |
|---|---|---|
| `DeskDataset` | `FunctionalObject` | the root: holds the facts, exposes the join contract as methods |
| `DeskQueries` | `StatelessFunctionalObject` | joins over a passed-in dataset (R6 keys) |
| `Derivations` | `StatelessFunctionalObject` | every R1 aggregate |
| `ParametricPricing` | `StatelessFunctionalObject` | greeks/PV behind the `ModelRef` seam (D4) |
| `BulkGenerator` | `StatelessFunctionalObject` | seeded, pure `(seed, index) → facts` (D3) |
| `JournalFold` | `StatelessFunctionalObject` | facts → current state (the P4 seam) |

```java
// Stratum 0 — identity is a value
public record TradeId(String value) implements ValueObject {}

// Stratum 1 — quantity carries its unit
public record Money(BigDecimal amount, Currency currency) implements ValueObject {}

// Stratum 4 — a fact: every edge is a typed id, every figure a quantity
public record Trade(TradeId id, InstrumentId instrument, PortfolioNodeId portfolio,
                    ActorId bookedBy, Money premium, Lineage lineage,
                    Optional<RfqId> rfq) implements ValueObject {}

// Stratum 7 — behaviour has no state, and no statics
public final class Derivations implements StatelessFunctionalObject {
    public DeskTotals totals(DeskDataset ds, List<PositionId> scope) { … }
}
```

### 3.4 Relationships

Every edge is **by typed id**, single-directional (child → parent, fact →
subject), and resolved through stratum-7 queries. No object graph cycles, no
back-pointers — the reverse direction is a query, which is what makes any
widget able to start from any node.

| From | Edge | To | Card. | Exists so that |
|---|---|---|---|---|
| `Position` | `instrument` | `Instrument` | 1 | strike/barrier/expiry render without duplication |
| `Position` | `contributingTrades` | `Trade` | 1..n | drill position → trade; amendment impact |
| `Position` | `portfolio` | `PortfolioNode` (leaf) | 1 | portfolio-tree selection at any level (R6) |
| `Position` | `lineage` | `MarketSlice`/`EpochRef`/`ModelRef` | 1 each | P1 explain; "everything priced by model X" |
| `Trade` | `portfolio`, `bookedBy`, `rfq?` | node / `Actor` / `Rfq` | 1 | blotter grouping; actor journals; RFQ→trade chain |
| `Trade` | *(reverse)* `amendments` | `JournalEntry` | 0..n | amendment history unfolds under the trade |
| `Instrument` | `pair`, `barrier?` | `CurrencyPair`, `Barrier` | 1 | pip-size maths, barrier proximity |
| `PortfolioNode` | `children` | `PortfolioNode` | 0..n | the 3-level tree; `leafIds()` closure |
| `SurfaceEpoch` | `pair`, `waitingOn?` | `CurrencyPair`, `PillarQuote` | 1 | the degradation chain, root-cause click-through |
| `PillarQuote` | `source`, `pair`, `tenor` | `DataSource`, … | 1 | feed grid; composite membership |
| `JournalEntry` | `actor`, `payload refs` | any stratum 2–4 | 1..n | the audit tape is a union of journals |
| `OverrideRecord` | `scope` | pair / epoch / mark / portfolio | 1 | one inventory across every scope |
| `ChangePackage` | `diff`, `impact` | `SelectionMatrixRow`, `PositionId` | 0..n | replay impact is a real position list |
| `LimitDef` | `scope` | `PortfolioNode` | 1 | utilisation follows the same tree as everything else |
| `ScenarioDef` | *(applied to)* | `Position` | n | results derive; definitions stay governed |

### 3.5 Usage — which widget consumes what

This is the connection map: a widget **drives** a key (publishes it),
**follows** it (filters on it), or **renders** the entities behind it.

| Type / projection | Widgets | Role |
|---|---|---|
| `PortfolioNode` | Portfolio Tree | drives `portfolioNodeId` (resolved to leaf set) |
| | Portfolio, Trade Blotter | follow → filter; blotter also groups by leaf (Σ view) |
| | Risk Views, P&L Explain | follow → limit scope, attribution scope |
| `Position` | Portfolio | renders + drives `positionId`/pair/tenor |
| | Risk Blotter (via `BucketRow`) | renders derived buckets; drives pair/tenor |
| | Concentrations, Expiry/Pins, Barrier Watch | render derived bands/clusters from the same positions |
| | Scenario Workbench | inputs to `ScenarioResult` |
| `Trade` | Trade Blotter | renders + drives `tradeId`, unfolds amendments |
| | Lifecycle, Audit Explorer, P&L Explain | follow → break subject, journal rows, amendment term |
| `Instrument` | Pricer, Client Pricer | render parsed ticket; drive pricing |
| | Barrier Watch, Expiry/Pins | barrier + expiry features |
| `SurfaceEpoch` / `FittedSmile` | Surface Manager | renders; drives `epochRef` |
| | Epoch Flow, Calibration Lab | follow → cadence state, diagnostics |
| | Pricer, RFQ Tape | lineage stamps (P1) |
| `PillarQuote` | Feed & Quality | renders grid; drives `sourceId`/quote state |
| | Surface Manager | pillar table + override target |
| `QuotingPairState` | Quoting Console | renders + drives pair; Ring-3 actions |
| `Rfq` | RFQ Tape | renders; drives pair; lineage to epoch |
| `Model` / `SelectionMatrixRow` | Model Inventory | renders; drives `modelRef` (reverse query) |
| | Change Console | diff subjects |
| `JournalEntry` | Audit Explorer | renders the union tape; filters by journal/actor |
| | Trade Blotter, Quoting Console, Override Inventory | render their own slice of the same journals |
| `OverrideRecord` | Override Inventory | renders every scope, oldest first |
| | Surface Manager | shows the mark override it owns |
| `ChangePackage` | Change Console | renders package; approval facts |
| `LimitDef` → `LimitUtilization` | Risk Views | renders utilisation + breach worklist |
| | Summary | rollup tile (projection only) |
| `SloSample`, `EpochFlowState` | Platform Console, Epoch Flow | render; drive root cause → pair |
| **Every type** | Summary Dashboard | projections only — no facts of its own (P5) |

**The invariant this table encodes:** no widget owns data. Each is a lens on
the same graph, so any selection made in one is resolvable by all the others —
including across workspaces, since the party bus carries ids (R6) and both
ends resolve against the same dataset.

### 3.6 The ontology gate

`fin-dash-data` ships `DeskOntologyTest`, which runs jOntology's
`PackageScanner` + `OntologyEnforcer` over the whole module and fails on any
violation. Rules:

1. **No `Mutable` type** in the module.
2. Every fact/id/quantity is a `ValueObject`; every behaviour type is a
   `FunctionalObject` or `StatelessFunctionalObject`.
3. **No static methods** (the enforcer's rule; `main` excepted) — behaviour
   lives on the singletons of stratum 7.
4. **One documented allowance:** JDK collection fields (`List`/`Map`/`Set`)
   fail the enforcer's transitive check (a `List` *could* be an `ArrayList`),
   but are permitted when the canonical constructor defensively copies via
   `List.copyOf`/`Map.copyOf`/`Set.copyOf`, which yields genuinely immutable
   instances. This mirrors the framework's own value objects (e.g.
   `ConformanceStudioFixtures(… , List<Crate> topLevel)` with `List.copyOf`).
   The allowance is a filter with a written reason — the same pattern as
   `FinDashConformance.ALLOWANCES` — never a suppressed check.

## 4. Realism requirements

- **Magnitudes:** desk-plausible (spots near market conventions for G10;
  vols 6–12; notionals 5–50M; greeks consistent with notional × vol scale).
- **Consistency invariants (tested):** position greeks sum to bucket and desk
  totals; utilization = usage/limit; portfolio position counts match; smile
  points consistent with pillar quotes (fit ≈ mkt − residual pattern); barrier
  distances = |barrier − spot| in pips using the pair's pip size; expiry
  clusters = positions expiring that day at that cut; audit tape ⊇ every
  journaled chain event.
- **Volume (decided: large):** ~8 pairs, 3-level tree with 10–14 leaves,
  **120–160 positions**, **150–200 trades**, ~25 RFQs, 3 days of journal
  history (~200–300 entries), 4–5 change packages, 3–4 scenarios with results.
  This stress-tests the density NFR (30+ rows without scrolling) and makes
  aggregation views read as real.
- **Heroes + generated bulk.** At this scale the dataset splits into:
  - **Hero entities** — hand-authored, id-stable, story-bearing: every
    participant in an R7 chain (the quarantined quote, the stale epoch, the
    KO trade `T-4471`, the amended one-touch, the NT position at 12 pips, the
    change packages, …). Heroes carry the demo narrative and the numbers the
    docs/wireframes cite; they never come from the generator.
  - **Bulk entities** — produced by a **deterministic generator**: fixed
    seeds, pure functions of (seed, index), no wall-clock — same build, same
    bytes, so R4 holds. Bulk fills books, tenor ladders, journal background
    noise, and RFQ traffic. The generator lives in the data module and runs
    at dataset construction (no build step); generated ids are namespaced
    (`P-9xxx`, `T-9xxx`) so heroes remain recognisable.
  - All R1 derivations and §4 invariants run over heroes + bulk combined; the
    integrity test does not distinguish them.
- **Time:** zone-explicit everywhere; cuts (NY 10am / Tokyo 3pm) as structured
  cut refs; the follow-the-sun handover is representable.

## 5. Packaging & access

- **Dependencies:** `fin-dash-data` depends on **jOntology core** (the
  markers) and, in test scope, **jOntology enforcing-utils** (the gate). It
  depends on no UI module, and on no homing serving module — the data must be
  buildable and testable with the whole front end deleted.
- **One home:** the dedicated `fin-dash-data` module (D1) exposing:
  - the typed entity records + the dataset instance (`DeskDataset.INSTANCE`);
  - **query helpers** that encode the join contract (`positionsIn(leafIds)`,
    `tradesFor(positionId)`, `pricedBy(modelRef)`, `journal(journalId)`,
    `auditTape()`, `derived aggregates`) so every feed uses the same joins;
  - derivation functions for every R1 aggregate.
- Feeds (`GetAction`s) become thin projections: query → JSON. **No feed may
  author domain facts.** JSON field names follow the entity model, not widget
  needs; formatting moves to the kit (`fdk.fmt` grows money/vol formatters).
- **Two gates**, both running under `mvn -o test` beside the conformance gate:
  - `DeskDatasetIntegrityTest` — referential closure, R1 derivation
    agreement, R7 chain traversals, R4 determinism, and the JSON export.
  - `DeskOntologyTest` — the jOntology enforcer over the whole module (§3.6).

## 6. Migration requirements

- Land the dataset + integrity test first; re-point feeds one workspace at a
  time (trader/book first — richest joins); delete each `DeskData` fragment as
  its consumers move. No screen may regress: existing verified behaviours
  (drill chain, portfolio filtering, aggregated blotter, story chips) must
  survive on the new data.
- Bus message shapes stay backward-compatible during migration (`pair`,
  `leafIds` fields keep their names).

## 7. Deliberate non-goals that shape the design anyway

- **Ticking:** the snapshot is "as-of = now, frozen". Entities that would tick
  (quotes, spots, ages) are modelled as values-at-instant so a later ticking
  layer replaces the instant, not the shape.
- **Time travel (P4):** journal-first (R8) means additional as-of snapshots
  are a matter of replaying journals to an earlier instant later; ids must
  therefore never be reused.
- **Multiple desks/asset classes:** out of scope; ids are namespaced so a
  second desk could be added without collisions.

## 8. Scoping decisions (agreed 2026-08-11)

- **D1 — Home: a new `fin-dash-data` module.** Own module, own integrity
  gate, **zero UI dependencies** (must not depend on `fin-dash-core`'s kit;
  if shared plumbing is needed, it moves down, never up). `fin-dash-core`
  keeps the kit/bus; persona modules and feeds depend on `fin-dash-data`.
- **D2 — Canonical form: Java records + JSON export.** Typed records are
  canonical (compile-time referential safety); the integrity test emits the
  complete dataset as one JSON artifact (`target/desk-dataset.json`) — the
  standing proof of UI-agnosticism and the seed for future non-Java consumers.
- **D3 — Scale: large** (§4 volumes, 120–160 positions) via the heroes +
  deterministic-generator split.
- **D4 — Pricing: calibrated parametric.** Smooth deterministic formulas;
  hero numbers stay pinned to the wireframes. The R5 lineage seam (`modelRef`
  on every figure) keeps a later upgrade to real closed-form math invisible
  to every consumer.
