# Defect — `/app-refs` resolves one app id to an arbitrary registration

**Component:** `homing-studio-base`
**Severity: medium.** Nothing breaks or throws; the chrome confidently
displays the wrong page identity. Cosmetic in mechanism, misleading in effect.
**Found:** taking `homing-demo-fin-dash` through a twelve-workspace runtime sweep.

---

## Symptom

Every workspace page shows a breadcrumb naming **a different workspace than the
one on screen**. Observed in the FX Options Desk demo: the header reads

```
FX Options Desk / 🖥 Workspaces / Model Governance
```

while the workspace immediately below it reads `Middle Office :: default`. The
name is stable within a server run and changes between runs, which is what made
it look random — over one session the same page was labelled Model Governance,
Platform Operations, Quant Lab, Trader Desk and Data Ontology.

The browser tab carries the same wrong name (`Quant Lab · FX Options Desk` on a
middle-office page). Very likely the same cause; not traced end to end.

## Reproduction

Thirteen catalogue entries share the app id `genericWorkspace`, differing only
by `ws_kind`. The endpoint ignores the discriminator:

```
GET /app-refs?app=genericWorkspace&ws_kind=trader        -> "title":"Model Governance"
GET /app-refs?app=genericWorkspace&ws_kind=risk          -> "title":"Model Governance"
GET /app-refs?app=genericWorkspace&ws_kind=audit         -> "title":"Model Governance"
GET /app-refs?app=genericWorkspace&ws_kind=middle-office -> "title":"Model Governance"
```

The answer is invariant under `ws_kind`, and `ws_kind` never reaches the server
anyway — `StandardMPA.java:325` builds the request from the app id alone:

```java
crumbUrl = '/app-refs?app=' + encodeURIComponent(sp.get('app') || '');
```

## Cause

`AppRefsGetAction.indexAppDocs` (line 66) keys the lookup on the app's simple
name and drops every later registration on the floor:

```java
out.putIfAbsent(ad.nav().app().simpleName(), ad.uuid());   // line 70
```

Thirteen distinct `AppDoc`s collapse to one entry, chosen by `DocRegistry`
iteration order. `execute` then resolves that arbitrary winner (line 93), and
`AppDoc.title()` returns `nav.name()` — the winner's display name, which the
renderer appends as the final crumb.

## Why this is a defect and not the documented trade-off

The method's javadoc already anticipates a collision and accepts first-wins:

> When two AppDocs wrap the same AppModule class (multi-catalogue placement of
> the same Navigable), the first one wins — acceptable since AppDoc equality
> collapses identical Navigables, and distinct display framings would point at
> the same breadcrumb source.

That reasoning is sound **for the case it names**: the *same* `Navigable` placed
in several catalogues. Those really are interchangeable, so picking either is
harmless.

It does not hold for what `GenericWorkspace` does. Thirteen *different*
`Navigable`s share one `AppModule` class, each with its own `name` — "Trader
Desk", "Model Governance", "Middle Office". They are not interchangeable, and
the code has no way to notice the difference: `putIfAbsent` discards the losers
silently, so an assumption that stopped being true produces no signal.

The premise was correct when written. Parameterised apps arrived later.

## What needs fixing

**Minimum — stop guessing.** Detect the collision instead of swallowing it:
collect the simple names that wrap more than one distinct `AppDoc`, and for
those serve the `AppModule`'s own `title()` as the final crumb rather than an
arbitrary registration's name. `GenericWorkspace.title()` already returns
`"Workspace"`, so the crumb becomes a stable *FX Options Desk / 🖥 Workspaces /
Workspace*. Roughly 25 lines across `AppRefsGetAction` and a
`DocRefsGetAction.serialize` overload taking a title override. No client change,
no downstream change.

Note `AppModule.title()` is documented as the generated page's title (browser
tab + scaffold), so it is doing double duty. If the crumb should ever differ
from the tab, a `crumbLabel()` on `AppModule` defaulting to `title()` separates
them at the cost of touching the interface.

**Worth doing regardless — make the condition audible.** A boot-time warning
when two `AppDoc`s share a simple name but differ in params. That is exactly the
state that yields a wrong crumb, and it would have surfaced this the day the
second workspace was registered rather than in a screenshot months later.

**Not proposed:** threading `ws_kind` through to the endpoint so the crumb names
the actual workspace. It is achievable — `Navigable.url()` already encodes the
params, so the discriminator exists — but for an app that switches workspaces
in place, the URL parameter is app state rather than page identity, and the
navigation trail does not need to track it.

## Scope beyond workspaces

Nothing about the fix is workspace-specific. The rule is: *an app class
registered N times with different params has no single navigational identity, so
fall back to the app's own name.* Any future app with in-app switching inherits
correct behaviour, and resolution stops depending on registration order.
