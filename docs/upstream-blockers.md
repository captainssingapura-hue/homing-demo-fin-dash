# Upstream blockers — homing core

Two defects found by taking `homing-demo-fin-dash` to zero inline styling. Both
are **unfixable downstream**: no amount of consumer work clears them, because
the framework either forbids the only working API or owns the styling itself.

Everything else from that migration is done — 220 conformance findings to 5, and
those 5 are blocker #1.

---

## 1. Conformant code cannot create SVG

**Severity: blocking.** One module (`SmileChartModule`) cannot be made
conformant at any effort.

Two framework facts that do not compose:

| | |
|---|---|
| `branch.createElement` | ends in `document.createElement(tagName)` — [DomOpsPartyBaseModule.js:158](). There is **no namespace support anywhere in DomOpsParty**: `grep -c createElementNS` over both `DomOpsParty*.js` returns 0. |
| `UseDomOpsPartyRule` | forbids `document.createElementNS` explicitly — the pattern at [UseDomOpsPartyRule.java:34]() matches `createElement\|createElementNS\|createTextNode\|createDocumentFragment` on a `document.` receiver. |

So the rule bans the only API that works, and the sanctioned API cannot do the
job: SVG elements built via `document.createElement('path')` are HTML elements
named `path` and never render.

**Consequence here.** `SmileChartModule` — the W2 volatility smile — accounts
for all 5 remaining findings in this repo's baseline:

```
[use-dom-ops-party] var e = document.createElementNS(NS, tag);
[no-inline-style]   svg.style.cssText = 'display:block;background:' + SURF + …
[no-literal-color]  var BLUE = '#2a78d6', MUTED = '#898781', GRID = '#e1e0d9',
[no-literal-color]  SURF = '#fcfcfb', INK = '#52514e', WARN = '#9a6b1f';
[no-literal-color]  fill: overridden ? '#fab219' : INK, stroke: SURF, …
```

**Suggested fix.** `branch.createElementNS(name, ns, tagName)`, or a namespace
option on `createElement`. Ownership, naming and `dissolve()` semantics are
unchanged — only the factory call differs.

**Downstream is ready.** `FdChartCss` is already written (axis, gridline,
series, series-point, annotation, label, all token-based). The chart converts as
soon as a namespace-aware factory exists; the remaining 5 findings clear with it.

---

## 2. `TreeRendererModule` styles inline, so a theme cannot restyle it

**Severity: high.** Not blocking a build, but it puts unthemeable pixels inside
every consumer that uses the framework's tree.

`TreeRendererModule.js` carries **10 inline styling sites**, including baked
colours:

| line | code |
|---|---|
| 153 | `row.style.cssText = 'display:flex;align-items:center;gap:6px;padding:3px 6px;'…` |
| 158 | `caret.style.cssText = '…color:#888;font-size:10px;…'` |
| 167, 171 | `label.style.cssText = 'flex:1;white-space:nowrap;overflow:hidden;'…` |
| 216, 219 | `row.style.background = 'rgba(0,0,0,0.05)'` / `''` (hover) |
| 225, 229 | `row.style.background = 'rgba(59,130,246,0.20)'` (selection) |
| 275 | `entry.kidsEl.style.display = expanded ? 'block' : 'none'` |

**Consequence here.** `PortfolioTreeWidget` is fully converted and conformance-
clean, yet renders **40 inline-styled elements** at runtime — every one of them
minted by `TreeRenderer`, none by the widget. The hover and selection colours in
particular (`rgba(0,0,0,0.05)`, `rgba(59,130,246,0.20)`) are invisible to the
theme layer, so on this repo's dark terminal theme they are a grey smear and a
blue that belongs to no palette.

**Suggested fix.** The same treatment the rules already require of consumers: a
`CssGroup` for the tree's own classes, with hover and selection as classes
rather than background writes, and `#888` / the two `rgba()` values routed
through `--color-*` tokens.

---

## 3. `SplitPaneModule` hardcodes its divider colour

**Severity: low effort, high annoyance.** Previously raised; now with evidence
that makes the fix obvious rather than a judgement call.

`.hsp-divider{…background:rgba(0,0,0,0.08)…}` at line 57, `rgba(0,0,0,0.22)` on
hover at line 60, injected via a `<style>` element the module builds itself. No
theme can reach either value. This repo works around it by overriding
`.hsp-divider` from its own theme — precisely the reach into framework
internals a `no-foreign-selector` rule ought to forbid.

**It is the only module in its own package that does this.** Its sibling
`MultiTabPaneModule` injects a `<style>` block the same way and is entirely
token-based:

```css
.hmtp-leaf   { background: var(--color-surface); color: var(--color-text-primary); }
.hmtp-strip  { background: var(--color-surface-raised);
               border-bottom: 1px solid var(--color-border); }
.hmtp-chip   { color: var(--color-text-muted); }
```

So this is not a missing convention — it is one module out of step with the
convention its neighbour already follows. The minimal fix is two substitutions:
`rgba(0,0,0,0.08)` → `var(--color-border)`, `rgba(0,0,0,0.22)` →
`var(--color-border-emphasis)`. That alone retires this repo's `.hsp-divider`
override.

### A stronger option, with a working reference

`js-demos/split_pane/SplitPane.js` — the standalone original the framework
module derives from — does not own its dividers at all. They are **injected by
the caller** (`opts.dividers.vertical`, `opts.dividers.horizontal`); the library
only measures them (`offsetWidth`) and binds drag behaviour, toggling an
`sp-active` class. Measured against the framework copy:

| | demo `SplitPane.js` | framework `SplitPaneModule.js` |
|---|---|---|
| `createElement` calls | **0** | creates dividers (lines 269, 438) |
| colour literals | **0** | 2, injected via `<style>` |
| divider ownership | caller supplies | library owns and styles |

The framework version **regressed** from that design when it took over divider
creation. Restoring caller-injected dividers removes the whole class of problem
rather than recolouring it — the consumer supplies an element it already styles
through its own CssGroup, and the library never has an opinion about colour.

More invasive than the two substitutions, so worth treating as the follow-up
rather than the immediate fix; recorded here because the reference
implementation exists and works.

**The pattern.** All three are framework primitives that style themselves while
the rule set requires consumers not to. Worth a sweep of the primitives rather
than three point fixes — the consumer-side discipline is now proven achievable
at scale (39 widgets, ~576 style fragments, zero left), so the same standard is
reasonable to hold the framework's own view code to.

Note that the sweep is smaller than it looks: `MultiTabPaneModule` already
passes, and `SplitPaneModule` needs two substitutions to join it. The real work
is `TreeRendererModule` (10 sites) and the SVG factory.

---

## 4. A widget cannot persist its own view state

**Severity: limiting.** Not a defect — a missing seam.

A tab's params are written once, at spawn (`WorkspaceStateModel`,
`TabRegistry`), and the shell reads nothing back from a widget's controller
except `root`, `setActive`, `partyDeregister` and the write-lock `takeOver`.
So a widget whose *view* has state — the risk ladder's folded sections and
its pair focus; a tree's expanded nodes; a blotter's sort — cannot say "my
params changed", and a reconstruction of the workspace replays the params it
was spawned with. In the ladder, every block reopens.

**What it costs downstream.** The state lives in a closure by necessity. The
desk's backlog carries it as item #10 with nothing to do until this lands.

**Suggested fix.** One optional controller method, read by the shell at
checkpoint time and on tab close:

```js
// controller shape, RFC 0028 — proposed addition
{ root, setActive, partyDeregister?, onClose?, params? }
//                                             ^ () => Params — the widget's
//                                               CURRENT params, replacing
//                                               the spawn-time ones in the
//                                               persisted tab
```

`params()` returns the same shape `construct(branch, params, ctx)` received,
so the persistence model, the codec and the picker are untouched; only the
checkpoint reads it when present. A widget that does not implement it
persists as today. The ladder would return `{ focus, folded }` and
reconstruct with them.

## Note on what conformance did and did not catch

Offered because it bears on where rules are worth adding.

Of roughly 300 defects fixed during the migration, the gate reported about one
in seven. Three failure modes it does not model, each of which cost real
debugging here:

1. **Styling laundered through a helper.** `no-inline-style` matches
   `.style.x =` and `setAttribute('style')`, so `fdk.el('div', 'color:#a8502a')`
   passed clean. One widget had 15 such calls and 3 findings.
2. **An undeclared `CssClass`.** A class reaches the served JS only via a
   `ModuleImports` entry; without one it is a runtime `ReferenceError`.
3. **A duplicate branch element name** — inside a loop, or minted by an event
   handler that fires twice. `createElement` throws, and nothing static catches
   it. This one only appears on a *second* interaction, so it survives a
   single-click smoke test as well.

(3) is arguably best addressed by JS-level unit tests rather than a conformance
rule. (1) and (2) look like genuine rule candidates.

*Update:* (3) is now caught at build time. The runtime sweep (KT.md §7e)
dispatches every recorded `click`/`dblclick` twice after mounting each
widget under GraalJS; its first run reported `DataTypeTreeWidget` minting
`chip-1` again on the second selection — the exact shape described above —
and the fix (a dissolvable sub-branch) followed. A second interaction is no
longer something a smoke test can miss.
