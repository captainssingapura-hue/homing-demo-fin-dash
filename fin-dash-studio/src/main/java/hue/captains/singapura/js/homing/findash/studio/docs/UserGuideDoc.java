package hue.captains.singapura.js.homing.findash.studio.docs;

import hue.captains.singapura.js.homing.studio.base.composed.ComposedDoc;
import hue.captains.singapura.js.homing.studio.base.composed.TextSegment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The FX Options Desk user guide — how to open a workspace, add widgets, and
 * follow the demo story across personas. Authored as a {@link ComposedDoc}
 * (typed segments; rendered by the framework's composed viewer with TOC).
 */
public final class UserGuideDoc {

    private UserGuideDoc() {}

    private static final UUID ID = UUID.fromString("3e9a4c88-51d6-4f2b-a7e0-8b64c2f1d955");

    public static final ComposedDoc INSTANCE = build();

    private static ComposedDoc build() {
        var whatThisIs = new TextSegment(
                """
                The FX Options Desk is one workspace per human actor in the FX options stack — trader, \
                e-trading supervisor, sales, market data operations, quant, market risk, model governance, \
                middle office, product control, platform SRE, audit, and management. Every workspace runs on \
                the same shell (split panes, tabs, saved layouts) and every screen obeys the same five \
                principles: every number can explain itself (**P1**), degraded state is loud (**P2**), the \
                governance rings are visible in the chrome (**P3**), time travel is universal (**P4**), and \
                UIs are consumers, not calculators (**P5**).
                """,
                Optional.of("What this is"));

        var gettingStarted = new TextSegment(
                """
                From the landing page open the *Workspaces* catalogue and pick a persona. A workspace opens \
                with empty panes: press the `+` button in any pane and pick a widget from that persona's \
                roster. Panes split horizontally or vertically, tabs drag between panes, and layouts persist \
                per workspace. The `+` picker only offers the widgets that persona's spec declares — a widget \
                that is not in a registered crate is refused by the server outright.
                """,
                Optional.of("Getting started"));

        var tour = new TextSegment(
                """
                - **Trader Desk** — the Pricer (desk shorthand in, `eurusd 3m 1.0850 ko 1.1200 10`, Enter \
                prices), the Surface Manager (fit vs market per tenor, Ring-3 override entry), the Risk \
                Blotter (pair-by-tenor buckets with freshness meters), Barrier Watch, and Expiry/Pins.
                - **e-Trading Supervision** — the Quoting Console (per-pair streaming state, one-gesture \
                master kill, ceremonious un-kill) and the slice-stamped RFQ Tape.
                - **Market Data Operations** — the Feed & Quality grid (click a degraded cell for its \
                downstream impact) and the Override Inventory (every live Ring-3 action, oldest first).
                - **Market Risk** — limits and utilization on snapshot cadence (the as-of is explicit), \
                the governed Scenario Workbench, and concentration views.
                - **Model Governance** — the Change Console (Ring-2 packages: semantic diff, replay impact, \
                four-eyes chain, epoch-staged activation) and the Model Inventory with the reverse query.
                - **Platform Operations** — SLO budget meters and Ring-3 controls that state their blast \
                radius before commit, plus the Epoch Flow with root-cause click-through.
                - Sales, Quant, Middle Office, Product Control, Audit, and Management each carry their \
                primary screen — client pricer, calibration lab, lifecycle workstation, P&L explain, \
                cross-journal explorer, and the summary rollup.
                """,
                Optional.of("The workspaces"));

        var crossWidget = new TextSegment(
                """
                Widgets in a workspace share a selection bus (the desk party). Click a row in the Risk \
                Blotter, Barrier Watch, Expiry/Pins, Quoting Console, or a quarantine entry — the Pricer \
                prefills that pair and the Surface Manager switches to it. The Scenario Workbench broadcasts \
                scenario picks the same way. This is the study's cross-widget flow: one selection, every \
                consumer refreshes.
                """,
                Optional.of("Cross-widget selection"));

        var story = new TextSegment(
                """
                The fabricated data tells one coherent story. Broker C's `volBF` quote trips the jump filter \
                and is quarantined (*Feed & Quality*). The USDJPY surface loses its composite depth and goes \
                stale (*Surface Manager* shows the waiting recalibration; *Epoch Flow* shows the cadence \
                breach). The reval error budget stretches to 92% (*Risk Blotter* freshness meters; *Risk \
                Views* worklist). Quoting auto-widens under rule QW-3 (*Quoting Console* event stream; a \
                degraded-window RFQ is lost on the *RFQ Tape*, stamped with the stale surface). A trader \
                override ages in the *Override Inventory*, product control flags the same mark in *IPV*, \
                package #414 proposes the tolerance fix in the *Change Console*, every action lands in the \
                *Audit Explorer*, and the *Management Summary* rolls it all up as one warning.
                """,
                Optional.of("The demo story"));

        var conformance = new TextSegment(
                """
                Every served widget and model is declared in its module's crate and graded by the RFC 0044 \
                conformance gate — the conformance studio (port 8101) shows the crates, the modules grouped \
                by type (including the downstream `risk-model` type with its determinism rule), and the \
                exported report. A module missing from its crate fails the build and is refused by the \
                server: nothing reaches the browser while escaping conformance.
                """,
                Optional.of("Under the hood: conformance"));

        return new ComposedDoc(ID,
                "User Guide",
                "How to open a workspace, add widgets, follow the cross-widget selection flow, "
                        + "and read the demo story that runs through every persona's screens.",
                "USER GUIDE",
                List.of(whatThisIs, gettingStarted, tour, crossWidget, story, conformance),
                List.of());
    }
}
