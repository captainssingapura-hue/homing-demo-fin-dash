# Plan — paying down the reference-implementation debt

The register is [`backlog.md`](backlog.md) (items 2–11). This is the order
they are worked in, what each step touches, how it is verified, and where
things stand. One item per commit, or one commit per mechanical pass; every
step ends with a clean build, the conformance report unchanged or improved,
and the runtime sweep green on every widget the step touched.

## Order, and why

| step | item | why here |
|---|---|---|
| 1 | #2 swallowed exceptions | smallest change with the highest copy risk; makes every later diff cleaner |
| 2 | #3 risk judgement in the UI | the P5 breach in the widgets a desk copies first; changes the JSON contract, so it goes before the fetch helper is written against it |
| 3 | #8 baseline regeneration | tiny; removes a documented footgun before anything else regenerates the baseline |
| 4 | #5 actor identity | a kit helper and twenty mechanical call sites |
| 5 | #4 fetch and failure per widget | a kit helper and twenty-seven mechanical call sites; done after #3 so the helper sees the final contract |
| 6 | #7 stubs and `onCopy` | closes the case study's honest gaps once the ladder's data path is settled by steps 2 and 5 |
| 7 | #6 the runtime sweep, automated | the largest item and the one that protects all the others; last so it lands on finished code |
| — | #9 standing baseline | upstream; tracked, not worked here |
| — | #10 view state, #11 three.js | after the above, or as someone's spare afternoon |

## Steps

### Step 1 — #2 swallowed exceptions

- Enumerate all 25 `catch (e) {}` sites; for each, name what could throw and
  why the call is on that path at all.
- Replace each with a guard that makes the throw impossible (`if (grid)`,
  a once-only `partyDeregister`), or with a real handler where the failure
  is a legitimate state.
- Add `no-empty-catch` to the desk's own conformance rules so the count
  cannot rise.
- **Verify:** `grep -c 'catch (e) {\s*}'` over `src/main` is 0; the rule is
  in the report; sweep on the 8 widgets touched.

### Step 2 — #3 risk judgement in the UI

- One tolerance table in `fin-dash-core` (freshness budget, reval budget
  bands) as desk facts, with the source of each number stated.
- `BookGetAction`, `PositionsGetAction`, `PlatformGetAction` publish
  `freshState` / `budgetState` beside the raw numbers.
- `RiskLadderWidget`, `RiskBlotterWidget`, `PortfolioWidget`,
  `PlatformConsoleWidget` render the state and stop comparing.
- Update the RelationGrid case study §9 (the gap is closed) and its §5
  paragraph on the adapter's verdict (it now relays, not decides).
- **Verify:** no numeric threshold in served widget JS; the four widgets
  show the same states as before on the deterministic book; sweep.

### Step 3 — #8 baseline regeneration

- Found on reading the source: `BaselineRegen` **already** writes the file
  itself, UTF-8, with a summary — the register's evidence was wrong. What was
  stale was KT.md §9.3, which still told the reader to "have a probe write
  fingerprints" as though the class did not exist, and led with the hazard
  rather than the procedure.
- KT.md §9.3 rewritten: the command first, the hazard as the reason it
  exists, the verification tip kept for the case where anything else touches
  the file.
- **Verify:** the documented command is the one the class's own javadoc
  gives; build unaffected (docs only).

### Step 4 — #5 actor identity

- Establish what `workspaceCtx` can supply as a stable instance identity;
  if nothing, a per-kind counter in the kit.
- `fdk.actorId(kind)` in the kit; twenty call sites.
- Widen `DeterministicRiskRule` (or add a sibling) so `Math.random` is
  banned in all served code, not only risk models.
- **Verify:** `Math.random` absent from `src/main` served JS; the bus still
  routes (blotter ↔ pricer ↔ ladder); sweep across all 20 widgets.

### Step 5 — #4 fetch and failure handling

- `fdk.load(branch, host, url, paint)`: HTTP status policy, JSON policy, the
  stale-response guard by construction (the branch is the generation
  token), one failure rendering.
- Twenty-seven call sites.
- **Verify:** no `fetch(` in a widget body; failure copy identical; the
  `LifecycleWidget` rapid-navigation case still correct; sweep on all 27.

### Step 6 — #7 stubs and `onCopy`

- Supply `onCopy` to the ladder's grid; the copy guard becomes reachable.
- `subscribe` over a fixture feed (poll the deterministic book with a
  slice advance); `update` / `deleteRows` refuse loudly on a read-only
  adapter.
- Update the case study §8 and §9.
- **Verify:** `Ctrl+C` copies with aggregates blank; the ladder ticks.

### Step 7 — #6 the runtime sweep

- Done as `WidgetMountSweepTest` + `ActionsShapeTest` (KT.md §7e): every
  widget's *served* module run under GraalJS against a browser stub, with
  `fetch` routed to the real actions; assertions on the error root, load
  failures, non-200 fetches, console errors, actors that did not leave, and
  intervals left scheduled; a canary duplicate-name widget that must be
  reported. Every `/fx/*` action answers a non-empty JSON object.
- Not a headless browser: GraalJS with the conformance engine's own renderer,
  so the sweep runs the artifact in-process in ~3 s and needs no new runtime.
- The click-everything half is not automated; it is item #12 on the same
  harness. There was no manual protocol in KT.md to delete — it lived in
  session notes — so §7e is new.
- Found on the way: the Jackson version split (item #13, fixed in the root
  pom).
- **Verify:** the sweep is a build step (part of `mvn install`) and the
  canary fails it when the detection is broken.

## Status

| step | item | state | commit |
|---|---|---|---|
| 1 | #2 swallowed exceptions | **done** | "Debt #2: no swallowed exceptions" |
| 2 | #3 risk judgement in the UI | **done** | "Debt #3: the desk judges, the UI renders" |
| 3 | #8 baseline regeneration | **done** (docs; the class already existed) | "Debt #8: the baseline procedure is the class, not the redirect" |
| 4 | #5 actor identity | **done** | "Debt #5: an identity comes from the workspace, not from chance" |
| 5 | #4 fetch and failure | **done** | "Debt #4: one data path — fdk.load" |
| 6 | #7 stubs and `onCopy` | **done** | "Debt #7: the ladder's contract is real — copy, feed, refusal" |
| 7 | #6 runtime sweep | **done** (mount half + actions; click half → #12) | "Debt #6: the sweep is a build step" |
