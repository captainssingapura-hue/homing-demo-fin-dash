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

**Origin.** Named as the first of two genuine taxes in
[`react-rewrite-cost.md`](react-rewrite-cost.md) §5 and §8 — the honest half
of a study that otherwise found little in React's favour here.
