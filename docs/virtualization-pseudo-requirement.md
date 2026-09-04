# Virtualization is a pseudo-requirement

**A study.** Every grid on the market advertises the number of rows it can
scroll — a hundred thousand, a million — and every evaluation copies the
number into its checklist. This study asks the question the checklist skips:

> **Why would a user scroll something that is too large for a browser session
> to handle?**

The claim is that they would not; that what they are doing when they appear to
is one of a small number of other things, each of which the domain serves
better than a scrollbar; and that virtualization is therefore a requirement on
a mechanism standing in for a requirement on an outcome — which is what a
pseudo-requirement is.

---

## 1. Two limits, and the gap between them

There are two capacities in play and they are not close.

The **browser's** limit is DOM. A table of seven columns of bare cells — a
div, a class, a text node — builds and lays out five thousand rows in about
six tenths of a second on a current machine, and twenty thousand in three and
a half; scrolling either is effectively free. Virtualization exists for the
region past that — the tens of thousands. (Hold on to "bare cells"; §3 comes
back to it.)

The **reader's** limit is attention. A person can hold perhaps seven things in
mind, scan perhaps thirty to fifty rows for a pattern, and read one screen.
Past that, rows are not *seen*; they are *scrolled past*. And scrolling past
is not a use case. It is the absence of one.

Everything virtualization makes possible lives in the gap between those two
limits — from a few thousand rows up — and nothing a person needs lives there.
The feature makes the browser survive a quantity the reader had already
stopped being able to use.

## 2. What the user is actually doing

Watch someone "scroll a large table" and they are doing one of five things.

**Looking for something they can name.** A trade id, a counterparty, a
pair. This is *search*, and scrolling is search's most primitive form — a
linear scan with the eyes. The need is *find*, and the domain owns find:
a filter, a query, a jump-to. Once found, the row count is one.

**Looking for something they cannot name yet.** "Let me eyeball the book."
This is *browsing*, and no one browses five thousand rows for an anomaly. They
browse twenty subtotals and open the one that looks wrong. The need is
*summarise and drill* — groups, folds, aggregates — which is exactly the shape
of the risk ladder, and exactly what its domain code owns.

**Reading in sequence.** A journal, a tape, a log. Here "next" is the
navigation and the set is genuinely long — but the window is a *time range*,
and the reader wants the latest, then a way back. Tail and page-by-time. The
domain owns time.

**Reassuring themselves the whole thing is there.** "I want to see everything
made it." This is the one honest case for wanting *all N* — and the assurance
does not come from a scrollbar. It comes from a total ("57 trades, all
confirmed"), an as-of stamp, or a file. A thumb one pixel tall, standing for a
hundred rows, reassures no one.

**Because the tool offered nothing else.** The scrollbar is the navigation of
last resort, the one every UI has even when it has forgotten to provide
search, fold, or a time window. Virtualization makes that fallback survive at
scale. It is a feature that makes a missing feature bearable.

So the question inverts. A user scrolls a large set *because the UI failed to
offer the navigation the task needed*. Virtualization treats the symptom —
makes the scrolling fast — and leaves the cause in place.

## 3. The two deficits it compensates for

Set the honest exceptions (§7) aside and ask a plainer question: when is
virtualization *actually deployed*? There are two cases, and each is a
compensation for a deficit somewhere else.

**Upstream: the domain offers no breakdown.** No find, no fold, no summary,
no time window — so the UI is handed the whole set and the reader is handed a
scrollbar. This is §2's fifth case seen from the other side: the fallback was
not a choice the user made but a gap the domain left. Virtualization makes the
gap survivable at scale, and in doing so removes the pressure to close it.

**Downstream: the rendering is too heavy for moderate sizes.** This is the
case the checklists never state, and the one that explains the feature's
ubiquity. Nobody reaches for virtualization because of a million rows. They
reach for it because *three hundred* rows stutter — a component tree per cell,
a reconciliation pass on every tick, styles computed per cell per render. The
grid is virtualized not to cope with the data but to cope with itself. The
popular stacks need it at sizes a bare table would not notice; so the feature
is everywhere; so it is on every checklist; so it is a requirement.

Read §1's numbers again with the second deficit in view. Bare cells build and
lay out two thousand rows in about a quarter of a second, sweep a new value
through all two thousand of one column in thirty milliseconds, and hide ninety
percent of them in twenty. That is a table with no downstream deficit, and it
needs no window at any size a reader can use. Make the cell a component and
those numbers go up by an order of magnitude, and the window becomes
necessary — not because the data grew, but because the cell did.

So the diagnosis is two questions, asked before the feature is considered:

1. *Does the domain give the reader a way to the rows they need?*
2. *Does a row cost more to draw than it should?*

"Yes" and "no" leave virtualization with nothing to compensate for. "No" or
"yes" names the thing to fix — and fixing it is cheaper than the feature,
because the feature must be paid for again at every gesture in §6.

## 4. What the scrollbar is for, and when it stops being for it

A scrollbar's thumb encodes *position in the set* and its length encodes *how
much there is*. Both affordances are real at fifty rows. At a hundred thousand
they are gone: the thumb is a fixed-height sliver whose position resolves to
hundreds of rows per pixel, and its length says only "a lot".

Virtualization's central trick is to *maintain that illusion* — a scroll height
that pretends every row is present — precisely in the regime where the
affordance it preserves has already stopped working. It keeps the scrollbar
honest about a quantity the scrollbar can no longer express. A count expresses
it better in six characters: `40 of 57,214`.

## 5. The tell: a UI that has become a store

There is a second reason the requirement is a pseudo one, and it is about
where the data lives.

Client-side virtualization presumes the client *holds* all N rows and renders
a window of them. A UI holding a hundred thousand rows is no longer a consumer
of the domain's answers; it is a second copy of the database, with the
domain's job — deciding what is relevant — transplanted into the browser and
delegated to the reader's thumb. That is the UI-study's P5 violated
structurally rather than arithmetically: not a UI that *calculates*, but a UI
that *stores*.

The vendors know this. Every serious grid ships a *server-side* row model
alongside the client-side one, in which the server answers "rows 400–460" on
demand. But look at what that is: it is the **domain** choosing a window, with
the grid drawing a scrollbar over rows it does not have. The feature, in its
mature form, has already conceded the point. The windowing is domain work; the
grid keeps the illusion.

## 6. What it breaks, and then rebuilds

Rows that are not in the DOM are not in the DOM. Every gesture that reads the
page rather than the model breaks, and a virtualizing grid must then
reimplement each one against its own model:

| gesture | what happens | what the grid must rebuild |
|---|---|---|
| Ctrl+F | the browser cannot find text that is not rendered | its own find |
| Ctrl+A, copy | copies the forty rows on screen | copy from the model, not the DOM |
| screen reader | announces forty rows | ARIA row counts and virtual positions |
| print | forty rows | a print path |
| End, Page Down | positions within the rendered slice | its own key handling |
| a link to a row | nothing to link to | scroll-to-row with deferred render |

Ctrl+F is not a corner case on a desk; it is how a trader finds a trade id in
a blotter. Each rebuild is a place where the grid's copy of a browser feature
is a little worse than the browser's, and each is a cost paid so that a
quantity no one reads can be scrolled.

## 7. The honest exceptions, and where they resolve

It would be too neat to say there is never a case. There are three that come
up, and it is worth following each to where it ends.

**A stream.** A tick tape or an event log is unbounded by construction. But
the reader wants the head of it and a way back in time; the window is a time
range, the domain owns time, and paging by time is not virtualization. It is
what virtualization would be if the scrollbar were honest.

**Expensive cells.** A sparkline in every row hurts at two hundred rows, not
ten thousand — and the answer is again the domain's: do not render two hundred
sparklines. Render them on hover, on expand, or for the focused block.

**The full-blotter mandate.** "Regulation requires the complete list." It
requires the list to *exist* and to be *exportable* — which is a server
endpoint and a file. It does not require the list to be scrolled, and a
regulator has never scrolled one.

Each exception resolves to a case for *having* N on the server and *seeing* a
domain-chosen window. None resolves to a case for scrolling N.

## 8. What the ladder already does instead

The risk ladder in this desk never virtualizes and never needs to, because
neither deficit of §3 is present. Upstream, its domain code answers the five
questions of §2 directly:

- **find** — selecting a pair anywhere on the desk focuses the ladder to it
  (the filter predicate, driven by the bus);
- **browse** — every pair opens as a section that folds to its subtotal
  (a row kind and the same predicate);
- **sequence** — tenors read down the curve in the order `pks()` hands them;
- **reassurance** — the grand total, the as-of slice, and the caption's
  statement that totals come from the desk, not from summing what is shown;
- **the fallback** — is not needed, because the four above exist.

And the one grid mechanism that touches the question — the filter *detaching*
hidden rows rather than disposing them — means the domain's reduction of the
set is the DOM's reduction too, for free. The grid did not need a window.
The domain made one.

Downstream, the deficit is absent by construction. A ladder cell is a div, a
class and a text node — the weight of the cells §1 was measured with — and the
grid's update batch coalesces a hot feed to one `update()` per cell per frame.
Nothing reconciles a tree; nothing computes a style per render. That is not an
optimisation applied to the ladder; it is what "cells are ours" costs, which
is to say nothing.

## 9. Why the number is on every checklist anyway

Because it is a number, and because the stacks that dominate the market need
it. "Handles a million rows" can be benchmarked, screen-recorded and put on a
pricing page; "reduces the set to what the reader can use" cannot. And a grid
whose cells are components must virtualize at three hundred rows or stutter
(§3), so every such grid ships the feature, and a feature every product ships
becomes a line every checklist copies. Requirements that are easy to measure
crowd out requirements that are true — the same mechanism by which a line
count becomes a code-quality metric.

Which is where this study and the 250-line rule meet. Both are numbers in the
machine's units. One is a joke that knows it (past 250 lines the *author* can
no longer hold the module — 二百五). The other is a boast that does not: past
fifty rows the *reader* can no longer hold the table, and a grid proud of
rendering a million of them is proud of a capacity no one can use.

## 10. The conclusion, as a rule

**A requirement stated in rows-the-grid-can-render is either a requirement on
the domain that has been misfiled, or a bill for the rendering stack that has
been misaddressed.** Read it as "what does the user need to see, and how do
they get to it?" — and answer with find, fold, summarise, window by time, and
count. Then ask what a row costs to draw, and if the answer is "a component",
answer that instead. If, after all of that, the set on screen is still too
large for the browser, the domain has not finished; it has not become the
grid's problem.

Virtualization is what you build when you decide not to answer that question.
It is very well engineered. It is still the wrong question.
