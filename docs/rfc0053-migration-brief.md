# fin-dash → Homing 0.8.0 (RFC 0053 "One Tree") — migration brief

**For:** the fin-dash agent
**Date:** 2026-09-02
**Full guide:** `/cat/releases/migrations/migrating-to-rfc-0053-one-tree` in the
Homing self-studio — source `MigrateToRfc0053Doc.java`. Read it if anything below
is ambiguous; this is the short form with fin-dash's actual code in it.

> The guide is marked **DRAFT** on purpose. It was written from fin-dash's compile
> errors but the port was never performed. **This port is what validates it.**
> If a step is wrong, that is a finding worth reporting, not a mistake to work around.

---

## 1. What to change first

`homing.core.version` in the root `pom.xml`:

```
LOCAL-SNAPSHOT   →   0.8.0
```

0.8.0 is installed in the local m2 repo (verified). It is the merge of
`feature/rfc0053_one_tree` into `main` (PR #54).

## 2. Blast radius — exactly two files

```
fin-dash-ontology/.../ontology/data/DataTypesGetAction.java    3 nodes, 4 dim puts
fin-dash-book/.../book/data/PortfoliosGetAction.java           2 nodes, 3 dim puts
```

Nothing else in fin-dash touches the tree substrate. Widgets, workspace specs,
parties and secretaries are **unaffected** — the catalogue-authoring surface did
not move.

**Build clean, not incrementally.** `mvn clean install`. fin-dash reported green
three times during development on incremental builds that never recompiled it.

## 3. Change 1 — a node carries a segment and an identity

```java
// before
new NormalizedNode(TreeLevel.L1.INSTANCE, dims(...), children)
NormalizedNode.leaf(TreeLevel.L2.INSTANCE, dims(...))

// after
new NormalizedNode(level, NodeName.slug(label), identity, Map.of(), children)
NormalizedNode.leaf(level, NodeName.slug(label), identity, Map.of())
```

- **`NodeName`** — the path segment. Charset `[A-Za-z0-9._-]`, 48 chars.
  `NodeName.slug(text)` derives one. Siblings must be unique (Law 2) or the boot fails.
- **`NodeIdentity`** — an open interface; bring your own record. Two rules:
  **intrinsically global, never positional** (derive from what the node *is*, not
  where it sits), and **equality is the contract** (it is a map key).

Mint one type per action, e.g.:

```java
public record DataTypeNodeIdentity(String kind, String value) implements NodeIdentity {
    public static DataTypeNodeIdentity stratum(String s) { return new DataTypeNodeIdentity("stratum", s); }
    public static DataTypeNodeIdentity type(String id)   { return new DataTypeNodeIdentity("type", id); }
}
```

`CrateNodeIdentity` in `homing-conformance-studio` is the in-tree worked example.

### The lucky part — your identity source already exists

Both files already smuggle the machine key through the `Summary` display slot,
and `PortfoliosGetAction` even says so:

```java
dims.put(Summary.INSTANCE, new NameValue(n.id()));   // machine field: the portfolio id
```

`DataTypesGetAction` does the same — its `dims(label, summary, kind)` helper is
called with `t.id().value()` and `"stratum:" + s.name()` in the summary position.

**So the second argument of `dims(...)` is your identity, already isolated.**
That is exactly the anti-pattern RFC 0053 retired — a display slot used as a data
channel because no other channel existed — and it means the port is mechanical
rather than a redesign.

## 4. Change 2 — dimensions retire in favour of a resolved row

| was | now |
|---|---|
| `DisplayLabel` dimension | `RowDisplay.label` |
| `Summary` dimension **(deleted)** | `RowDisplay.note` — but yours is really the identity |
| `Category` dimension **(deleted)** | `RowDisplay.badge` |
| `Kind` dimension **(deleted)** | `RowDisplay.kind` |
| `CategoryValue`, `KindValue` **(deleted types)** | none — `RowDisplay` is four strings |
| `Map<DimensionKey, DimensionValue>` on the node | `Map.of()` — display leaves the node |

`DimensionKey` itself survives (it still carries `DisplayLabel` and `NodeKey`), so
do not delete the import wholesale — delete the *members* you no longer use.

Model what the node **is** in your own type, render at the edge:

```java
sealed interface DataTypeDetails {
    RowDisplay row();
    record OfStratum(String label, int count) implements DataTypeDetails {
        public RowDisplay row() { return new RowDisplay(label, "", count + " types", "stratum"); }
    }
    record OfType(String label, String marker, int usages) implements DataTypeDetails {
        public RowDisplay row() { return new RowDisplay(label, "", usages + " usages", marker); }
    }
}
```

Then fill the details map and the tree in **one walk**, and hand the writer a
projection instead of letting it read the node:

```java
var details = new LinkedHashMap<NodeIdentity, DataTypeDetails>();
NormalizedNode root = root(details);
RowDisplaySource rows = node -> node instanceof NormalizedNode n && details.get(n.identity()) != null
        ? details.get(n.identity()).row()
        : null;
writer.write(root, rows);      // NOTE: the two-arg overload
```

`TreeNodeJsonWriter.write(root)` still exists and still compiles — it just emits
no display block, so **the tree will render unlabelled if you forget the second
argument.** That is the one silent failure in this migration; everything else is
compiler-caught.

## 5. Two things to get right while you are in there

**Keep counts as `int` and markers as enums.** The temptation is to store
`"12 positions"`. That is data you can no longer count with, and it is precisely
the mistake the dimension vocabulary made. When the conformance studio was ported,
typing the fields immediately fixed a root node that had always read *"1 crates"*.

**`positionCount` / `usages().size()` stay numbers** until `row()` renders them.

## 6. Report back

The guide moves from DRAFT to validated on the strength of this port. Worth
reporting either way:

- any step that was wrong, missing, or in the wrong order;
- anything the guide claims is compiler-caught that was not;
- how long it actually took, against the guide's implied "mechanical".

Post-port, a clean-build sweep of downstreams is being added to the release
checklist — this port is the first data point for it.
