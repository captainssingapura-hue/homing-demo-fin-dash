# The desk in React: the cost of a cold start

**A study.** The comparison that would mean something is a cold start: the
same papers this desk was built from, handed to a React team who build a
workable desk on whatever backend they are fluent in, and who never see this
repository. This study is the React side of that ledger — what such a team
would pay, in which currencies, and how the experiment that settles it would
be run.

It is not a rewrite costing. A rewrite carries this desk's design over and
prices its translation; the design is the thing in question, and a team
starting from the papers would arrive at their own.

---

## 1. The size of the requirement

The papers — the UI study with its five principles, the engine architecture,
the demo-data requirements, and the user guide read as acceptance — have one
existing answer, and its size is the floor on what any answer must contain.
Measured from the repository on 2026-09-04:

| | |
|---|---|
| workspaces, one per persona | 13 |
| widgets | 41 |
| data endpoints | 21 |
| themes | 10 |
| conformance rules the build enforces | 12 |
| docs served from source | 9 |
| server-side code | ≈16,200 lines of Java |
| client-side code | ≈4,600 lines of JavaScript |

A React team pays for at least this. What they pay with is the rest of the
study.

## 2. Currency one: the stack they must source

React is a view library. Of what the papers need, it supplies the components
and nothing else. Every other capability this desk gets from its framework, a
React team sources from a package — and the choice of package is itself work,
made before a single workspace exists:

| the papers need | a React build sources from |
|---|---|
| a docking shell — panes, splits, tabs, persistence | `dockview`, `flexlayout-react` |
| shallow / deep focus across panes | hand-written on the layout's API; no library has an opinion |
| a message bus between widgets | `zustand`, `redux`, or a hand-rolled emitter |
| tree views (portfolios, ontology) | `react-arborist` |
| a grid (blotters, the ladder) | AG Grid, TanStack Table |
| typed styling and ten themes | CSS Modules and CSS variables |
| a catalogue, doc readers, diagrams | `react-router`, `react-markdown`, `remark-gfm`, `mermaid` |
| a 3D surface | `three` |
| enforcement of the papers' rules | ESLint with custom rules, partially |
| a runtime sweep | Playwright |
| a build | Vite, and whatever marries it to the backend's |
| a backend | the team's choice — Node, Spring, .NET |

Fourteen decisions, each with its own upgrade cadence, before the first
persona is served.

## 3. Currency two: the dependency tree

This is the measured part. Two lockfiles were resolved on 2026-09-04 with
`npm install --package-lock-only`, no scripts run, from current versions of
well-maintained libraries:

| stack | direct | resolved packages | advisories open that day |
|---|---|---|---|
| the floor — React, React DOM, TypeScript, Vite, the React plugin | 5 | **111** | 2 (1 moderate, 1 high) |
| the stack of §2 — the floor plus AG Grid, dockview, react-arborist, zustand, react-markdown, remark-gfm, mermaid, three, ESLint and its React rules, Vitest, Testing Library, jsdom, Playwright | 21 | **552** | 5 (3 moderate, 1 high, 1 critical) |
| for scale: this desk's *entire server*, HTTP stack included, runtime scope | — | **65** artifacts, ≈24 third-party | — |

Three things about that table. The 552 is the front end alone; the backend
the team chooses is added to it, not replaced by it. The advisory counts are a
snapshot and will differ next week, which is the point: they are not a number
a team fixes but a number it *attends to*, on every build, indefinitely. And
the critical one was open on a tree assembled that morning — an ordinary tree,
not a neglected one. That is the resting state.

## 4. Currency three: build systems

A React front end is a second build: its own resolver, lockfile, cache, CI
lane and failure modes, married to the backend's build by a plugin or kept in
a second repository. It is not an install. It is a system, owned for the life
of the product.

The papers' one existing answer builds and runs from source on a clean
machine with one tool. Whether the React answer can — and how many tools it
takes — is one of the experiment's measurements, because it is the cost that
is paid by everyone who ever clones the thing.

## 5. Currency four: enforcement

The papers carry rules — no literal colours, no inline styles, no wholesale
DOM destruction, a line ceiling, a theme-token vocabulary — and this desk
enforces twelve of them at build time, over the JavaScript the browser
actually receives, with a baseline that lets a rule land before every
violation is fixed.

A React team enforces over *source*, because the bundle is what ships and a
toolchain stands between the two. Line limits, CDN imports, inline styles and
literal colours have ESLint and Stylelint analogues. Crate integrity — every
module served is one the crate declares — has none, because the bundler is
the crate. The baseline ratchet has none. And DOM ownership cannot be
enforced at all, because of the next currency.

## 6. Currency five: ownership

React's premise is that the framework owns the DOM. That is not a cost in the
sense the others are; it is a change of terms.

A cell in this desk is a div, a class and a text node, owned by the domain
code that made it. In React a cell is a component, reconciled by the
framework on every change — which is precisely the downstream deficit the
virtualization study found makes a grid need a window at three hundred rows.
"Cells are ours", the finding both prior studies rest on, is not available
to a React team at any price; the ecosystem's grids exist to compensate for
its absence.

For most products this is simply the deal, taken without a second thought.
For the papers' P5 — the UI is a consumer, not a calculator — it is the
currency in which the boundary erodes: when the grid offers to compute the
totals, someone lets it.

## 7. Where React's cost is lowest

A ledger with only one column is not a ledger.

**Authoring.** The papers' existing answer writes most of its JavaScript as
Java string arrays — escaped, unhighlighted, gated by line count. JSX in an
editor that understands it is simply better, and it is not close. (This is
now the first item on this repository's backlog; it is a tax on the existing
answer, not on its premise.)

**The inner loop.** Hot reload in under a second against a build-and-restart
on the order of a minute. Over a project that is hours; over a team it is the
difference in how often anyone tries something.

**State-heavy widgets.** The pricer, the calibration lab, the override
inventory — forms with derived values — are what "UI as a function of state"
was invented for. A React team should reach acceptance on those workspaces
first, and the experiment expects them to.

**People.** Every React developer can read a component on day one. Devtools,
testing libraries and answered questions already exist.

## 8. The experiment

**The requirement set.** The papers, and nothing else. No access to this
repository. The user guide is the acceptance, workspace by workspace, each
with its intro naming who it is for.

**Two cold starts.** A React team on the backend of their choice — and, so
the comparison is fair, a homing team that has never seen homing. The
existing answer was built by the framework's author as the framework's test
case; a cold start removes that from both sides rather than pretending it
was not there.

**Rules.** Same clock. Same AI-assistance policy on both sides, whichever it
is. No framework author and no paper author on either team. A third party
judges acceptance; partial credit is reported per workspace and never
averaged into a score.

**What is measured.**

| dimension | how |
|---|---|
| time | calendar days and developer-days to each workspace's acceptance |
| size | lines by language; the count of things authored |
| dependency tree | resolved packages or artifacts from a lockfile; advisories open on acceptance day |
| build | number of build systems; a cold build from empty caches on a clean machine |
| enforcement | which of the twelve rules have an *enforced* equivalent, and which are policy only |
| runtime | the mount-everything, click-everything sweep, run by the judge on both |
| the boundary | the number of places the UI computes what the server should have published |
| ownership | who owns a cell |

**What this study expects, stated so it can be wrong.**

1. The React team reaches acceptance first on the form-heavy workspaces and
   last on the grid-heavy ones.
2. The React tree is larger by an order of magnitude and carries at least one
   open advisory on acceptance day.
3. Without a reviewer holding the papers, the P5 boundary erodes on the React
   side, because the grid offers to compute.
4. The homing cold start pays its first week to the framework and its later
   weeks are cheaper; whether the curves cross before acceptance is the
   result most worth having.
5. On a clean machine, one side builds from source with one tool and the
   other does not.

If the fourth is false — if the homing side never catches up — that is a
finding against the framework, and the experiment is worth running precisely
because it can come out that way.

## 9. The conclusion: the ledger at a glance

| currency | React, cold start | this desk | status |
|---|---|---|---|
| sourcing decisions before the first persona | 14 | 0 — one framework | counted, §2 |
| resolved packages, front end | 111 at the floor, **552** for the stack | none — no front-end tree | measured 2026-09-04, §3 |
| resolved artifacts, the server | the backend's own, on top | **65**, ≈24 third-party | measured, §3 |
| advisories open, that day | 2 at the floor, **5** for the stack, one critical | not scanned; the exposure is the 24 | measured / not measured |
| build systems | 2 | 1 | counted, §4 |
| cold build on a clean machine | Node, npm, the backend's tools, and the marriage between them | JDK and Maven, one command — proven from an empty repository | proven one side, expected the other |
| papers' rules enforced at build, of 12 | about 5 with an analogue; crate integrity, the baseline ratchet and DOM ownership with none | 12 | estimated, §5 |
| who owns a cell | the framework | the domain | binary, §6 |
| **developer-days to acceptance, 13 workspaces** | **90–150**, expected | **60–105**, expected | hypothesis — the experiment's output |
| the existing answer's clock | — | 76 commits in 25 calendar days | measured, and not a cold start |

The day figures are expectations and are labelled so. The React range is the
stack of §2 plus a backend built from the papers: roughly ten to fifteen days
of data model and endpoints, twelve to twenty for shell, focus, bus, themes
and catalogue, forty to eighty for forty-one widgets, and ten or so for
enforcement, tests, the sweep and the build. The homing range removes what
the framework supplies — the shell, bus, trees, grid, catalogue, themes and
conformance, some twenty-five to thirty-five days of it — and pays instead a
first week to the framework and the authoring tax of §7 on every widget. The
two ranges overlap widely. That overlap is the reason the experiment is worth
running: it is the one number in the table that only the experiment can
settle, and it could settle it either way.

So the cost of React development, for these papers, has a figure in every
currency but the one the schedule shows, and an expectation there. Where it is
lowest — authoring and the inner loop — is exactly where the existing answer's
is highest, and both of those are taxes on this repository rather than on its
premise, and are on its backlog.

**Cost a framework in every currency it charges, not only the one the
schedule shows.**
