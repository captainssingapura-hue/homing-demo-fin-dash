package hue.captains.singapura.js.homing.findash.studio.docs;

import hue.captains.singapura.js.homing.studio.base.ExternalReference;
import hue.captains.singapura.js.homing.studio.base.Reference;
import hue.captains.singapura.js.homing.studio.base.composed.ComposedDoc;
import hue.captains.singapura.js.homing.studio.base.composed.Segment;
import hue.captains.singapura.js.homing.studio.base.composed.TextSegment;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * One short introduction per persona workspace — who the desk is, what its
 * screens are for, and a path through the demo that shows the point.
 *
 * <h2>Why these exist</h2>
 *
 * <p>They replace the thirteen catalogue tiles that used to sit under
 * <i>Workspaces</i>. Those tiles were pure navigation: thirteen entries
 * differing only by a URL parameter, offering no reason to pick one. They also
 * all wrapped the same {@code AppModule} class, which the framework's
 * {@code /app-refs} lookup keys by app id — so it collapsed them to one
 * arbitrary winner and every workspace page showed a breadcrumb naming a
 * different workspace (recorded in {@code docs/defect-app-refs-ambiguous.md}).</p>
 *
 * <p>A document per persona is the honest shape. Switching workspace is
 * <em>app state</em>, done in place from the workspace title; <em>understanding</em>
 * a persona is reading material. Docs also resolve by UUID rather than app id,
 * so their breadcrumbs are correct by construction.</p>
 *
 * <h2>Authoring</h2>
 *
 * <p>Content is data — one {@link Intro} per persona, rendered by one builder.
 * That keeps the thirteen consistent and makes the set cheap to extend when a
 * persona gains a screen. Every UUID is fixed: a doc's identity is its
 * permalink, and regenerating one would break saved links.</p>
 */
public final class WorkspaceIntroDocs {

    private WorkspaceIntroDocs() {}

    /**
     * @param kind    the workspace kind ({@code ws_kind}) this intro opens
     * @param title   the persona's display name
     * @param uuid    fixed doc identity — never regenerate
     * @param mission what this desk is accountable for, in its own terms
     * @param screens the widgets, each as "**Name** — what it is for"
     * @param tryThis a path through the demo that shows the point
     */
    private record Intro(String kind, String title, String uuid,
                         String mission, List<String> screens, String tryThis) {}

    private static final List<Intro> INTROS = List.of(

        new Intro("trader", "Trader Desk", "a1f2c3d4-0001-4a11-9c01-fd0000000001",
            """
            The trader runs the book: quote it, price it, mark it, hedge it. This is the only \
            desk that both writes risk and carries it, so every number it shows has to say \
            where it came from — the slice it was priced on, the surface epoch, the model \
            version — without being asked.
            """,
            List.of(
                "**Pricer** — desk shorthand in, price out. Type `eurusd 3m 1.0850 ko 1.1200 10` and press Enter.",
                "**Surface Manager** — fit versus market per tenor, with Ring-3 override entry when the broker run is stale.",
                "**Vol Surface 3D** — the whole surface at once, for the shape a table cannot show.",
                "**Risk Blotter** — pair-by-tenor buckets, each with a freshness meter; stale is a state, not a footnote.",
                "**Barrier Watch** — proximity to knock-out levels, loudest when it matters.",
                "**Expiry / Pins** — what expires today, and where the pin risk sits."),
            """
            Price the barrier ticket above, then open the Risk Blotter and click the EURUSD row \
            — the Pricer and Surface Manager follow the selection. Watch the freshness meters: \
            when one crosses its budget it changes state visibly rather than quietly ageing. \
            Everything on screen carries the same slice stamp, which is the P1 claim made concrete.
            """),

        new Intro("etrading", "e-Trading Supervision", "a1f2c3d4-0002-4a11-9c01-fd0000000002",
            """
            Auto-quoting makes money while it is safe and loses it faster than a human can react \
            when it is not. This desk watches the machine quote and holds the kill switch, so the \
            console's job is to make the current posture unmistakable and stopping trivial.
            """,
            List.of(
                "**Quoting Console** — per-pair streaming state, and a one-gesture master kill.",
                "**RFQ Tape** — every quote that went out, stamped with the slice it was priced on."),
            """
            Hit MASTER KILL. Note that stopping takes one gesture and restarting deliberately does \
            not — the asymmetry is the design. Then read the RFQ tape: each row carries its own \
            lineage, so a quote can be explained months later without reconstructing the state \
            that produced it.
            """),

        new Intro("sales", "Sales & Structuring", "a1f2c3d4-0003-4a11-9c01-fd0000000003",
            """
            Sales prices for the client, not for the desk. The margin is theirs to set and \
            everyone's to see: it is shown as its own number rather than blended into "the price", \
            so product control can attribute it and the desk can recognise its own mark underneath.
            """,
            List.of(
                "**Client Pricer** — desk price, sales margin, and the client all-in, decomposed."),
            """
            Price a ticket, then change the margin — the client all-in updates and still shows its \
            parts. Quote FIRM and watch the countdown: firm is a commitment with an expiry, not a \
            label. The decomposition is P1 again, applied to a number usually presented as atomic.
            """),

        new Intro("market-data", "Market Data Operations", "a1f2c3d4-0004-4a11-9c01-fd0000000004",
            """
            Everything downstream is a function of the inputs, so this desk owns whether the inputs \
            can be trusted. Its screens assume degraded data is normal, and that the interesting \
            question is always "what does this affect?".
            """,
            List.of(
                "**Feed & Quality** — feed health by source, where a degraded cell opens its downstream impact.",
                "**Override Inventory** — every live Ring-3 action, oldest first, each with who, why, and when it expires."),
            """
            Click a degraded cell and follow the impact through to the desks that consume it. Then \
            read the override inventory as an ageing list: an override is a debt with a holder and \
            a maturity, which is why the oldest sorts to the top.
            """),

        new Intro("quant", "Quant Lab", "a1f2c3d4-0005-4a11-9c01-fd0000000005",
            """
            The quant owns whether the model still fits the market, and owns how it changes. \
            Diagnostics here are read-only and live: evolution goes through governance (Ring 2), \
            never through this screen.
            """,
            List.of(
                "**Calibration Lab** — per-pair residuals, arb-gate margins, solver behaviour, and fast-versus-slow agreement."),
            """
            Read one pair's card top to bottom. Every value is a point on a time series rather than \
            a status light, and the arb-gate margins say how much room is left before the surface \
            stops being arbitrage-free — a margin, not a boolean.
            """),

        new Intro("risk", "Market Risk", "a1f2c3d4-0006-4a11-9c01-fd0000000006",
            """
            An independent view of what the desk is carrying, on a cadence that is stated rather \
            than implied. Risk does not recompute the desk's numbers; it reads the same journaled \
            slices and says what they mean against the limits.
            """,
            List.of(
                "**Risk Views** — exposures against limits, with the as-of made explicit.",
                "**Scenario Workbench** — governed scenarios, run against a named slice.",
                "**Concentrations** — where the book is bunched up."),
            """
            Note the as-of stamp before reading any number — a risk figure without its as-of is a \
            rumour. Run a scenario and see that it names the slice it ran against, so the same \
            scenario re-run later gives the same answer (P4).
            """),

        new Intro("governance", "Model Governance", "a1f2c3d4-0007-4a11-9c01-fd0000000007",
            """
            Nothing prices or marks without approval, and approval is a record rather than a \
            conversation. This desk decides what may run, and the evidence for each decision stays \
            attached to it.
            """,
            List.of(
                "**Change Console** — Ring-2 packages: semantic diff, replay impact, four-eyes chain, epoch-staged activation.",
                "**Model Inventory** — what is live where, and the reverse query: which desks depend on this model?"),
            """
            Open a pending change and read it as a case file: what changes, what it would have done \
            to yesterday's book, who signed, and when it activates. Then use the reverse query — \
            "who breaks if I retire this?" is the question that is normally unanswerable.
            """),

        new Intro("middle-office", "Middle Office", "a1f2c3d4-0008-4a11-9c01-fd0000000008",
            """
            Every trade correct through its whole life, not just at booking. The journal is the \
            audit trail; these screens render it rather than summarising it, so what you read is \
            what was recorded.
            """,
            List.of(
                "**Trade Blotter** — the journal as a blotter. Arrow keys walk it; amended trades unfold their history.",
                "**Lifecycle Workstation** — one trade's full event journal, from booking to settlement.",
                "**Portfolios** — the book tree, which scopes everything to its left."),
            """
            Pick a portfolio, then walk the blotter with the arrow keys — the lifecycle view follows \
            every step. Open an amended trade: the amendment carries who, what, the P&L impact, and \
            the four-eyes record, because an amendment is a journal fact and not an edit.
            """),

        new Intro("ipv", "Product Control & IPV", "a1f2c3d4-0009-4a11-9c01-fd0000000009",
            """
            P&L is right and the marks are independently verified. Every attribution term is \
            computed from the same journaled slices the desk traded on, so an explain discussion is \
            about the business rather than about whose spreadsheet is correct.
            """,
            List.of(
                "**P&L Explain / IPV** — attribution by term, the IPV workbench (desk versus independent), and the EOD marks sign-off."),
            """
            Read the explain: the unexplained term carries its tolerance, so it is either within it \
            or it opens a drill-down — the argument is pre-empted by construction. The sign-off \
            stamps an epoch official, a governance bit on the same machinery rather than a separate \
            system.
            """),

        new Intro("platform", "Platform Operations", "a1f2c3d4-0010-4a11-9c01-fd0000000010",
            """
            The system itself is a thing that can be healthy or not, and the people who keep it up \
            need controls that say what they will do before they do it.
            """,
            List.of(
                "**Platform Console** — SLO budget meters, and Ring-3 controls that state their blast radius before commit.",
                "**Epoch Flow** — the epoch pipeline, with root-cause click-through when one stalls."),
            """
            Read a control's blast radius before pressing it — a control that cannot say what it \
            affects should not be pressed. Follow a stalled epoch back to its cause: the chain is \
            the same journal every other desk reads.
            """),

        new Intro("audit", "Audit Explorer", "a1f2c3d4-0011-4a11-9c01-fd0000000011",
            """
            Reconstruct anything, attest to it, and be able to do so again next year. If the journal \
            is complete then audit is a standing capability rather than an annual excavation.
            """,
            List.of(
                "**Audit Explorer** — cross-journal search and time travel.",
                "**Trade Blotter** and **Portfolios** — the same anchors the desks use, seen from outside."),
            """
            Reconstruct a past state and note what makes it trustworthy: not that the screen can be \
            rebuilt, but that it rebuilds identically every time (P4). The desks and the auditor read \
            the same journal — there is no separate audit extract to reconcile.
            """),

        new Intro("summary", "Management Summary", "a1f2c3d4-0012-4a11-9c01-fd0000000012",
            """
            Situational awareness in one screen, and a way in when something needs attention. \
            Management does not need a different truth from the desks — it needs the same one, \
            aggregated, with every tile a door.
            """,
            List.of(
                "**Summary** — P&L, risk against limits, and quoting posture, rolled up."),
            """
            Read it as a glance, then click through a tile to the desk that owns it. The aggregation \
            is presentation only (P5): nothing here computes a number that a desk does not already own.
            """),

        new Intro("data-ontology", "Data Ontology", "a1f2c3d4-0013-4a11-9c01-fd0000000013",
            """
            For the builders rather than the desk: the type catalogue behind every screen, and which \
            widgets require which types. It is the demo explaining its own substrate.
            """,
            List.of(
                "**Data Types** — browse the ontology and see each type's consumers."),
            """
            Pick a type and read its consumers — the reverse query again, this time over data rather \
            than models. It is the quickest way to see how much of the demo shares one vocabulary \
            instead of thirteen private ones.
            """)
    );

    /** Every intro, in participant-map order. */
    public static final List<ComposedDoc> ALL = build();

    private static List<ComposedDoc> build() {
        var out = new ArrayList<ComposedDoc>();
        for (Intro i : INTROS) out.add(doc(i));
        return List.copyOf(out);
    }

    private static ComposedDoc doc(Intro i) {
        var segments = new ArrayList<Segment>();
        segments.add(new TextSegment(i.mission(), Optional.of("What this desk is for")));

        var screens = new StringBuilder();
        for (String s : i.screens()) screens.append("- ").append(s).append('\n');
        segments.add(new TextSegment(screens.toString(), Optional.of("The screens")));

        segments.add(new TextSegment(i.tryThis(), Optional.of("Try this")));

        // The composed viewer does not render a References section, and the text
        // grammar accepts no link form other than `[label](#ref:name)` — so a
        // citation here would render as an anchor that scrolls nowhere. The path
        // is given literally instead: readable, and correct when pasted.
        segments.add(new TextSegment(
                "Open **Workspace** from the landing page, then click the workspace title and "
                + "pick **" + i.title() + "** from the type list — switching happens in place, "
                + "and every other participant's desk is reachable the same way.\n"
                + "\n"
                + "To go straight there: `/app?app=genericWorkspace&ws_kind=" + i.kind() + "`\n",
                Optional.of("Opening it")));

        List<Reference> refs = List.of(new ExternalReference(
                "open",
                "/app?app=genericWorkspace&ws_kind=" + i.kind(),
                i.title(),
                "The " + i.title() + " workspace."));

        return new ComposedDoc(
                UUID.fromString(i.uuid()),
                "Intro :: " + i.title(),
                firstSentence(i.mission()),
                "DOC",
                segments,
                refs);
    }

    /** The summary shown on the catalogue tile — the mission's opening claim. */
    private static String firstSentence(String prose) {
        String flat = prose.replace('\n', ' ').replaceAll("\\s+", " ").trim();
        int dot = flat.indexOf(". ");
        return dot < 0 ? flat : flat.substring(0, dot + 1);
    }
}
