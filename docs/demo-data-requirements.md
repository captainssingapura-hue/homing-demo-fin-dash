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

## 3. Entity inventory

Reference (Ring 1–2): `CurrencyPair` (conventions: premium ccy, delta
convention, cut times, pip size, spot), `Tenor`, `Model` (+version, status,
validation-doc ref), `SelectionMatrixRow` (instrument class × purpose → model),
`QuoteClass` (staleness tolerances), `EventCalendarEntry`, `ScenarioDef`
(governance state incl. DRAFT), `LimitDef`, `PortfolioNode` (3-level tree),
`Actor` (persona-typed), `DataSource`, `Rule` (QW-3, QA-1 — quoting/quality
automation rules referenced from journal entries).

Market state (per as-of): `MarketSlice`, `CurveSetEpoch`, `SurfaceEpoch` (per
pair: status, waiting-on ref, sub-version), `PillarQuote` (source × pair ×
tenor × kind: value, state, reason), `FittedSurface` (per tenor: smile points
mkt/fit, residual, tolerance, gate margins), `SpotRate`.

Book: `Instrument` (**structured**: type enum, strike, barrier {type, level,
monitoring}, expiry date + cut, notionals, ccys), `Trade` (journal-anchored,
lineage refs, portfolio leaf, actor, counterparty/RFQ ref), `Position`
(instrument ref, trade refs, greeks as numbers incl. smile-bucket vega,
reval-budget fraction, freshness), `Amendment` (journal).

Quoting/sales: `QuotingPairState` (+ rule ref, base vs current spread as
numbers), `Rfq` (client ref, instrument ref, quote, outcome, lineage).

Risk/control: `LimitUtilization` (derived), `Breach` (state machine:
unack/acked{by,at}), `ScenarioResult` (per scenario: derived from positions
via the deterministic parametric model), `PnlAttribution` (terms per position,
rolled up on demand), `IpvComparison`, `MarksSignoffState`.

Ops/governance: `QualityEvent`, `OverrideRecord` (scope ref, expiry,
approval), `ChangePackage` (diff entries as structured refs, replay impact:
{positionCount, flaggedPositionIds}), `LifecycleItem` (derived from positions
where possible: expiries, barrier watches; authored only for externals like
fixings), `BreakItem` (trade ref), `SloSample`, `EpochFlowState`.

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

- **One home:** a dedicated `fin-dash-data` module (or a `data` package in
  `fin-dash-core` — see open question Q1) exposing:
  - the typed entity records + the dataset instance (`DeskDataset.INSTANCE`);
  - **query helpers** that encode the join contract (`positionsIn(leafIds)`,
    `tradesFor(positionId)`, `pricedBy(modelRef)`, `journal(journalId)`,
    `auditTape()`, `derived aggregates`) so every feed uses the same joins;
  - derivation functions for every R1 aggregate.
- Feeds (`GetAction`s) become thin projections: query → JSON. **No feed may
  author domain facts.** JSON field names follow the entity model, not widget
  needs; formatting moves to the kit (`fdk.fmt` grows money/vol formatters).
- **Integrity gate:** a `DeskDatasetIntegrityTest` in the data module —
  referential closure, R1 derivation agreement, R7 chain traversals, R4
  determinism (no `Instant.now()` etc. by inspection). Runs with `mvn -o test`
  like the conformance gate.

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
