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
| 9 | a standing baseline of grandfathered findings | 5 findings, 1 module | medium | upstream |
| 10 | widget view state lost on reconstruction | 1 widget known | low | small |
| 11 | a vendored library four years old | 1 file, 600 KB | low | small |
| 12 | click-everything not yet automated | — | low | small |
| 13 | dependency convergence not enforced | 1 split found | medium | small |

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

## 12. Click-everything not yet automated

**Copy risk: low.** The mount half of the runtime sweep is a build step
(`WidgetMountSweepTest`, KT.md §7e); the "click every control twice" half is
still the manual protocol. It found the fixed-name-per-row defects at mount
time, so what it would add is the second-render class: a control whose
handler re-renders on a branch it did not dissolve.

**Fix.** The stub already records every listener; dispatch each `click`
twice per widget after mount and assert as the mount sweep does. Next
increment on the same harness, not a new one. **Acceptance:** the manual
protocol deleted from wherever it still lives.

## 13. Dependency convergence not enforced

**Copy risk: medium.** vert.x 4.5.11 brought `jackson-databind 2.17.2` and
`jackson-core 2.16.1` into the same classpath and nothing said so; parsing a
float threw `NoSuchMethodError`, and the desk did not notice because it only
ever encoded. The sweep found it by parsing an action's answer. A reference
implementation that ships a split classpath teaches that it is fine.

**Fix.** The root pom now manages Jackson at one version (KT.md §9.9). The
general fix is `maven-enforcer-plugin` with `dependencyConvergence` (or
`requireUpperBoundDeps`) — not added today because the plugin is not in the
offline repository and the build's portability promise is checked offline.
**Acceptance:** the enforcer rule in the root build, green.
