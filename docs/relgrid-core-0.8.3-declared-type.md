# homing-rel-grid 0.1.0 does not boot on homing core 0.8.3

*Found 2026-09-15 bumping fin-dash from core 0.8.2 to 0.8.3 on the branch that
carries the group ladder. The reactor's own defect, not fin-dash's; fin-dash
declares no mechanical type anywhere.*

## Observed

With `homing.core.version` at 0.8.3 and `relgrid.version` at 0.1.0, the desk
refuses to start, before a page is served:

```
java.lang.IllegalArgumentException:
  'hue.captains.singapura.js.homing.relgrid.group.RelGridGroupStyles' declares
  the mechanical type GENERATED_CSS. A mechanical type is inferred from the
  module's form, never declared: declaring one would exempt a first-party
  module from its discipline. Declare a domain role (PRIMITIVE, SECRETARY,
  PURE_LOGIC) or leave it undeclared.
    at hue.captains.singapura.js.homing.core.CrateEntry.<init> (CrateEntry.java:37)
    at hue.captains.singapura.js.homing.core.CrateEntry.of (CrateEntry.java:60)
    at hue.captains.singapura.js.homing.relgrid.group.RelGridGroupCrate.entries (RelGridGroupCrate.java:32)
    at hue.captains.singapura.js.homing.findash.studio.FinDashFixtures.servableModuleClasses (FinDashFixtures.java:186)
```

The same refusal fails `FinDashConformanceTest` at `mvn test`, so the build is
red as well as the boot.

## Cause

Core 0.8.3 changed the rule: a **mechanical** module type (`GENERATED_CSS` among
them) is inferred from the module's form and may no longer be declared on a
`CrateEntry`. The reactor, cut against 0.8.2, declares it three times:

| file | line | module |
|---|---|---|
| `rel-grid-group/…/relgrid/group/RelGridGroupCrate.java` | 32 | `RelGridGroupStyles` |
| `rel-grid/…/relgrid/RelGridCrate.java` | 40 | `RelGridStyles` |
| `rel-grid/…/relgrid/RelGridCrate.java` | 41 | `RelGridStockStyles` |

Each is `CrateEntry.of(X.INSTANCE, StandardJsModuleType.GENERATED_CSS)`. The boot
stops on the first one walked, which is why only `RelGridGroupStyles` is named;
the other two are the same shape and fail next.

## Fix, reactor side

Drop the second argument from the three calls — `CrateEntry.of(X.INSTANCE)` —
and cut against core 0.8.3. Nothing else in the reactor was touched by the bump:
with those three lines changed, fin-dash on 0.8.3 compiles, converges and passes
conformance (65 modules, 0 errors) up to exactly this point.

## What fin-dash did meanwhile

Two things 0.8.3 changed that were fin-dash's to absorb, both done and held in a
stash on `feature/ladder-on-group` until a reactor cut exists:

- **`CssGroup.cssImports()` is gone** from the interface (replaced by the
  inherited `imports()`, plus a new `prior()`). All seven fin-dash CSS groups
  overrode it with an empty list and failed to compile with *"method does not
  override"*. The seven overrides are removed; none imported another group.
- **The reactor drags `homing-core` and `homing-codec` at 0.8.2** transitively,
  which the enforcer's convergence rule refused against 0.8.3. Both are now
  managed at `${homing.core.version}` in the root pom — the same move as
  Jackson, for the same reason.

Nothing was committed in that state, since its own build fails on the item
above. The tree is back on core 0.8.2 and green.

## Also unblocked by 0.8.3

0.8.3 carries `WorkspaceGroupApp` and the `group()` → `section()` rename, so the
parked RFC 0058 branch (`feature/rfc0058_workspace_group`) can come off
`LOCAL-SNAPSHOT` onto a release. It needs the same seven `cssImports()`
removals and nothing from the reactor, so it is not blocked by this.
