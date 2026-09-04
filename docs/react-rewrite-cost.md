# The desk in React: a cold-start comparison

**A study, and a protocol.** The previous two studies costed one widget
against AG Grid and took one grid feature apart. This one asks the whole
question — and finds that the obvious way to ask it, *what would a rewrite
cost?*, is the wrong way. A rewrite prices translation, and nobody would ever
do the translation. The comparison that would mean something is a **cold
start**: the same requirements, two teams, two stacks, neither having seen the
other's answer.

So this study does three things. It prices the rewrite anyway, as a bound
(§3). It designs the cold start, as the experiment (§4). And it states what it
expects, as hypotheses written so they can be wrong (§4, §7).

---

## 1. What "the demo" is

Measured from the repository on 2026-09-04, so the costing is of a thing and
not an impression of one:

| | |
|---|---|
| workspaces (one per persona) | 13 |
| widgets | 41 |
| served JS modules beyond the widgets | 17 (8 authored in Java, 9 as co-located `.js`) |
| typed CSS classes, in groups | 82, in 7 |
| data actions (server endpoints) | 21 |
| themes | 10, one desk-authored |
| conformance rules the build enforces | 12 |
| docs served from the repo | 9 |
| Java, main / test | ≈16,200 / ≈700 lines |
| JavaScript | ≈4,600 lines (≈3,650 authored as Java string arrays, ≈930 co-located) |
| history | 76 commits over 25 calendar days |

And what the framework underneath supplies, none of which the demo wrote: the
workspace shell (panes, splits, tabs, persistence, the RFC 0048 focus model),
the party bus and secretaries, the tree views, the relation grid, the studio
catalogue and doc readers, the theme system, and the conformance engine with
its baseline ratchet.

The build resolves **65 runtime artifacts** for the studio module — the whole
server, HTTP stack included — of which 41 are the framework's, 17 are the
desk's, and the remaining two dozen are third-party in seven groups (Jackson,
Vert.x, Netty, and two small libraries from the same author as the framework).

## 2. What React is, and what it leaves you to source

React is a view library. It replaces exactly one layer of this demo: the
authoring of DOM-owning modules and the party that composes them. Everything
else the framework supplies has to be sourced from somewhere, and in the React
ecosystem "somewhere" is a package:

| this demo has | from | a React build needs | typical source |
|---|---|---|---|
| widgets that own named DOM | `DomModule` + `DomOpsParty` | components | React |
| panes, splits, tabs, persistence | workspace shell | a docking layout | `dockview`, `flexlayout-react` |
| shallow / deep focus (RFC 0048) | shell | reimplemented on the layout's API | hand-written |
| party bus, secretaries | core | a store with message semantics | `zustand`, `redux` |
| tree views | `homing-tree-views` | a tree | `react-arborist` |
| grid | `RelationGrid` | a grid | AG Grid, TanStack |
| typed CSS classes | `CssGroup` / `CssClass` | class typing | CSS Modules + d.ts, `vanilla-extract` |
| ten themes as CSS variables | theme system | the same variables | unchanged — the one free transfer |
| catalogue, doc readers | studio base | a router and a renderer | `react-router`, `react-markdown`, `remark-gfm`, `mermaid` |
| 12 conformance rules over the *served artifact* | conformance engine | rules over *source* | ESLint + custom rules; partial |
| the runtime sweep | a browser and a protocol | end-to-end tests | Playwright |
| one build | Maven | two builds | Maven + Vite, and a plugin to marry them |

The data layer — 21 actions, the deterministic book, the lifecycle model — is
Java and stays Java. React changes nothing there except that the JSON
contract, which today is an internal detail, becomes an API surface.

## 3. The wrong comparison: a rewrite, line by line

It is still worth pricing, because it bounds the answer. Developer-days for
someone fluent in the React ecosystem, given the Java server as it stands:

| work | days | note |
|---|---|---|
| 41 widgets as components | 40–80 | one to two each; the pricer and calibration lab are the expensive ones, the fact panels the cheap ones |
| docking shell on `dockview` | 5–8 | panes, splits, tabs, persistence to IndexedDB |
| the focus model on top of it | 3–5 | shallow vs deep entry, `setActive`, keyboard scoping — none of which a layout library has an opinion about |
| bus → store | 2–3 | `DeskSecretary` is already a pure `(state, envelope) → Step`; it transfers as a reducer almost verbatim |
| tree views | 2–3 | |
| the ladder and blotters on a grid | 4–6 | costed in the RelationGrid study |
| 82 classes → CSS Modules | 3–5 | typing survives via generated declarations; the *gate* does not |
| themes | 1–2 | the variables are runtime CSS and carry over |
| catalogue, docs, mermaid | 5–8 | |
| conformance as ESLint | 5, partial | line limits, CDN imports, inline styles and literal colours have analogues; crate integrity, DOM ownership and the baseline ratchet do not |
| runtime sweep as Playwright | 5 | |
| Vite, and marrying it to Maven | 3–5, then forever | `frontend-maven-plugin` or a split repository; either way a second CI lane |
| **total** | **≈ 80–135** | four to six months of one developer, or two to three of a pair |

Two honesty notes on the number. First, the current demo's 25 calendar days
of commits are not comparable developer-days — the framework was being
finished alongside it, by its own author, with the desk as the test case — so
the table is a costing of *what must be built*, not a race. Second, the
estimate assumes competent humans and no tooling advantage on either side;
whatever accelerates one column accelerates the other.

And the larger problem, which is why this is the wrong comparison: a rewrite
carries this desk's design over intact — its row kinds, its party, its
boundary between what the server publishes and what the UI shows — and prices
only the *translation* of it into another vocabulary. But that design is the
thing in question. A React team starting from the same papers would not
arrive at this design; they would arrive at theirs, and the interesting
differences are exactly the ones a translation preserves. The 80–135 days is
what it would cost to get a React app that is secretly this one. It is a
ceiling on the wrong quantity.

## 4. The right comparison: a cold start

**The requirement set.** The papers this desk was built from, and nothing
else: the UI study with its five principles, the engine architecture, the
demo-data requirements, and the user guide read as acceptance criteria —
thirteen workspaces, each with its intro naming who it is for and what they
do there. No access to this repository. The papers are the specification; the
desk is one answer to it, and the experiment asks for another.

**Two cold starts, not one.** A React team builds it on whatever backend they
are fluent in — Node, Spring, .NET; the choice is theirs and is part of the
result. And, because this is what makes it fair, a *homing* team that has
never seen homing builds it too. The 25 calendar days in §1 are not a cold
start: the framework's author built the desk as the framework's test case,
finishing the one alongside the other. Whatever that was worth, a cold start
removes it from both sides rather than pretending it was not there.

**Rules.** Same clock. Same AI-assistance policy on both sides, whichever it
is. No framework author on either team, and no author of the papers either. A
third party applies the acceptance, workspace by workspace, against the user
guide; partial credit is reported per workspace and never averaged into a
score.

**What is measured.**

| dimension | how |
|---|---|
| time | calendar days and developer-days to each workspace's acceptance |
| size | lines by language; the count of things authored — components or modules, classes, endpoints |
| dependency tree | resolved packages or artifacts from a lockfile; advisories open on acceptance day |
| build | number of build systems; a cold build from empty caches on a clean machine — the README's portability promise, applied to both |
| enforcement | which of the desk's twelve conformance rules have an *enforced* equivalent on each side, and which are policy only |
| runtime | the mount-everything, click-everything sweep, run by the judge on both |
| the boundary | P5 audited: the number of places the UI computes what the server should have published |
| ownership | who owns a cell — the domain or the framework — since that is the question both prior studies turn on |

**Hypotheses, stated so they can be wrong.**

1. The React team reaches acceptance first on the form-heavy workspaces —
   the sales pricer, the quant lab, governance — and last on the grid-heavy
   ones: the blotters, the ladder, the audit explorer.
2. The React tree is larger by an order of magnitude and has at least one
   open advisory on acceptance day — because §6's snapshot is the resting
   state of such a tree, not an anomaly in it.
3. Without a reviewer holding the papers, the P5 boundary erodes on the React
   side: totals get computed in the client because the grid offers to. The
   homing side is not immune; it is merely not offered the shortcut.
4. The homing cold start pays its first week to the framework — the party,
   the crate, the string-array authoring of §5 — and its later weeks are
   cheaper than React's. Whether the two curves cross before acceptance is
   the result most worth having.
5. On a clean machine, one side builds from source with one tool, and the
   other does not.

If the fourth is false — if the homing side never catches up — that is a
finding against the framework, and a more useful one than any rewrite costing
could produce. The experiment is worth running precisely because it can come
out that way.

## 5. What React would make better

This section exists because a study that finds nothing is not a study.

**Authoring.** Roughly 3,650 lines of this demo's JavaScript are written as
Java string arrays — `"    var x = ...",` — with escaped Unicode, no syntax
highlighting, and a line gate to stay under. The co-located `.js` path exists
and nine modules use it, but the string arrays are the majority and they are a
tax. JSX in a `.tsx` file with an editor that understands it is simply
better, and it is not close.

**The inner loop.** A change here is a clean build and a server restart, on
the order of a minute; the risk ladder took about twenty of those in one
afternoon. Vite's hot reload is under a second. Over a project that is
hours, and over a team it is the difference in how often anyone tries
something.

**State-heavy widgets.** The pricer, the calibration lab, the override
inventory — forms with derived values and validation — are what "UI as a
function of state" was invented for. They are the widgets where the current
approach is most laborious and React's would be least.

**Hiring, tooling, ecosystem.** Every React developer can read a component;
nobody has read a `DomModule` before their first day here. Devtools, testing
libraries, and answers to questions already exist.

## 6. What React would make worse

**A second build system**, argued in the RelationGrid study and not repeated,
except to say that it is not an install but a second set of failure modes,
owned forever.

**The dependency tree.** This is the measured part. Two lockfiles were
resolved on 2026-09-04 with `npm install --package-lock-only`, no scripts run:

| stack | direct | resolved packages | advisories open that day |
|---|---|---|---|
| the floor: React, React DOM, TypeScript, Vite, the React plugin | 5 | **111** | 2 (1 moderate, 1 high) |
| a fair build of §2: the floor plus AG Grid, dockview, react-arborist, zustand, react-markdown, remark-gfm, mermaid, three, ESLint and its React rules, Vitest, Testing Library, jsdom, Playwright | 21 | **552** | 5 (3 moderate, 1 high, 1 critical) |
| for comparison: the demo's *entire server*, runtime scope | — | **65** artifacts, ≈24 third-party | — |

Three things about that table. The 552 is the front end *alone*, added on top
of the same 65-artifact server; nothing is replaced. The advisory counts are a
snapshot and will be different next week, which is the point: they are not a
number you fix, they are a number you *attend to*, on every build, forever.
And the critical one was open on a stack assembled that morning from current
versions of well-maintained libraries — not a neglected tree, an ordinary one.

**The conformance discipline.** The twelve rules run over the *served
artifact* — the JavaScript the browser actually receives — because in this
architecture the module is the artifact. React compiles the artifact away: the
rules would run over source, the bundle would be what ships, and the two are
related by a toolchain with its own transforms. The baseline ratchet, which
lets a rule land before every violation is fixed, has no ESLint analogue.
Crate integrity — every module served is one the crate declares — has no
analogue at all, because the bundler is the crate.

**The ownership model.** This is the one that is not a cost but a
contradiction. `DomOpsParty` gives every element a name and an owner, and a
branch that can be dissolved whole. React's reconciler owns the DOM; that is
its premise, and it does not share. "Cells are ours" — the finding on which the
RelationGrid study and the virtualization study both rest — becomes "cells
are components", which is precisely the downstream deficit that makes a grid
need virtualization at three hundred rows. The rewrite would not merely
re-express the demo. It would remove the property the demo was built to show.

## 7. Not a dependency — a premise

Which is what the cold start would actually be testing. React is not
something this demo could *add*. It is a different answer to the question the
demo exists to answer — and the experiment in §4 asks whether that difference
shows up in the hands of people who have never heard of either premise.

The demo's premise: a UI is a set of served modules that own a DOM by name,
compose through a party, and are checked as the artifacts they are. React's
premise: a UI is a function of state, and the framework owns the DOM. One
cannot be adopted as a library inside the other; the second replaces the
first entirely. So the cost of "the demo in React" is not the 80–135 days in
§3 and not the 552 packages in §6. It is that the result would no longer be
a downstream validation case for the framework. It would be a React app with
a Java back end, of which the world has a great many.

For almost any other product, that is fine. A team hiring from the market,
building a form-heavy UI with no appetite for a bespoke framework, should use
React, pay the dependency tax as routine, and think no more about it — the
costs in §6 are the ordinary price of the ecosystem, and most of the industry
pays it without noticing. The costs are only *decisive* here because this
repository's reason to exist is the thing they would remove.

## 8. The conclusion, as a rule

**Cost a framework against what the system is for, not against what one
screen needs — and compare it cold, or not at all.** The RelationGrid study
ended with "a grid should be chosen for what it costs the system"; this one
ends one level up. A rewrite that preserves every feature and discards the
premise has a cost of one hundred percent, whatever the day count says; and a
comparison that carries one side's design over to the other has measured
nothing but translation.

This study is therefore a protocol awaiting its experiment. Until it runs, §3
is a ceiling, §6 is a forecast, and §4's hypotheses are the only claims here
that can be settled.

And the honest corollary, since §5 is real: the string-array authoring and the
one-minute loop are the demo's two genuine taxes, and neither is the premise.
Both are fixable inside it — more modules on the co-located path, a faster
serve-from-source loop — for a great deal less than four months and 552
packages. The first is now a backlog item; that is where the next effort
belongs.
