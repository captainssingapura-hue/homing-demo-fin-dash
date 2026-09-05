# A List of Homologous Relations

*A proposal for RFC 0050, written from the first downstream consumer.*

---

## 1. The proposal

> **A list of homologous relations displayed together with shared column
> controls.**

Precisely: an **ordered list of relations over one column schema**, rendered in
one viewport with **shared column geometry**. Each relation carries its own
keys, its own row view, its own caption, its own depth, and its own policies.

Homology is a **type-level** property, not a runtime check:

```java
List<Relation<T>>
```

One `T` fixes the column set, the key type and the headers. The compiler
enforces it where the code is written; the served grid receives a schema it can
trust, because the widget could not otherwise have compiled. That is the same
shape as RFC 0044 conformance — a build gate rather than a runtime guard — and
the same move typed CSS already makes on the presentation side.

That is the whole proposal. The rest of this document is why, what it deletes,
and where its edges are.

---

## 2. Where this came from

`RiskLadderWidget` was the first downstream consumer of RFC 0050. The feature
is unremarkable on a trading floor: the book down the curve, a subtotal closing
each currency pair, the book total closing the table, blocks that fold.

It was built, it works, and it was **not hard**. That is the point worth
holding onto. The argument here is not that the primitives were inadequate.
They were sufficient, and a careful consumer got a correct result out of them.
The argument is about **what that consumer had to know**, and what the next
twenty will not.

---

## 3. What a single-relation grid makes you build

`RiskLadderWidget` and `RiskLadderCellModule` are **319 effective lines** of
served JavaScript. **98 of them exist only to teach a one-relation grid about
rows that are not data rows.**

| what those lines do | effective lines |
|---|---|
| row constructors, ordering, placing a subtotal last *within its own scope* | 28 |
| rendering a non-data row: void cells, the section band, indent | 45 |
| collapse: fold state, the toggle, the keyboard equivalent | 21 |
| safety: refusing writes, blanking aggregates on copy | 4 |
| **total** | **98 of 319** |

The part that is actually FX options risk — the column choice, the number
formatting, the freshness vocabulary, the pair focus — is the smaller half.

### 3.1 The four lines that matter most

The safety row is four lines, and it is the strongest argument in this
document.

```js
// An aggregate's NUMBER never leaves as data. A ladder copied into a mail
// or a sheet is a real desk gesture, and a subtotal pasted alongside the
// rows it totals is a column that sums to twice the book.
if (this._value.agg && this._value.kind !== 'label') return '';
```

Remove that and a trader copies a ladder into a spreadsheet where the column
sums to twice the book. It is a **financial error produced by an omission in a
user interface**. Nothing in the grid, the conformance rules, or the runtime
sweep would have caught it. It exists because the author happened to be
thinking about copy semantics that afternoon.

The other two lines refuse writes the adapter cannot honour:

```js
update:     function (pk, col, v) { throw new Error('risk ladder is read-only: ' + pk + '/' + col); },
deleteRows: function (pks)        { throw new Error('risk ladder is read-only: rows belong to the book'); }
```

A read-only relation forced to implement a read-write interface, and to refuse
at runtime what it could have declined to offer.

### 3.2 The structural burden, in full

**Row kinds as a runtime discriminator.** Four row constructors and a `kind`
field: `section`, `leaf`, `subtotal`, `grand`. Because a cell is never told
which row it sits in, the kind then has to be threaded down inside *every cell
value*, alongside four more row properties.

**Ordering by hand.** Seventeen lines to interleave sections, details and
subtotals such that an aggregate sorts last *within its own scope* — a pair's
subtotal closing its own block, the book total closing the table. Last-overall
would tear each subtotal away from the rows it totals.

**A row that is not a row.** A section header has a name and nothing else, so
every other column is a `VoidCell`: nineteen lines, almost all of it an inert
edit contract answering methods it will never be asked to honour. It also
walked straight into a trap that took DOM measurement to find — a cell
rendering no text collapses to no height, so the band had to be given
`line-height` and `min-height` as a pair.

**A band drawn by agreement.** The grid owns the `<tr>`, so a full-row band is
drawn by every cell in the row agreeing to wear the same class, and eight
classes are reset on every paint to keep that honest.

**Fold in a closure.** Fold state lives in `state.folded`, a filter predicate
re-derives the visible rows, and a direct `updateCell` plus `flushNow()`
repaints the caret. It is lost on workspace reconstruction, because a widget
has no seam through which to update its own params (see
[`upstream-blockers.md`](upstream-blockers.md) §4).

---

## 4. The reframing: the header was never a row

The obvious fix is to make row kinds first-class — `DATA`, `HEADER`,
`AGGREGATE` — and let the grid own height, copy semantics, collapse and indent
per kind. That is an improvement, and it is the **wrong shape**. It keeps the
section header as a row and then has to invent a row that holds no measurement.

The better model says it was never a row.

A section header is the **caption of a relation**. The ladder is not one
relation with three kinds of row. It is a *stack* of relations that happen to
share a schema:

| the ladder, re-read | |
|---|---|
| EURUSD detail | a relation, 3 rows keyed by tenor |
| EURUSD subtotal | a relation, 1 row |
| USDJPY detail | a relation, 2 rows |
| USDJPY subtotal | a relation, 1 row |
| … | … |
| FXO book total | a relation, 1 row |

Every one of them is over the same column schema. The differences that used to
be encoded per row — aggregate or not, indented or not, copyable or not,
editable or not — are properties **of the relation**, declared once.

This is why the model deletes rather than renames. `VoidCell` does not get a
better name; it stops existing, and with it the inert contract, the zero-height
trap, the eight-class reset and the band-by-agreement trick. That was the
largest of the four blocks.

---

## 5. Homology belongs in the type system

The remaining question is what makes a list of relations *displayable
together*. The answer is not a runtime validation that returns an error. It is
a type.

Same `T` means:

- the same set of columns
- the same primary-key type
- the same headers

`List<Relation<T>>` states all three, and the compiler checks them at the point
of authorship. A mis-declared stack does not fail in a browser, or in the
sweep, or in a conformance run. It does not compile.

This is idiomatic here rather than novel. Typed CSS is already a Java
declaration that generates styling and arrives in served JavaScript as checked
names; module exports are declared as Java records. A relation whose columns,
headers and key type come from `T` is the same move applied to data instead of
presentation. If `T` is a record, its components supply the names and types
mechanically, which is how the repository models domain shapes everywhere else.

### 5.1 The discriminator disappears

| a cell value carries today | belongs to |
|---|---|
| `kind`, `n`, `text`, `state` | the cell |
| `agg`, `rowKind`, `indent`, `scope`, `leaf` | **the relation** |

Five of nine fields stop travelling per cell. What survives is the desk's
per-cell verdict — the half that was always a real judgement.

### 5.2 What else falls out

**Ordering becomes structural.** A subtotal is a one-row relation placed after
its detail relation. The seventeen lines encoding "aggregates last within their
scope" have nothing left to encode.

**Indent is a relation's depth,** not a boolean threaded through every value.

**Fold is collapsing a relation:** hide its rows, keep its caption and its
subtotal relation. It becomes grid view state rather than a closure and a
filter predicate — which is also what would let it survive the `params()` seam.

**Policies are per relation.** Read-only and copy semantics become
declarations, and per relation is *more* expressive than per grid: a detail
relation may well be editable in another widget while an aggregate relation
never is.

**Keys need only be unique within their relation.** The synthetic ones the
ladder mints to dodge collisions — `pair + '/Σ'`, `'ΣΣ'` — disappear, and
EURUSD and GBPUSD can both hold a row called `3M`.

---

## 6. Sorting

Today sorting is **one flat pass over a single global key list**.
`GridViewStateModule.applyRowView()` filters `basePks()` and stable-sorts the
survivors. There is no boundary it could respect, so a sort on the ladder would
scatter captions through the table and strand subtotals beside rows they do not
total. Sortability is currently a per-*column*, grid-wide predicate: the wrong
axis for this.

The rule:

> **Sorting never crosses a relation.**

Scoped that way, "within a single relation" is the only thing sorting *can*
mean, and sortability becomes per relation, per column. A ladder then declares
its relations unsortable — typically the right answer for a ladder, whose row
order *is* the curve — and the pathological case is unreachable by
construction rather than merely unwired.

---

## 7. Why this must be the framework's

**"Shared column controls" is the load-bearing half of the definition.**

A desk can already stack several grids down a pane today. Nothing prevents it.
What it *cannot* do is make their widths, order, hidden set and horizontal
scroll agree, because that means reaching into each grid's layout — precisely
the reach into framework internals the rule set exists to forbid, and the same
complaint this repository already files against `SplitPaneModule` (§3 of the
blockers).

Captions and per-relation policies could in principle be domain code. **Column
agreement cannot.** It is the irreducible framework contribution, and it is why
this is a facility rather than a pattern to document.

### 7.1 The positive form of the virtualization argument

[`virtualization-pseudo-requirement.md`](virtualization-pseudo-requirement.md)
concluded that users scroll enormous tables because the domain never gave them
a breakdown mechanism — that virtualization answers a requirement on a
mechanism standing in for a requirement on an outcome.

This is the mechanism. Twelve relations of five rows each, with their
subtotals, is what makes fifty thousand rows *unnecessary* rather than merely
slow. The two documents are the same argument from opposite ends.

---

## 8. The line the facility must not cross

> **The grid may own the SHAPE of an aggregate relation. It must never own the
> NUMBER in it.**

The moment the API offers `aggregate: 'sum'`, the grid is computing risk. This
repository spent a commit removing exactly that (backlog #3, *the desk judges,
the UI renders*; P5 in the UI Requirements Study). The desk supplies every
value, totals included, and the ladder's own subtitle says so to the user.

This belongs in the RFC text itself, not in a document explaining it
afterwards.

There is a second, subtler version of the same hazard. **`T` gives you shape,
not truth.** Nothing about the type stops a desk publishing a subtotal that
does not match the rows above it. Type discipline and P5 answer different
questions, and the risk is feeling covered by the first and quietly relaxing
the second.

A related caution on scope: a *general* grouping engine is the wrong answer and
the easy one to drift into. AG Grid's row grouping is large, configuration-
driven, and it computes. Shipping that here would import, under a different
name, the failure the virtualization study identified. **The facility should
render structure the desk has already decided. It must not decide structure.**

---

## 9. API delta

| | today | proposed |
|---|---|---|
| unit of display | one relation | ordered `List<Relation<T>>` |
| homology | n/a | compile-time, one `T` |
| cell addressing | `(pk, col)` | `(relationId, pk, col)` — **the breaking change** |
| adapter | one | one per relation |
| sortable | per column, grid-wide | per relation, per column |
| read-only / copy policy | per grid | per relation |
| caption, depth | none | per relation |
| nesting | n/a | flat list + depth per relation, **not** a tree |

Relation-qualified addressing is the real cost, and it is better taken
deliberately than discovered halfway through.

On nesting: a region layer above pairs would be three levels deep. Keeping the
container a flat list and giving each relation a **depth** gives arbitrary
nesting without importing a tree type and everything that follows from one.
Indentation is then a relation property, which is where it belonged anyway.

---

## 10. What this does not fix

Two costs from the same build are **defects, not missing features**, and should
not be absorbed here. A facility that swallows defects makes them permanent and
invisible.

1. **The animation-frame batching leak.** `updateCell` batches on
   `requestAnimationFrame`, which never fires in a hidden tab, so a fold left
   the caret pointing the wrong way until the pane came forward. `flushNow()`
   is domain code compensating for a leaky abstraction. Fix the abstraction.

2. **The zero-height cell contract.** A cell rendering no text collapsing to no
   height is a bug in the cell contract. It evaporates here only because void
   cells stop existing — luck, not a fix.

---

## 11. Edges to settle before it is written

- **What exactly is homologous?** Identical column schema, with a refusal
  otherwise. The ladder's book total showing `—` for columns the desk does not
  publish is a *null in a present column*, which the strict reading handles and
  which stays a domain decision.
- **Selection and cursor across a boundary.** Navigation should cross freely.
  Copy should emit relation by relation, with each relation's policy applied.
- **An empty relation.** Does its caption still show? Probably the desk's call,
  since the desk supplies the list.
- **Captions on a single-relation grid.** Harmless, and free once a relation
  can carry one.

---

## 12. Caveat on the evidence

This is **one consumer**. Some of the 98 lines are the ladder's taste rather
than structure, and a second consumer would separate them.
`TradeBlotterWidget` and `PortfolioWidget` are the candidates; neither has been
tried.

But height, copy semantics and the edit contract follow from a non-data row
**existing at all**, not from anything specific to ladders. Those three need no
second witness.

---

## 13. Acceptance

The ladder rebuilt on the facility:

- on the order of **85 of its 98 structural lines gone**, what remains being a
  declaration of which relations exist and what each one is;
- the schema — columns, labels, key — sourced from `T` rather than from quoted
  JavaScript, which also dents [`backlog.md`](backlog.md) item #1;
- the conformance report unchanged;
- the runtime sweep green.

---

## 14. On the name

`RelationGrid` currently displays a single relation, which made the name
slightly grander than the thing it named.

*A list of relations over one `T`, with shared column controls*, is what the
name was always describing.

---

*Registered as [`backlog.md`](backlog.md) #14 and
[`upstream-blockers.md`](upstream-blockers.md) §5. Companion to
[`relation-grid-risk-ladder.md`](relation-grid-risk-ladder.md), which is the
build this proposal is measured from.*
