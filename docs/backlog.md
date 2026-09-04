# Backlog — homing-demo-fin-dash

Work this demo owes itself. Distinct from [`upstream-blockers.md`](upstream-blockers.md),
which is what the framework owes the demo. Items carry evidence and a
suggested fix, and are removed when done rather than ticked.

---

## 1. JavaScript authored as Java string arrays

**Severity: tax, not defect.** Nothing is wrong; everything is slower than it
should be, on every widget, every day.

**Evidence.** Of the demo's ≈4,600 lines of JavaScript, ≈3,650 are written
inside `constructBodyJs()` as `List<String>` — one Java string literal per
line — across all 41 widgets. The remaining ≈930 lines, in 9 modules, live on
the **co-located path**: a real `.js` file beside its `DomModule`
(`RiskLadderCellModule.js`, `TradeRowModule.js`, `DeskSecretaryModule.js`, …).
Both are served identically and pass through the same conformance gate, which
runs over the served artifact and does not care where the text came from.

What the string-array form costs, all of it observed while building the risk
ladder:

| cost | instance |
|---|---|
| no syntax awareness | a JS error is a runtime error in the browser, never a compile error in the IDE |
| escaping | every `"` is `\"`, every non-ASCII glyph is `\\u03a3`; a `\\u2014` inside a *comment* still has to be escaped |
| tooling hostility | `sed`/`perl` edits mangled `\\uXXXX` sequences twice in one session and once failed on CRLF; only exact-string editing was safe |
| the line gate | `max-effective-lines` counts served lines, so a widget approaching 250 is split by hand — `TradeRowModule` exists for this reason alone |
| no formatting, no linting, no refactoring | nothing that understands JavaScript ever sees it as JavaScript |
| review | a diff of quoted, escaped, indented-inside-Java lines is read by nobody with the care a `.js` diff gets |

The co-located path has none of these, and the cell module written on it was
the one part of the ladder that was pleasant to edit.

**Suggested fix.**

1. **Confirm the path for widget bodies.** The co-located mechanism is
   `DomModule`'s; establish whether a `WorkspaceWidget` body can be sourced the
   same way (a body resource beside the widget class), and if not, add that
   seam — it is the framework's one contribution to this item.
2. **Migrate largest-first.** The widgets nearest the 250-line gate benefit
   most; `TradeBlotterWidget`, `RiskLadderWidget`, `ClientPricerWidget`,
   `CalibrationLabWidget` are the first four. Each migration is mechanical:
   unquote, unescape, move.
3. **Acceptance.** No `constructBodyJs()` body longer than a screen; the
   conformance report unchanged (same module count, same baseline); the
   runtime sweep green. The 250-line rule stays exactly as it is — it counts
   served lines, and served lines do not change.

**What this does not fix.** The build-and-restart inner loop (on the order of
a minute per change) is the demo's other tax and a separate item when someone
takes it; a serve-from-source mode for `.js` resources would be its shape.

**Origin.** Named in [`react-rewrite-cost.md`](react-rewrite-cost.md) §7 and
§9 as the place where React's cost is lowest and this repository's highest —
a tax on the existing answer, not on its premise.

---

# Reference-implementation debt

This demo exists to be copied into real desks. So the criterion for what
follows is not *is it wrong* but **would it be copied** — and a practice that
appears twenty times in a reference implementation has already been copied
twenty times before anyone outside sees it. Evidence gathered 2026-09-04 by
grep over `src/main`, `lib/` excluded.

| # | item | sites | copy risk | size |
|---|---|---|---|---|
| 2 | swallowed exceptions | 25 in 8 widgets | high | small |
| 3 | risk judgement computed in the UI | 2 widgets, 3 thresholds | high | moderate |
| 4 | fetch and failure handling written per widget | 27 widgets | medium | moderate |
| 5 | actor identity from `Math.random()` | 20 widgets | medium | small ×20 |
| 6 | the runtime sweep is a manual protocol | — | high | large |
| 7 | contract methods stubbed; copy guard unreachable | 1 adapter | medium | small |
| 8 | baseline regeneration depends on shell piping | 1 test class | medium | tiny |
| 9 | a standing baseline of grandfathered findings | 5 findings, 1 module | medium | upstream |
| 10 | widget view state lost on reconstruction | 1 widget known | low | small |
| 11 | a vendored library four years old | 1 file, 600 KB | low | small |

## 2. Swallowed exceptions

**Copy risk: high.** `catch (e) {}` teaches that teardown may fail silently.

**Evidence.** 25 occurrences across 8 widgets — `VolSurfaceWidget` 4,
`RiskLadderWidget` 3, `TradeBlotterWidget` 2, one each in five more. Almost
all wrap the same two calls: `grid.destroy()` on a possibly half-built grid,
and `party.leave(actorId)` on a possibly already-left actor.

**Fix.** Do not catch; make the cases impossible. `destroy()` is only called
on a grid that was built (`if (grid)` already guards most sites); `leave()`
is only called once, from `partyDeregister`. Where a framework call can
genuinely throw on a legitimate path, that is an upstream item, not a
`catch {}`. **Acceptance:** zero `catch (e) {}` in served code; a
conformance rule `no-empty-catch` added to the desk's own rule set so it
stays zero.

## 3. Risk judgement computed in the UI

**Copy risk: high.** This is P5 — the UI is a consumer, not a calculator —
violated in the two widgets a desk would copy first.

**Evidence.** The server publishes raw `freshSecs` and `budgetFrac`
(`BookGetAction`, `PositionsGetAction`, `PlatformGetAction`) and no verdict.
`RiskLadderWidget` decides `secs > 10` is stale and `budgetFrac >= 0.90` /
`>= 0.75` is over / warn; `RiskBlotterWidget` carries its own copy of the
freshness cut-off; `PortfolioWidget` and `PlatformConsoleWidget` band values
locally too. The tolerances are desk facts and live in four places, none of
them the desk.

**Fix.** The actions publish the verdict beside the number — `freshState`,
`budgetState` — from one tolerance table in `fin-dash-core`. Widgets render
the state and never compare. **Acceptance:** no numeric threshold in any
widget's served JS; the RelationGrid case study's §9 gap closed.

## 4. Fetch and failure handling written per widget

**Copy risk: medium.** Twenty-seven independent copies of the same policy
means twenty-seven places for it to drift — and it has: the stale-response
guard that `LifecycleWidget` needed after rapid navigation exists there and
nowhere else.

**Evidence.** 27 widgets each carry `fetch(url).then(r => { if (!r.ok) throw
… })` and their own "load failed" rendering.

**Fix.** `fdk.load(branch, host, url, paint)` in the kit: one HTTP policy,
one JSON policy, the stale-response guard by construction (the branch is the
generation token), one failure rendering. **Acceptance:** no `fetch(` in a
widget body; failure copy identical across the desk.

## 5. Actor identity from `Math.random()`

**Copy risk: medium — and already realised.** The pattern
`actorId = '<kind>-' + Math.random().toString(36).slice(2, 8)` appears in
20 widgets verbatim. The desk's own `DeterministicRiskRule` bans
`Math.random()` in risk models; its party identities are random everywhere.

**Consequences.** No stable identity across a workspace reconstruction, so
nothing can be addressed or persisted by actor; the runtime sweep cannot
reproduce a bus interaction; a log line names an actor that will never exist
again.

**Fix.** Identity from the workspace — the pane/widget instance id
`workspaceCtx` can supply — or, failing that, a per-kind counter. One helper
in the kit, twenty call sites. **Acceptance:** `Math.random` absent from
served code; the deterministic-risk rule widened to say so.

## 6. The runtime sweep is a manual protocol

**Copy risk: high, in the other direction.** The sweep — mount every widget,
click every control twice, close all tabs first — found every runtime defect
this month (fixed-name-per-row in two widgets, an undissolved branch that
would have thrown on the second price, journal stacking, self-filtering on the
bus). Nothing runs it. A desk that copies this repository copies a test suite
of 7 files across 3 of 18 modules and a protocol in a knowledge-transfer
document.

**Fix.** Automate the sweep as a test: headless browser, every widget kind
mounted, every control exercised twice, assertions on console errors,
duplicate-name throws and orphaned branches. Then per-module tests for the 21
actions, which are pure functions of deterministic data and have none.
**Acceptance:** the sweep is a build step; each module with a `GetAction`
has a test asserting its shape.

## 7. Contract methods stubbed; the copy guard unreachable

**Copy risk: medium.** A reference adapter with `subscribe: function (fn)
{}` teaches that a contract is something to stub.

**Evidence.** `RiskLadderWidget`'s adapter stubs `subscribe`, `unsubscribe`
and `deleteRows`, and `update` returns silently. `onCopy` is not supplied to
the grid, so `Ctrl+C` does nothing and the cells' `getValueToCopy()` guard —
described three times as protecting a live path — is unreachable.

**Fix.** Wire `onCopy` (the guard becomes real). Implement `subscribe` over a
polling or push feed (the case study's §8 path). For a read-only adapter,
`deleteRows` and `update` should refuse loudly, not silently. **Acceptance:**
`Ctrl+C` in the ladder copies with aggregates blank; a fixture feed ticks
the ladder.

## 8. Baseline regeneration depends on shell piping

**Copy risk: medium.** KT.md §9.3 warns *never* to regenerate the baseline by
piping console output to a file on Windows, because UTF-8 mangling makes the
whole baseline load empty and every violation pass. A procedure with a
footgun in its knowledge-transfer notes is a procedure that will be run
wrong.

**Fix.** `BaselineRegen` writes the file itself, in UTF-8, and prints only a
summary. **Acceptance:** KT.md §9.3 deleted because it no longer applies.

## 9. A standing baseline of grandfathered findings

**Copy risk: medium.** The baseline "may only shrink" and has held at five
for weeks, all on `SmileChartModule`, all because conformant code cannot
create SVG ([`upstream-blockers.md`](upstream-blockers.md) §1). A permanent
baseline teaches that baselines are permanent.

**Fix.** Upstream: `branch.createElementNS`. Downstream, nothing is possible;
`FdChartCss` is already written and waiting. **Acceptance:** baseline empty;
the file kept, with its header, as the record that it once was not.

## 10. Widget view state lost on reconstruction

**Copy risk: low.** `RiskLadderWidget` takes `_None` params, so fold and
focus live in a closure and a replay reopens every block. Other widgets
likely share it; the ladder is the one known.

**Fix.** Fold set and focus as widget params, per the workspace persistence
model. **Acceptance:** reload the workspace, the folds survive.

## 11. A vendored library four years old

**Copy risk: low.** `three@0.128.0` (April 2021), 600 KB, pinned with no
recorded refresh procedure. The React study praises "one file, no resolver";
the other half of that bargain is that someone has to refresh it by hand,
and nobody has.

**Fix.** Pin to a current release and record the procedure — where it is
fetched from, how it is verified, what the vol-surface module needs from
it — beside the file. **Acceptance:** a `lib/README.md` that answers those
three questions.
