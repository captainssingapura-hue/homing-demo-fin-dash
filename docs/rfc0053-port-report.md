# RFC 0053 "One Tree" — port report from fin-dash

**Against:** `docs/rfc0053-migration-brief.md` (2026-09-02) and the DRAFT guide it
summarises.
**Result:** ported, green, and working — `homing.core.version` is now `0.8.0`.
**Elapsed:** ~11 minutes, of which the tree substrate itself was about three.

The brief asked to be told what was wrong. Three things were, and two of them
would have shipped broken rather than failing the build.

---

## 1. The blast radius is not two files

> "Blast radius — exactly two files … Nothing else in fin-dash touches the tree
> substrate. Widgets, workspace specs, parties and secretaries are **unaffected**
> — the catalogue-authoring surface did not move."

The two named actions were correct as far as the *tree substrate* goes. But the
release carries two further breaks that the brief does not mention, and fin-dash
needed **eight** files changed plus two new ones:

| File | Why |
|---|---|
| `PortfoliosGetAction` | the port, as briefed |
| `DataTypesGetAction` | the port, as briefed |
| `PortfolioNodeIdentity` *(new)* | identity type |
| `DataTypeNodeIdentity` *(new)* | identity type |
| `PortfolioTreeWidget` | **selection broke — see §3** |
| `DataTypeTreeWidget` | **selection broke — see §3** |
| `FinDashDocCatalogue` | `Entry.of(C, Doc)` is gone — see §2 |
| `WorkspaceIntrosCatalogue` | same |
| `HtmlResourceDoc` | `Doc.url()` is gone — see §2 |
| `pom.xml` | the version |

The catalogue-authoring surface **did** move.

## 2. Two more compiler-caught breaks, unmentioned

Both are fine to hit — they fail the build loudly — but a reader who trusts
"exactly two files" will plan the wrong size of job.

**`Entry.of(catalogue, doc)` no longer exists.** The Doc-bearing overloads are
now `of(C, M, P, Doc)` and `of(C, M, P, Doc, NodeName)`: the catalogue names the
viewer app and its params. The fix follows the framework's own current pattern:

```java
Entry.of(this, DocReader.INSTANCE,
        new DocReader.Params(UiStudyDoc.INSTANCE.uuid().toString()),
        UiStudyDoc.INSTANCE)
```

Note the framework javadoc is stale here — `CatalogueIllustration`'s class comment
still shows `Entry.of(this, SomeDoc.INSTANCE)`, which no longer compiles.

**`Doc.url()` was deleted** (`d7d1251` — *"a doc no longer knows how it is
viewed"*). Any downstream `Doc` sub-interface overriding it fails with *"method
does not override or implement a method from a supertype"*. Ours was
`HtmlResourceDoc`. This is the same idea as the tree change — the doc stops
carrying its own presentation — so it belongs in the brief as a sibling of it,
not as a surprise.

## 3. The widget half of the smuggling is not automatic — and it is silent

This is the important one, and the brief points straight past it.

The brief correctly identifies that both actions smuggled the machine key through
the `Summary` display slot, and calls that "the lucky part" because the value is
already isolated. True — but **something was reading it at the other end**:

```js
var id = sel.summary;             // the node's machine field
var meta = index[id];
if (!meta) return;                // ← now always taken
```

After the port `summary` is fed by `RowDisplay.note`, so it returns `"39
positions"`, the index lookup misses, and `broadcast` returns early. The tree
still draws, the rows still highlight, and **selection silently stops driving
anything** — no error, nothing in the console. Both tree widgets had this.

The replacement is `sel.namePath` (the RFC 0053 address). Two details that cost
time and belong in the guide:

- `namePath` **excludes the root's own segment**, so the root selects as `''`.
  A widget that addressed the root by id needs that case handled explicitly.
- `NodeName.slug()` **lower-cases**. The ontology's ids are camelCase (`PairId`),
  so the segment is `pairid` and a direct `index[segment]` lookup misses. We fold
  case through a `segment → id` map built from the index. Anyone whose ids are
  not already lower-case will hit this.

So the guide's §4 table should gain a row for the *consumer* side: if a node's
machine field was read off `summary`, it now comes from `namePath`, and that is a
code change in the widget, not in the action.

## 4. Confirmed as briefed

- `0.8.0` present in the local repo, and it is the whole story — no snapshot needed.
- `NormalizedNode(level, NodeName, NodeIdentity, Map, children)` and
  `leaf(level, NodeName, NodeIdentity, Map)` exactly as documented.
- Retired types (`Summary`, `Category`, `Kind`, `CategoryValue`, `KindValue`) are
  gone from the jar, so every action-side change is compiler-caught.
- `DimensionKey` does survive; deleting the import wholesale is indeed wrong.
- The **one-arg `write(root)` silent failure is real** and worth the warning it
  gets. Both our actions called it.
- The "build clean" instruction earned itself: our first `clean install` is what
  surfaced the `fin-dash-studio` breaks in §2.

## 5. One correction to §5's own advice

> "The temptation is to store `"12 positions"`. That is data you can no longer
> count with."

Right in principle, and we kept every count an `int` until `row()`. But the
brief's worked example puts the count in `RowDisplay.note`, and for a navigator
tree **the note is not drawn**: `TreeRenderer` gates it behind `showNote`, which
defaults to false — *"Both off by default, so the workspace navigator trees are
untouched."* Following the example literally would have silently dropped
`(39 pos)` and `(52 types)` from the two trees.

What works is to keep the count typed in the details record and fold it into the
**label** at render time, which satisfies the principle without changing the UI:

```java
record PortfolioDetails(String label, String kind, int positions) {
    RowDisplay row() {
        return new RowDisplay(label + "  (" + positions + " pos)", "",
                              positions + " positions", kind);
    }
}
```

The note is still populated, so a listing that opts into `showNote` gets it.

## 6. Verified

- `mvn clean install` from clean: **BUILD SUCCESS**, 25 tests, conformance gate
  green (0 errors, 5 warnings, baseline 5 — unchanged by the port).
- Portfolio tree: labels and counts intact; selecting a leaf narrows the blotter
  57 → 4 trades, selecting a book → 15 with the aggregated view.
- Data-ontology tree: 52 types render; selecting `PairId` produces the selection
  chip and the Type Usage pane follows — the case-folding path works.
- Both trees' JSON now carries a proper `display` block and an empty
  `dimensions` array.
