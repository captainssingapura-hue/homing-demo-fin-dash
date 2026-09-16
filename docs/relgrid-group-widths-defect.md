# RelGridGroup — column widths must be uniform across the group by construction

*Found 2026-09-15 rebuilding the risk ladder on the group (RFC 0050 · Episode 2),
against `homing-rel-grid` `docs/first-release` at `08574cd`, installed as
`LOCAL-SNAPSHOT`. fin-dash commit `86328ec` carries the workaround.*

## Observed

A group built **without** `columnWidths` renders with its members disagreeing on
where every column starts. Eleven tables — the header, five detail members, five
one-row subtotal members, one one-row book member — each sized its columns by its
own content:

```
Book ▸ tenor                              Δ          vATM        ← header: 'tenor' widest
1W              1.10M        18k                                 ← detail: Δ ends where content ends
Subtotal                     8.20M                               ← subtotal: Δ somewhere else again
```

Nothing threw. Nothing logged. The build, the conformance gate and the runtime
sweep were all green. The only way to see it was to look.

## Cause, from the code

`RelGridGroupModule.js`:

```js
this._widths = _hrggSnapshot(opts.columnWidths);   // empty when the option is absent
```

`_level()` applies only what `_widths` holds, and nothing derives a first
snapshot from the members. With no widths held, the group never touches a
member's `<colgroup>`, so every `<table>` falls back to its own automatic
layout. The Outlets bench does not show this because it seeds defaults on every
build (`{ dish: 140, sold: 80, revenue: 110, lastSale: 110 }`).

## Why this is a defect, not a missing option

`RelGridGroupContract`'s own javadoc:

> The one thing separate tables cannot agree on by themselves. The group
> applies its widths to every member at construction …

That is the group's invariant, and the reason it exists — everything else a
member does it would do standing alone. An invariant that holds only when the
host remembers to seed it is not one the group enforces; and the failure mode
is the worst kind: **silent, visual, and invisible to every automated gate.** A
consumer who reads the contract, builds a group, and looks at it will
reasonably conclude the group is broken.

Compare the group's own behaviour for the *other* homology check: two members
with different `columns()` throw at construction, naming both lists. Widths
should be held to the same standard.

## Proposed fix

**Derive, then apply.** At construction, when `columnWidths` is absent:

1. take the header's labels and every member's `columns()`;
2. compute a first width per column — the widest natural width across the
   header cells and every member's cells, bounded by `minColumnWidth` as a
   resize is;
3. apply that snapshot through `_level()` exactly as a seeded one would be, so
   `columnWidths()` reads it back and a host can persist it.

`columnWidths` stays as it is — the override a host uses to *restore* a
snapshot it kept. Nothing about the resize road changes.

If deriving is the wrong round for this cut, the minimum is to **refuse**:
throw at construction in `'group'` header mode when no widths are given, naming
the columns, as the column-list check already does. A refusal is honest; a
silent misalignment is not.

## Acceptance

- `RelGridGroupTest`: a group built with no `columnWidths` from members whose
  content widths differ per column; assert the header and every member put
  each column's edge at the same offset, and `columnWidths()` returns the
  derived snapshot.
- The Outlets bench built with its defaults removed renders aligned.
- fin-dash's `RiskLadderGroupWidget` drops its seed
  (`{ tenor: 118, delta: 74, … }`) and stays aligned — that is the consumer-side
  confirmation, and it is one line.

## For the KT's §9

Add it as item 13, above the widths note in 9.7: *a group built with no widths
does not level its members; seed one until this is fixed.* It would have saved
the hour it took to see the misalignment, find the empty snapshot, and confirm
the bench was masking it.
