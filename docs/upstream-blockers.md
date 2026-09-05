# Upstream blockers — homing core

What this demo needs from the framework and cannot get for itself. Every item is
**unfixable downstream**: the framework either forbids the only working API,
owns the styling itself, or holds the seam a consumer would need.

Items 1–3 came out of taking `homing-demo-fin-dash` to zero inline styling —
220 conformance findings to 5, and those 5 are blocker #1. Items 4 and 5 came
out of building the risk ladder, the first downstream consumer of RFC 0050;
#5 is the largest single tax this repository has measured.

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

---

## 5. A grid displays ONE relation, so every desk re-invents the second one

**Severity: the largest single tax measured downstream.** Not a defect — a
missing facility, and the one this repository paid most for.

### The conceptual model

> A list of homologous relations displayed together with shared column controls.

Precisely: an **ordered list of relations over one column schema**, rendered in
one viewport with **shared column geometry**. Each relation carries its own
keys, its own row view, its own caption, its own depth and its own policies.

Homology is a **type-level** property, not a runtime check:

```java
List<Relation<T>>
```

One `T` fixes the column set, the key type and the headers. The compiler
enforces it at the point of authorship; the served grid receives a schema it
can trust because the widget could not otherwise have been compiled. That is
the same shape as RFC 0044 conformance — a build gate, not a runtime guard —
and the same move typed CSS already makes on the presentation side.

### The evidence: what the first consumer paid

`RiskLadderWidget` + `RiskLadderCellModule` are **319 effective lines** of
served JS. **98 of them exist only to teach a single-relation grid about rows
that are not data rows:**

| what those lines do | effective lines |
|---|---|
| row constructors, ordering, placing a subtotal last **within its own scope** | 28 |
| rendering a non-data row: void cells, the section band, indent | 45 |
| collapse: fold state, the toggle, the keyboard equivalent | 21 |
| safety: refusing writes, blanking aggregates on copy | 4 |
| **total** | **98 of 319** |

The part that is actually FX options risk — the column choice, the number
formatting, the freshness vocabulary, the pair focus — is the smaller half.

### What the model deletes

**The section header stops being a row.** It is a relation's *caption*. That
single move retires the `VoidCell` class and its nineteen lines of inert edit
contract, the zero-height trap (a cell rendering no text collapsing to no
height), the eight class resets per paint, and the trick where a full-row band
is drawn by every cell in the row agreeing to wear the same class. Largest of
the four blocks; it ceases to exist rather than getting a better name.

**Ordering becomes structural.** 17 lines encode "aggregates sort last within
their scope". Under the model a subtotal is a one-row relation placed after its
detail relation, so there is nothing to encode.

**The discriminator disappears.** A cell is never told which row it is in, so
today the row's `kind` is threaded down inside every cell value:

| a cell value carries | belongs to |
|---|---|
| `kind`, `n`, `text`, `state` | the cell |
| `agg`, `rowKind`, `indent`, `scope`, `leaf` | **the relation** |

Five of nine fields stop travelling per cell. What survives is the desk's
per-cell verdict, which is the half that was always a real judgement (P5).

**Fold** is collapsing a relation: hide its rows, keep its caption and its
subtotal relation. It becomes grid view state rather than a closure and a
filter predicate — which is also what lets it survive §4's `params()` seam.

**Read-only and copy policy become per relation**, which is *more* expressive
than per grid: a detail relation may well be editable in another widget while
an aggregate relation never is.

**Keys need only be unique within their relation**, so the synthetic ones the
ladder mints to dodge collisions (`pair + '/Σ'`, `'ΣΣ'`) disappear.

### Sorting

Today sorting is **one flat pass over a single global key list** —
`GridViewStateModule.applyRowView()` filters `basePks()` and sorts the
survivors. There is no boundary it could respect, so a sort on the ladder
would scatter captions and strand subtotals beside rows they do not total.
Sortability is currently a per-*column*, grid-wide predicate: the wrong axis.

The rule: **sorting never crosses a relation.** Scoped that way, "within a
single relation" is the only thing sorting *can* mean, and sortability becomes
per relation per column. A ladder then declares its relations unsortable —
typically the right answer for a ladder — and the pathological case is
unreachable by construction rather than merely unwired.

### Why this must be the framework's, not the desk's

**"Shared column controls" is the load-bearing half of the definition.** A desk
can already stack several grids down a pane. What it *cannot* do is make their
widths, order, hidden set and horizontal scroll agree without reaching into
each grid's layout — precisely the reach into framework internals the rule set
exists to forbid. Captions and per-relation policies could in principle be
domain code. Column agreement cannot. It is the irreducible contribution.

It is also the **positive form** of
[`virtualization-pseudo-requirement.md`](virtualization-pseudo-requirement.md).
That study concluded users scroll enormous tables because the domain never gave
them a breakdown mechanism. This is the mechanism: twelve relations of five
rows with their subtotals is what makes fifty thousand rows unnecessary rather
than merely slow.

### The line the facility must not cross

**The grid may own the SHAPE of an aggregate relation. It must never own the
NUMBER in it.** The moment the API offers `aggregate: 'sum'`, the grid is
computing risk — and this repository spent a commit removing exactly that
(backlog #3, "the desk judges, the UI renders"; P5 in the UI study). The desk
supplies every value, totals included.

This belongs in the RFC text itself, not in a doc explaining it afterwards.
Type discipline and P5 answer different questions: `T` gives you **shape, not
truth**. Nothing about the type stops a desk publishing a subtotal that does
not match the rows above it. The risk is feeling covered by the first and
quietly relaxing the second.

### API delta

| | today | proposed |
|---|---|---|
| unit of display | one relation | ordered `List<Relation<T>>` |
| homology | n/a | compile-time, one `T` |
| cell addressing | `(pk, col)` | `(relationId, pk, col)` — **the breaking change** |
| adapter | one | one per relation |
| sortable | per column, grid-wide | per relation, per column |
| read-only / copy policy | per grid | per relation |
| caption, depth | none | per relation |
| nesting | n/a | flat list + a depth per relation, **not** a tree |

Relation-qualified addressing is the real cost and is better taken deliberately
than discovered. Depth on a flat list gives arbitrary nesting (a region layer
above pairs is three deep) without importing a tree type and everything that
follows from one.

### What this does NOT fix

Two costs from the same build are **defects, not missing features**, and should
not be absorbed here:

1. **The rAF batching leak.** `updateCell` batches on `requestAnimationFrame`,
   which never fires in a hidden tab, so a fold left the caret pointing the
   wrong way until the pane came forward. `flushNow()` is domain code
   compensating for a leaky abstraction. Fix the abstraction.
2. **The zero-height cell contract.** A cell rendering no text collapsing to no
   height is a bug. It evaporates here only because void cells stop existing —
   luck, not a fix.

### Caveat on the evidence

This is **one consumer**. Some of the 98 lines are the ladder's taste rather
than structure, and a second consumer (`TradeBlotterWidget`, `PortfolioWidget`)
would separate them; neither has been tried. But height, copy semantics and the
edit contract follow from a non-data row *existing at all*, not from anything
specific to ladders. Those three need no second witness.

### Acceptance

The ladder rebuilt on the facility: on the order of **85 of its 98 structural
lines gone**, what remains being a declaration of which relations exist and
what each one is; the schema (columns, labels, key) sourced from `T` rather
than from quoted JS — which also dents [`backlog.md`](backlog.md) item #1;
conformance report unchanged; the runtime sweep green.

### A note on the name

`RelationGrid` currently displays a single relation, which made the name
slightly grander than the thing. *A list of relations over one `T` with shared
column controls* is what the name was always describing.

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
