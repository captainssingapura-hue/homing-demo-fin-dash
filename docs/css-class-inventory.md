# CSS class inventory

The typed-class vocabulary for all 39 widgets, grouped by concern.

## Method

Not invented — derived. Every inline style fragment in the repo was extracted
(576 of them across `*.java` widget bodies and `*.js` primitives), split into
declarations, and clustered by frequency. Counts below are real occurrence
counts, so every class earns its place. Anything appearing once stayed inline
for now rather than inflating the vocabulary.

The evidence was decisive: 16 style strings repeat **verbatim** 4+ times, and
the top 3 account for 77 occurrences. The widgets already share a vocabulary —
it just isn't named anywhere.

## Grouping

`CssGroup` is itself an `EsModule`, and `DomModule.cssGroups()` is derived from
imports — any imported module that is a `CssGroup` is picked up automatically as
a CSS dependency. So groups are **modules a widget imports**, exactly like
`fdk` and `fdGrid` today, and a widget pulls only the concerns it uses.

Seven groups. The split is by *concern*, so that a widget importing charts
doesn't drag in form controls:

| group | concern | imported by |
|---|---|---|
| `FdFrameCss` | shell, scroll, flex/grid structure | all 39 |
| `FdTextCss` | type scale, weight, numerics | all 39 |
| `FdSurfaceCss` | panels, cards, separators | ~30 |
| `FdStatusCss` | semantic state + chips | ~22 |
| `FdControlCss` | buttons, inputs, selection | ~18 |
| `FdDataCss` | tables, cells, meters, kv, stamp | ~14 |
| `FdChartCss` | SVG chart internals | 2 |

---

## 1. `FdFrameCss` — structure

| class | declarations | seen |
|---|---|---|
| `widget-root` | `height:100%;overflow:auto;box-sizing:border-box;padding:14px;font-family:system-ui,sans-serif;font-size:13px;color:var(--color-text-primary)` | 27 / 25 |
| `widget-root-flush` | as above, `padding:0` | 12 |
| `doc-root` | `padding:20px;font-family:system-ui,sans-serif;font-size:13px` | 12 |
| `header-row` | `display:flex;align-items:baseline;gap:10px;flex-wrap:wrap` | 14 |
| `row` | `display:flex;gap:10px;align-items:center;padding:6px 4px` | 7 |
| `cluster` | `display:inline-flex;gap:6px;align-items:center` | 10 |
| `stack` | `display:flex;flex-direction:column;gap:var(--space-2)` | — |
| `grid-auto` | `display:grid;grid-template-columns:repeat(auto-fit,minmax(180px,1fr));gap:var(--space-3)` | 3 |
| `spacer` | `flex:1` | 6 |
| `section` | `margin-top:var(--space-2)` | 6 |
| `hidden` | `display:none` | 2 |

`widget-root` merges the two strings that always co-occur (the ×27 typography
and the ×25 scroll frame) — they are one concept split by accident.

## 2. `FdTextCss` — typography

Today there are **ten** font sizes: 10.5, 11, 11.5, 12, 12.5, 13, 14, 18, 20,
22. None of them carries business meaning. The co-occurrence data shows why:

- **11px** almost always pairs with `color:var(--color-text-muted)`; **11.5px**
  almost always with `color:var(--color-text-primary)`. Same role — secondary
  text — with the real variable being **colour, not size**. The half-pixel is an
  accident riding along with an emphasis decision.
- **12.5px** is the table-row size (co-occurs with `border-bottom:1px solid` in
  10 of its uses); **12px** is controls. One arguable distinction, expressed as
  a difference nobody can perceive.
- **18 / 20 / 22px** is three sizes for **six total uses**, all `font-weight:700`.
- **14px** is the one clean case: 25 uses, all identical, all widget titles.

What does carry meaning is **density tier** (a blotter of 40 rows genuinely
needs tighter text than a pricer with 6 fields — a real trading requirement)
and **emphasis** (already carried by `muted` / `strong` / `strongest` as
orthogonal classes). Once emphasis leaves the size axis, five steps remain:

| class | declarations | role | replaces | seen |
|---|---|---|---|---|
| `display` | `font-size:20px;font-weight:700` | hero number, glanceable (P2) | 18, 20, 22 | 6 |
| `title` | `font-weight:700;font-size:14px;letter-spacing:0.3px` | widget identity | 14 | 25 |
| `body` | `font-size:13px` | default reading text | 13 | 45 |
| `dense` | `font-size:12px` | table and blotter rows | 12, 12.5 | 73 |
| `caption` | `font-size:11px` | labels, stamps, footnotes | 10.5, 11, 11.5 | 103 |

Emphasis and colour stay orthogonal — never encoded in size:

| class | declarations | seen |
|---|---|---|
| `section-title` | `font-weight:600;letter-spacing:0.2px` | — |
| `strong` | `font-weight:600` | 18 |
| `strongest` | `font-weight:700` | 4 |
| `muted` | `color:var(--color-text-muted)` | 69 |
| `num` | `font-variant-numeric:tabular-nums` | 15 |

**Drift being corrected:** ~85 of 252 font-size sites change, none by more than
0.5px except 10.5→11 and 18→20 (three uses). `display` is borderline at six
uses; it survives because a risk total readable at a glance is a requirement,
not decoration.

## 3. `FdSurfaceCss` — surfaces and separators

| class | declarations | seen |
|---|---|---|
| `panel` | `background:var(--color-surface-raised);border-radius:var(--radius-md);padding:8px 10px` | 5 |
| `card` | `border:1px solid var(--color-border);border-radius:var(--radius-md)` | 18 |
| `inset` | `background:var(--color-surface)` | 12 |
| `rule` | `border-bottom:1px solid var(--color-border)` | 14 |
| `rule-top` | `border-top:1px solid var(--color-border)` | 3 |
| `rule-strong` | `border-bottom:2px solid var(--color-border)` | 1 |

## 4. `FdStatusCss` — semantic state

**This group is where all 64 `no-literal-color` errors land.** Every class
references a `--color-status-*` token; no hex survives.

| class | token | replaces | seen |
|---|---|---|---|
| `status-good` | `var(--color-status-good)` | `#006300` | 6 |
| `status-warn` | `var(--color-status-warn)` | `#9a6b1f` | 16 |
| `status-serious` | `var(--color-status-serious)` | `#a8502a` | 30 |
| `status-critical` | `var(--color-status-critical)` | `#a32e2e` | 8 |
| `status-neutral` | `var(--color-status-neutral)` | `#52514e` | 4 |
| `status-*-bg` (×5) | `var(--color-status-*-bg)` | `#e6f4e6` `#fdf3dc` `#fceee8` `#f9e4e4` `#efefec` | 18 |
| `chip` | `display:inline-block;font-size:11px;font-weight:600;border-radius:999px;padding:2px 10px;white-space:nowrap` | — | — |
| `chip-*` (×5) | fg + bg pair per state | — | — |
| `error-text` | `var(--color-status-serious)` | the `#a8502a` error strings | 25 |

Tokens are **declared** now, seeded with today's exact hex → zero visual change.
Choosing validated light/dark values is a later theme-only edit touching no
widget. That separation is the whole point of tokenising.

## 5. `FdControlCss` — interactive

| class | declarations | seen |
|---|---|---|
| `btn` | `padding:5px 12px;font-size:12px;font-weight:600;border:none;border-radius:var(--radius-md);cursor:pointer;background:var(--color-accent);color:var(--color-accent-on)` | 4 + 5 |
| `btn-ghost` | as `btn`, `background:transparent;border:1px solid var(--color-border);color:var(--color-text-primary)` | — |
| `input` | `border:1px solid var(--color-border);border-radius:var(--radius-md);background:var(--color-surface);color:var(--color-text-primary);padding:4px 8px` | — |
| `clickable` | `cursor:pointer` | 26 |
| `selected` | `border-color:var(--color-accent)` | — |
| `focus-ring` | `outline:2px solid var(--color-accent);outline-offset:1px` | — |
| `tab` / `tab-active` | tab strip pair | — |

`outline-color:#2a78d6` and `background:#2a78d6` both resolve to
`var(--color-accent)`, which already exists — no new token needed.

## 6. `FdDataCss` — tables, meters, lineage

| class | declarations | seen |
|---|---|---|
| `table` | `border-collapse:collapse;width:100%` | 4 |
| `th` | `text-align:left;font-weight:600;font-size:12px;border-bottom:1px solid var(--color-border)` | 6 |
| `td` | `font-size:12.5px;border-bottom:1px solid var(--color-border)` | 6 |
| `td-num` | `text-align:right;font-variant-numeric:tabular-nums` | 7 |
| `row-group` | group header row emphasis | — |
| `row-indent` | `padding-left:var(--space-3)` | 1 |
| `meter` | `display:inline-block;height:6px;border-radius:3px;vertical-align:middle` | — |
| `meter-track` | `background:var(--color-status-*-bg)` | — |
| `meter-fill` | `display:block;height:6px;border-radius:3px;width:calc(var(--meter-frac) * 100%)` | — |
| `kv-row` / `kv-label` / `kv-value` | the `fdk.kv` triple | — |
| `stamp` | `color:var(--color-text-muted);font-size:11px;white-space:nowrap` | — |

**The meter is the sanctioned dynamic case.** Its width is live data, so it
cannot be a static class. It sets `--meter-frac` via
`style.setProperty('--meter-frac', f)` and the class consumes it as
`calc(var(--meter-frac) * 100%)`. That is precisely the custom-property escape
hatch R3 permits, and the only place in the vocabulary that needs it.

## 7. `FdChartCss` — SVG internals

`SmileChartModule` only. Classes for `axis`, `gridline`, `series`,
`series-point`, `annotation`, `label` so chart styling goes through CSS rather
than presentation attributes — keeps R2 satisfied as the chart grows.

---

## What this changes

- **~70 classes** replace 576 inline fragments.
- All 64 `no-literal-color` errors resolve inside `FdStatusCss` + existing
  `--color-accent`.
- All 82 `no-inline-style` findings resolve as widgets adopt the groups.
- `no-raw-css` starts applying once the css manager is injected, so class usage
  is typed from that moment — untyped `className =` / `classList.add` becomes a
  build error rather than a silent habit.

## Open decisions

1. **`fdk.el` signature.** It takes a style string; ~400 call sites pass one.
   Suggest adding a parallel class-taking form so widgets migrate one at a time
   instead of a big-bang change.
2. **Space tokens.** `--space-*` exist in `StudioVars`; the inventory uses them
   for padding/gap. Raw px in the tables above are the current literal values,
   to be mapped to the nearest existing step during implementation.
