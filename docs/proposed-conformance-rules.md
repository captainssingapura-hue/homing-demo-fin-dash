# Proposed conformance rules — CSS value discipline

Handoff note for homing core. Written from a downstream validation pass on
`homing-demo-fin-dash` against the RFC 0044 rule set as of `73036ce`.

## Why

`NoInlineStyleRule` + `NoRawCssRule` are a working pair and they close the
adoption gap: `no-raw-css` stops a module bypassing the css manager once it has
one, `no-inline-style` (ungated) forces a module with no `CssGroup` to adopt one.
On this repo they produce 200 findings and the ratchet works.

Both rules police **where** styling happens. Neither policies **what the values
are**. That is a hole with a specific failure mode:

> A widget's raw hex currently sits inside an inline style string, so
> `no-inline-style` sees it. Migrate that widget to a typed `CssGroup` and the
> hex moves into the class body — which no rule inspects. The finding
> disappears, the baseline shrinks, and the widget is still unthemeable.

The ratchet can be satisfied without delivering the thing it exists to deliver.
On this repo that is 94 literals on the served surface waiting to relocate.

---

## R1 — `no-literal-color`   *(generic; the one that matters)*

**Intent.** Colour comes from the theme via `var(--token)`. A served module must
not bake a literal colour value.

**Match**

| form | example |
|---|---|
| hex 3/6/8 digit | `#fff` `#B0741A` `#B0741Aff` |
| functional | `rgb(` `rgba(` `hsl(` `hsla(` |

**Do not match** — `var(--…)`, `transparent`, `currentColor`, `inherit`,
`none`, and **named colours** (`red`, `white`). Named colours read as ordinary
words and would false-positive on prose, ids and data; not worth the noise.

**Scope.** Consumer set, **ungated** — same reasoning as `NoInlineStyleRule`.
A module with no `CssGroup` is exactly the case to catch.

**False-positive risk: low.** Scanning this repo for non-colour `'#…'` string
uses (selectors like `'#root'`, fragment hrefs, id concatenation) returned
zero. If core wants belt-and-braces, skipping a `#` immediately followed by a
non-hex letter costs one lookahead.

**Note for core's own modules.** `SplitPaneModule` hardcodes
`rgba(0,0,0,0.08)` / `0.22` for the divider — R1 would flag it, correctly. That
literal is why a downstream theme currently cannot restyle the divider without
reaching into framework selectors (see R4). Worth fixing rather than exempting.

---

## R2 — `no-presentation-attribute`   *(generic; pre-emptive)*

**Intent.** SVG styling goes through CSS like everything else. Presentation
attributes bypass the theme exactly as `element.style` does, and are invisible
to `NoInlineStyleRule` because they are neither `.style.x =` nor
`setAttribute('style'`.

**Match** — `setAttribute(` with a first argument in:

```
fill  stroke  stroke-width  stroke-dasharray  stroke-linecap
opacity  fill-opacity  stroke-opacity
font-size  font-family  font-weight  text-anchor  dominant-baseline
```

**Must NOT match geometry** — `width` `height` `x` `y` `cx` `cy` `r` `d`
`viewBox` `points` `transform` `preserveAspectRatio`. These are structure, not
styling, and have no CSS-class equivalent in practice.

**Occurrences here: 0.** Proposed pre-emptively — the moment a chart widget
grows, this is where the styling escapes.

---

## R3 — amendment to `NoInlineStyleRule`   *(generic; two-line change)*

The `setProperty` escape hatch is right in spirit but unbounded. The javadoc
describes it as the way to set a **custom property** for a typed class to
consume as `var(--x)`. Nothing enforces the `--`:

```js
el.style.setProperty('color', '#fff');   // passes cleanly today
```

`STYLE_ASSIGN` requires `.style.NAME =`, and `setProperty` is a call, so there
is no match. Suggested added pattern:

```java
Pattern.compile("\\.style\\.setProperty\\(\\s*['\"](?!--)")
```

i.e. `setProperty` is exempt **only** when the property name starts with `--`.

**Occurrences here: 0.** Closing it before it is discovered as a workaround —
which it will be, once R1 makes inline hex expensive.

---

## R4 — a rule surface for theme CSS   *(generic; architectural, bigger ask)*

Rules operate on `ServedModule` — served **JS**. CSS authored Java-side in a
`Theme` is never scanned. So the layer whose entire job is styling is the one
layer with no styling discipline.

If core adds a `ServedStylesheet` analogue and a `CssRule` interface, two rules
follow immediately, both motivated by real defects found here:

- **`no-foreign-selector`** — a downstream theme must not target
  framework-internal selectors (`.hsp-*`). This repo's terminal theme overrides
  `.hsp-divider` because the divider colour is hardcoded upstream (R1). The rule
  would have failed that hack and pushed the fix to a token, which is the
  correct outcome.
- **`no-important`** — `!important` in a theme is a cascade defect, not a
  styling choice. Here it was needed because framework CSS wrapped in
  `@layer theme` loses to runtime-injected **unlayered** CSS regardless of
  specificity. The rule surfaces the layering bug instead of letting themes
  paper over it.

R1–R3 stand alone and do not depend on this.

---

## Two core fixes that are not rules

1. **The framework violates its own rule.** `WorkspaceWidget.selfContent`
   builds its error card with `err.style.cssText = …`. That is 39 of this
   repo's 121 `no-inline-style` findings, attributed to downstream module
   classes and **unfixable downstream**. It also puts a floor under the ratchet:
   "baseline may only shrink" bottoms out at 39, not 0.

2. **CSS endpoints ship no cache headers.** Cost real debugging time here — theme
   fixes appeared not to take effect. Unrelated to rules, worth batching.

---

## Suggested rollout

R1 is the one with real downstream debt behind it (94 literals). Landing it
warn-only first lets consumers baseline and migrate on the existing ratchet,
with no flag day. R2 and R3 can land as errors immediately — both are zero-
occurrence here, so they are pure prevention.

Local fallback if core defers: `FinDashConformance.POLICY` is ours, so R1 can be
implemented in-repo and lifted to core later. Preference is core — the rule is
generic and every consumer has the same hole.
