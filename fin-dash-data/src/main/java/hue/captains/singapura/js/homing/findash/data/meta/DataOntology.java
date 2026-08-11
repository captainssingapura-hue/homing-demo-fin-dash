package hue.captains.singapura.js.homing.findash.data.meta;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * <b>The catalogue of data types</b> — the ontology, declared as data.
 *
 * <p>Every type the dataset defines appears here with its stratum, its mode of
 * being, the era that introduced it, the edges it carries, and the widgets that
 * consume it. Two things fall out of writing this down:</p>
 * <ul>
 *   <li>the type graph becomes <b>browsable</b> — a tree the desk can navigate
 *       like any other, before a single fact exists;</li>
 *   <li>"which screens need this?" becomes a <b>query</b>, so the connection
 *       map in the requirements can never quietly drift from the code.</li>
 * </ul>
 *
 * <p>Era 1 (the trader's world) is declared. Later eras append: each adds its
 * types and — crucially — <i>adds usages to existing types</i>, which is what
 * makes "the same object, seen by another persona" visible as data.</p>
 */
public final class DataOntology implements StatelessFunctionalObject {

    // ------------------------------------------------------------ vocabulary

    private DataTypeId id(final String value) {
        return new DataTypeId(value);
    }

    private Relation rel(final String from, final String edge, final String to,
                         final Cardinality card, final String purpose) {
        return new Relation(id(from), edge, id(to), card, purpose);
    }

    private WidgetUsage use(final String widgetId, final String label, final String workspace,
                            final UsageRole role, final String note) {
        return new WidgetUsage(widgetId, label, workspace, role, note);
    }

    // ------------------------------------------------------------ the catalogue

    /** Every declared type, in stratum order. */
    public List<DataType> types() {
        final List<DataType> all = new ArrayList<>();
        all.addAll(identities());
        all.addAll(quantities());
        all.addAll(reference());
        all.addAll(marketState());
        all.addAll(book());
        all.addAll(journal());
        all.addAll(derived());
        all.addAll(behaviour());
        return List.copyOf(all);
    }

    public Optional<DataType> type(final DataTypeId id) {
        return types().stream().filter(t -> t.id().equals(id)).findFirst();
    }

    public List<DataType> inStratum(final Stratum stratum) {
        return types().stream().filter(t -> t.stratum() == stratum).toList();
    }

    /** Types introduced by a given era — the growth story, queryable. */
    public List<DataType> ofEra(final int era) {
        return types().stream().filter(t -> t.era() == era).toList();
    }

    /** Every type a given widget consumes — the inverse of the usage map. */
    public List<DataType> typesUsedBy(final String widgetId) {
        return types().stream()
                .filter(t -> t.usages().stream().anyMatch(u -> u.widgetId().equals(widgetId)))
                .toList();
    }

    /** Edges pointing at a type — the reverse direction, which is always a query (R6). */
    public List<Relation> relationsTo(final DataTypeId target) {
        final List<Relation> out = new ArrayList<>();
        for (final DataType t : types()) {
            for (final Relation r : t.relations()) {
                if (r.to().equals(target)) {
                    out.add(r);
                }
            }
        }
        return List.copyOf(out);
    }

    // ------------------------------------------------------- stratum 0: identity

    private List<DataType> identities() {
        final String why = "An identity is a value, never a bare String at a boundary — "
                + "which is what makes the join contract type-checked rather than hopeful.";
        return List.of(
                simpleId("PairId", "a currency pair, e.g. EURUSD", List.of(
                        use("surface-manager", "Surface Manager", "trader", UsageRole.DRIVES,
                                "pair tabs publish the selection"),
                        use("risk-blotter", "Risk Blotter", "trader", UsageRole.DRIVES,
                                "bucket rows publish pair + tenor"),
                        use("pricer", "Pricer", "trader", UsageRole.FOLLOWS,
                                "prefills the ticket for the selected pair"),
                        use("quoting-console", "Quoting Console", "etrading", UsageRole.DRIVES,
                                "pair name click"),
                        use("epoch-flow", "Epoch Flow", "platform", UsageRole.DRIVES,
                                "root-cause row publishes its pair"))),
                simpleId("TenorId", "a point on the standard ladder, e.g. 3M", List.of(
                        use("risk-blotter", "Risk Blotter", "trader", UsageRole.RENDERS,
                                "the tenor rung of pair ▸ tenor"),
                        use("surface-manager", "Surface Manager", "trader", UsageRole.DRIVES,
                                "tenor strip selection"))),
                simpleId("InstrumentId", "what was traded (the contract definition)", List.of(
                        use("portfolio", "Portfolio", "book", UsageRole.RENDERS,
                                "resolves the instrument to describe the row"))),
                simpleId("TradeId", "an execution in the trade journal", List.of(
                        use("trade-blotter", "Trade Blotter", "book", UsageRole.DRIVES,
                                "row click publishes the trade"),
                        use("portfolio", "Portfolio", "book", UsageRole.RENDERS,
                                "the trade column is the door to the journal"),
                        use("audit-explorer", "Audit Explorer", "audit", UsageRole.RENDERS,
                                "journal entries name their subject"))),
                simpleId("PositionId", "an aggregated holding", List.of(
                        use("portfolio", "Portfolio", "book", UsageRole.DRIVES,
                                "row click publishes the position"),
                        use("barrier-watch", "Barrier Watch", "trader", UsageRole.RENDERS,
                                "each watch row is a position"))),
                simpleId("PortfolioNodeId", "a node at any level of the book tree", List.of(
                        use("portfolio-tree", "Portfolio Tree", "book", UsageRole.DRIVES,
                                "the selection is resolved to leaf ids before publishing"),
                        use("portfolio", "Portfolio", "book", UsageRole.FOLLOWS,
                                "filters to the selected subtree"),
                        use("trade-blotter", "Trade Blotter", "book", UsageRole.FOLLOWS,
                                "filters, and groups by leaf when the selection spans several"))),
                simpleId("ActorId", "a human or automation that can appear in a journal", List.of(
                        use("trade-blotter", "Trade Blotter", "book", UsageRole.RENDERS,
                                "who booked it"),
                        use("audit-explorer", "Audit Explorer", "audit", UsageRole.FOLLOWS,
                                "filter the tape by actor"))),
                simpleId("SliceId", "the coherent market snapshot a figure was priced on", List.of(
                        use("pricer", "Pricer", "trader", UsageRole.RENDERS, "the P1 lineage stamp"),
                        use("trade-blotter", "Trade Blotter", "book", UsageRole.RENDERS,
                                "every trade carries the slice it was priced on"))),
                new DataType(id("ModelRef"), "id.ModelRef", Stratum.IDENTITY,
                        OntologyMarker.VALUE_OBJECT, 1,
                        "A pricing model at a version. Both halves are identity: "
                                + "\"priced by VV\" is not an answer an auditor accepts. " + why,
                        List.of(),
                        List.of(use("pricer", "Pricer", "trader", UsageRole.RENDERS,
                                        "the model badge (P3)"),
                                use("model-inventory", "Model Inventory", "governance", UsageRole.DRIVES,
                                        "the reverse query: everything priced by model X vY"))),
                simpleId("JournalId", "one append-only journal", List.of(
                        use("audit-explorer", "Audit Explorer", "audit", UsageRole.FOLLOWS,
                                "the journal filter"))),
                simpleId("JournalEntryId", "a single fact within a journal", List.of()));
    }

    private DataType simpleId(final String name, final String what, final List<WidgetUsage> usages) {
        return new DataType(id(name), "id." + name, Stratum.IDENTITY,
                OntologyMarker.VALUE_OBJECT, 1,
                "Identity of " + what + ".", List.of(), usages);
    }

    // ------------------------------------------------------- stratum 1: quantity

    private List<DataType> quantities() {
        return List.of(
                qty("Money", "An amount with its currency. Never a formatted string, "
                        + "never a bare double — formatting belongs to the UI kit.",
                        OntologyMarker.VALUE_OBJECT, List.of(
                        use("portfolio", "Portfolio", "book", UsageRole.RENDERS, "PV column"),
                        use("trade-blotter", "Trade Blotter", "book", UsageRole.RENDERS,
                                "premium at booking, and the aggregated Σ subtotals"),
                        use("pricer", "Pricer", "trader", UsageRole.RENDERS, "the price"))),
                qty("Vol", "An implied volatility in vol points. Distinct from a bare double so "
                        + "a vol can never be silently added to a strike or a delta.",
                        OntologyMarker.VALUE_OBJECT, List.of(
                        use("surface-manager", "Surface Manager", "trader", UsageRole.RENDERS,
                                "smile points and the pillar table"),
                        use("pricer", "Pricer", "trader", UsageRole.RENDERS, "vol used"))),
                qty("Greeks", "The risk of a holding, with smile-bucket vega split ATM/RR/BF so "
                        + "the buckets sum to total vega — an invariant the gate checks.",
                        OntologyMarker.VALUE_OBJECT, List.of(
                        use("risk-blotter", "Risk Blotter", "trader", UsageRole.RENDERS,
                                "the smile-bucket vega columns"),
                        use("portfolio", "Portfolio", "book", UsageRole.RENDERS, "Δ and vega columns"),
                        use("pricer", "Pricer", "trader", UsageRole.RENDERS, "the Greeks panel"))),
                qty("Fraction", "A bounded fraction in [0,1] — reval budget, limit utilisation. "
                        + "The bound a UI meter relies on is checked here, not hoped for.",
                        OntologyMarker.VALUE_OBJECT, List.of(
                        use("risk-blotter", "Risk Blotter", "trader", UsageRole.RENDERS,
                                "freshness / budget meters"),
                        use("portfolio", "Portfolio", "book", UsageRole.RENDERS, "per-position budget"))),
                qty("Pips", "A distance in pips. Always derived from a level difference and the "
                        + "pair's pip size — no screen may author it.",
                        OntologyMarker.VALUE_OBJECT, List.of(
                        use("barrier-watch", "Barrier Watch", "trader", UsageRole.RENDERS,
                                "distance to trigger, sorted by proximity"),
                        use("portfolio", "Portfolio", "book", UsageRole.RENDERS, "barrier column"))),
                qty("AsOf", "The instant the dataset is pinned to. Nothing in the module reads a "
                        + "clock: every age and countdown is a difference against this.",
                        OntologyMarker.VALUE_OBJECT, List.of(
                        use("risk-views", "Risk Views", "risk", UsageRole.RENDERS,
                                "the explicit NOT-live as-of chip"))),
                qty("Ccy", "A settlement currency — an enum, so the value is transitively "
                        + "immutable and the demo's universe stays closed.",
                        OntologyMarker.ENUM, List.of()));
    }

    private DataType qty(final String name, final String summary, final OntologyMarker marker,
                         final List<WidgetUsage> usages) {
        return new DataType(id(name), "qty." + name, Stratum.QUANTITY, marker, 1,
                summary, List.of(), usages);
    }

    // ------------------------------------------------------ stratum 2: reference

    private List<DataType> reference() {
        return List.of(
                new DataType(id("CurrencyPair"), "ref.CurrencyPair", Stratum.REFERENCE,
                        OntologyMarker.VALUE_OBJECT, 1,
                        "A tradable pair and its conventions (premium ccy, delta convention, pip "
                                + "size, standard cut). Ring-2 reference data: the trader reads it, "
                                + "only governance changes it.",
                        List.of(rel("CurrencyPair", "id", "PairId", Cardinality.ONE,
                                "identity"),
                                rel("CurrencyPair", "premiumCcy", "Ccy", Cardinality.ONE,
                                        "which currency the premium settles in")),
                        List.of(use("surface-manager", "Surface Manager", "trader", UsageRole.RENDERS,
                                        "conventions shown read-only (P3)"),
                                use("pricer", "Pricer", "trader", UsageRole.RENDERS,
                                        "inherited ticket conventions"))),
                new DataType(id("Tenor"), "ref.Tenor", Stratum.REFERENCE,
                        OntologyMarker.VALUE_OBJECT, 1,
                        "A point on the standard tenor ladder; days is the maths, the id is the label.",
                        List.of(rel("Tenor", "id", "TenorId", Cardinality.ONE, "identity")),
                        List.of(use("surface-manager", "Surface Manager", "trader", UsageRole.RENDERS,
                                "the tenor strip"))),
                new DataType(id("PortfolioNode"), "ref.PortfolioNode", Stratum.REFERENCE,
                        OntologyMarker.VALUE_OBJECT, 1,
                        "A node of the book tree: desk → book → portfolio. leafIds() is the whole "
                                + "connection mechanism — a selection at any level resolves to the "
                                + "leaves beneath it, so consumers filter by plain membership and "
                                + "never learn the tree's shape.",
                        List.of(rel("PortfolioNode", "id", "PortfolioNodeId", Cardinality.ONE,
                                        "identity"),
                                rel("PortfolioNode", "children", "PortfolioNode", Cardinality.MANY,
                                        "the 3-level hierarchy")),
                        List.of(use("portfolio-tree", "Portfolio Tree", "book", UsageRole.RENDERS,
                                        "the keyboard-navigable tree"),
                                use("risk-views", "Risk Views", "risk", UsageRole.FOLLOWS,
                                        "limits follow the same hierarchy"))),
                new DataType(id("Actor"), "ref.Actor", Stratum.REFERENCE,
                        OntologyMarker.VALUE_OBJECT, 1,
                        "Whoever can appear in a journal entry — automations included, because "
                                + "\"rule QW-3 widened the spread\" is an action with an author. That is "
                                + "what lets automatic and manual changes share one stream.",
                        List.of(rel("Actor", "id", "ActorId", Cardinality.ONE, "identity"),
                                rel("Actor", "persona", "Persona", Cardinality.ONE,
                                        "which role this actor plays")),
                        List.of(use("audit-explorer", "Audit Explorer", "audit", UsageRole.RENDERS,
                                "who did it"))),
                new DataType(id("Persona"), "ref.Persona", Stratum.REFERENCE, OntologyMarker.ENUM, 1,
                        "Who an actor is. Grows one era at a time — the enum is a readable history "
                                + "of the desk's growth (Era 1: TRADER, AUTOMATION).",
                        List.of(), List.of()),
                new DataType(id("Cut"), "ref.Cut", Stratum.REFERENCE, OntologyMarker.ENUM, 1,
                        "A named expiry cut (NY 10am, Tokyo 3pm, London 4pm) — zone-explicit by "
                                + "construction, per the follow-the-sun NFR.",
                        List.of(),
                        List.of(use("expiry-clusters", "Expiry / Pins", "trader", UsageRole.RENDERS,
                                "cut clusters are the bulk-processing unit"))),
                new DataType(id("DeltaConvention"), "ref.DeltaConvention", Stratum.REFERENCE,
                        OntologyMarker.ENUM, 1,
                        "How a pair's deltas are quoted; inherited by every ticket (Ring 2, read-only).",
                        List.of(), List.of()),
                new DataType(id("PortfolioKind"), "ref.PortfolioKind", Stratum.REFERENCE,
                        OntologyMarker.ENUM, 1,
                        "A node's level in the book tree — named rather than positional, so a "
                                + "fourth level could be inserted without renumbering anything.",
                        List.of(), List.of()),

                // --- Era 2: the quant arrives, and models become objects the desk does not own.
                new DataType(id("Model"), "ref.Model", Stratum.REFERENCE,
                        OntologyMarker.VALUE_OBJECT, 2,
                        "A named pricing model as an entity — status, owner — distinct from the "
                                + "ModelRef stamped on figures. The split is load-bearing: a retired "
                                + "model must stay nameable because old trades carry its stamp, so "
                                + "lineage points at the ref and never at this record. Ring 1: the "
                                + "trader selects from what the matrix allows and cannot change any "
                                + "of it — structurally, because there is no write path here.",
                        List.of(rel("Model", "ref", "ModelRef", Cardinality.ONE,
                                        "the identity historical figures carry"),
                                rel("Model", "status", "ModelStatus", Cardinality.ONE,
                                        "may it publish at all"),
                                rel("Model", "owner", "ActorId", Cardinality.ONE,
                                        "the quant accountable for the methodology")),
                        List.of(use("model-inventory", "Model Inventory", "governance", UsageRole.RENDERS,
                                        "every named strategy, version, status"),
                                use("calibration-lab", "Calibration Lab", "quant", UsageRole.RENDERS,
                                        "model basis: stream vs official"),
                                use("change-console", "Change Console", "governance", UsageRole.RENDERS,
                                        "the subject of a promotion package"))),
                new DataType(id("ModelStatus"), "ref.ModelStatus", Stratum.REFERENCE,
                        OntologyMarker.ENUM, 2,
                        "Where a model stands. CANDIDATE is the value that matters: a model that "
                                + "exists, runs in shadow, and whose numbers may not be published.",
                        List.of(),
                        List.of(use("model-inventory", "Model Inventory", "governance", UsageRole.RENDERS,
                                "the status chip"))),
                new DataType(id("InstrumentClass"), "ref.InstrumentClass", Stratum.REFERENCE,
                        OntologyMarker.ENUM, 2,
                        "The granularity the selection matrix works at — coarser than OptionType, "
                                + "because methodology is chosen per class of payoff, not per contract "
                                + "shape.",
                        List.of(), List.of()),
                new DataType(id("PricingPurpose"), "ref.PricingPurpose", Stratum.REFERENCE,
                        OntologyMarker.ENUM, 2,
                        "What a price is for — the other axis of the matrix. The same class may "
                                + "use a fast model to stream and a richer one to mark officially; "
                                + "change package #412 turns on exactly this.",
                        List.of(), List.of()),
                new DataType(id("SelectionMatrixRow"), "ref.SelectionMatrixRow", Stratum.REFERENCE,
                        OntologyMarker.VALUE_OBJECT, 2,
                        "Which model prices which instrument class, for which purpose, on which "
                                + "pairs. This is why a trader's model badge is a consequence rather "
                                + "than a choice: the pricer resolves through this row and stamps what "
                                + "it finds. Ring 2 — Era 2 lets the quant set it directly; Era 3 "
                                + "routes every edit through a change package with four-eyes and "
                                + "epoch-staged activation. The type does not change; only who may "
                                + "produce a new one.",
                        List.of(rel("SelectionMatrixRow", "model", "ModelRef", Cardinality.ONE,
                                        "what will actually be used"),
                                rel("SelectionMatrixRow", "instrumentClass", "InstrumentClass",
                                        Cardinality.ONE, "which payoffs"),
                                rel("SelectionMatrixRow", "purpose", "PricingPurpose", Cardinality.ONE,
                                        "stream / RFQ / official / risk"),
                                rel("SelectionMatrixRow", "pairs", "PairId", Cardinality.ONE_OR_MANY,
                                        "the scope it governs")),
                        List.of(use("change-console", "Change Console", "governance", UsageRole.RENDERS,
                                        "the semantic diff is expressed in these rows"),
                                use("model-inventory", "Model Inventory", "governance", UsageRole.RENDERS,
                                        "where the matrix uses each model"),
                                use("pricer", "Pricer", "trader", UsageRole.DERIVES_FROM,
                                        "resolves the model badge it stamps"))),

                // --- Era 3: validation arrives, and approval becomes evidence.
                new DataType(id("ValidationRecord"), "ref.ValidationRecord", Stratum.REFERENCE,
                        OntologyMarker.VALUE_OBJECT, 3,
                        "An independent validation of a model VERSION: who decided, when, with what "
                                + "outcome, against which document, until when. This is what makes "
                                + "APPROVED mean something — without it, approval is an adjective "
                                + "somebody typed. Keyed on ModelRef, so shipping v2.4 inherits "
                                + "nothing from v2.3.",
                        List.of(rel("ValidationRecord", "model", "ModelRef", Cardinality.ONE,
                                        "the version approved — never the entity"),
                                rel("ValidationRecord", "validator", "ActorId", Cardinality.ONE,
                                        "independent of the owner, by construction"),
                                rel("ValidationRecord", "outcome", "ValidationOutcome", Cardinality.ONE,
                                        "yes, no, or yes-within-bounds")),
                        List.of(use("model-inventory", "Model Inventory", "governance", UsageRole.RENDERS,
                                        "validation doc + the revalidation worklist ageing"),
                                use("change-console", "Change Console", "governance", UsageRole.RENDERS,
                                        "the evidence attached to a package"))),
                new DataType(id("ValidationOutcome"), "ref.ValidationOutcome", Stratum.REFERENCE,
                        OntologyMarker.ENUM, 3,
                        "PENDING / APPROVED / APPROVED_WITH_CONDITIONS / REJECTED. Real validation "
                                + "rarely says yes or no — it says yes, within these bounds, and a "
                                + "model whose conditions are invisible is one whose limits get "
                                + "forgotten.",
                        List.of(), List.of()));
    }

    // -------------------------------------------------- stratum 3: market state

    private List<DataType> marketState() {
        return List.of(
                new DataType(id("MarketSlice"), "market.MarketSlice", Stratum.MARKET_STATE,
                        OntologyMarker.VALUE_OBJECT, 1,
                        "The coherent market snapshot every consumer prices against — the seed of "
                                + "P5. A lone trader could have used ambient globals; the moment a "
                                + "second consumer exists, \"the market\" must be a named shared thing "
                                + "or two screens disagree and neither is wrong.",
                        List.of(rel("MarketSlice", "id", "SliceId", Cardinality.ONE, "identity"),
                                rel("MarketSlice", "asOf", "AsOf", Cardinality.ONE, "the pinned instant"),
                                rel("MarketSlice", "spots", "SpotRate", Cardinality.MANY, "spot per pair"),
                                rel("MarketSlice", "volMarks", "VolMark", Cardinality.MANY,
                                        "the trader's marks per pair/tenor")),
                        List.of(use("pricer", "Pricer", "trader", UsageRole.DERIVES_FROM,
                                        "prices off the slice"),
                                use("barrier-watch", "Barrier Watch", "trader", UsageRole.DERIVES_FROM,
                                        "spot is half of every barrier distance"))),
                new DataType(id("SpotRate"), "market.SpotRate", Stratum.MARKET_STATE,
                        OntologyMarker.VALUE_OBJECT, 1,
                        "Spot for one pair, as of the slice that carries it.",
                        List.of(rel("SpotRate", "pair", "PairId", Cardinality.ONE, "which pair")),
                        List.of(use("barrier-watch", "Barrier Watch", "trader", UsageRole.DERIVES_FROM,
                                "distance = |barrier − spot| ÷ pip size"))),
                new DataType(id("VolMark"), "market.VolMark", Stratum.MARKET_STATE,
                        OntologyMarker.VALUE_OBJECT, 1,
                        "The trader's own mark for a pair/tenor (ATM, 25Δ RR, 25Δ BF). Era 1 has "
                                + "nothing better: no fitted surface, no epoch, no provenance. Era 2 is "
                                + "precisely the argument that this is not enough.",
                        List.of(rel("VolMark", "pair", "PairId", Cardinality.ONE, "which pair"),
                                rel("VolMark", "tenor", "TenorId", Cardinality.ONE, "which tenor")),
                        List.of(use("surface-manager", "Surface Manager", "trader", UsageRole.RENDERS,
                                "the smile the desk is marking"))));
    }

    // ---------------------------------------------------------- stratum 4: book

    private List<DataType> book() {
        return List.of(
                new DataType(id("Instrument"), "book.Instrument", Stratum.BOOK,
                        OntologyMarker.VALUE_OBJECT, 1,
                        "What was traded — the contract, independent of who holds it. Separating "
                                + "instrument from trade is what lets several trades roll into one "
                                + "position, and lets pricer, blotter, barrier watch and expiry "
                                + "clusters all speak about the same object.",
                        List.of(rel("Instrument", "id", "InstrumentId", Cardinality.ONE, "identity"),
                                rel("Instrument", "pair", "PairId", Cardinality.ONE, "underlying"),
                                rel("Instrument", "tenor", "TenorId", Cardinality.ONE, "ladder point"),
                                rel("Instrument", "barrier", "Barrier", Cardinality.ZERO_OR_ONE,
                                        "present only for exotics"),
                                rel("Instrument", "cut", "Cut", Cardinality.ONE, "expiry cut"),
                                rel("Instrument", "notional", "Money", Cardinality.ONE, "size")),
                        List.of(use("pricer", "Pricer", "trader", UsageRole.RENDERS, "the parsed ticket"),
                                use("portfolio", "Portfolio", "book", UsageRole.RENDERS,
                                        "the instrument description per row"),
                                use("expiry-clusters", "Expiry / Pins", "trader", UsageRole.DERIVES_FROM,
                                        "expiry + cut make the clusters"))),
                new DataType(id("Barrier"), "book.Barrier", Stratum.BOOK,
                        OntologyMarker.VALUE_OBJECT, 1,
                        "A barrier feature. Proximity to spot is never stored here: it is derived "
                                + "from this level, the slice's spot and the pair's pip size, which is "
                                + "why three screens show the same distance without any owning it.",
                        List.of(rel("Barrier", "type", "BarrierType", Cardinality.ONE, "KO/KI/NT/OT"),
                                rel("Barrier", "monitoring", "Monitoring", Cardinality.ONE,
                                        "how it is observed")),
                        List.of(use("barrier-watch", "Barrier Watch", "trader", UsageRole.RENDERS,
                                "the whole widget is this type"))),
                new DataType(id("Lineage"), "book.Lineage", Stratum.BOOK,
                        OntologyMarker.VALUE_OBJECT, 1,
                        "P1 made structural: what a figure was produced from. Era 1 carries slice "
                                + "and model — already enough to make the reverse query a join rather "
                                + "than a hope. Era 2 widens it with the surface epoch.",
                        List.of(rel("Lineage", "slice", "SliceId", Cardinality.ONE, "the snapshot"),
                                rel("Lineage", "model", "ModelRef", Cardinality.ONE, "model + version")),
                        List.of(use("pricer", "Pricer", "trader", UsageRole.RENDERS, "the stamp"),
                                use("trade-blotter", "Trade Blotter", "book", UsageRole.RENDERS,
                                        "each trade's stamp at booking"),
                                use("model-inventory", "Model Inventory", "governance",
                                        UsageRole.DERIVES_FROM, "the reverse query joins on this"))),
                new DataType(id("Trade"), "book.Trade", Stratum.BOOK,
                        OntologyMarker.VALUE_OBJECT, 1,
                        "An execution: this instrument, into this portfolio, by this actor, priced "
                                + "off this lineage. Captured at booking and never recomputed — which "
                                + "is what makes risk and the eventual P&L explain agree with what the "
                                + "trader saw. Immutable: amendment is a later journal fact, not a "
                                + "mutation.",
                        List.of(rel("Trade", "id", "TradeId", Cardinality.ONE, "identity"),
                                rel("Trade", "instrument", "InstrumentId", Cardinality.ONE, "what"),
                                rel("Trade", "portfolio", "PortfolioNodeId", Cardinality.ONE,
                                        "the leaf it books into"),
                                rel("Trade", "bookedBy", "ActorId", Cardinality.ONE, "who"),
                                rel("Trade", "lineage", "Lineage", Cardinality.ONE, "priced off what")),
                        List.of(use("trade-blotter", "Trade Blotter", "book", UsageRole.RENDERS,
                                        "the journal rendered"),
                                use("portfolio", "Portfolio", "book", UsageRole.FOLLOWS,
                                        "position → trade drill"),
                                use("lifecycle", "Lifecycle", "middle-office", UsageRole.RENDERS,
                                        "the subject of lifecycle events"))),
                new DataType(id("Position"), "book.Position", Stratum.BOOK,
                        OntologyMarker.VALUE_OBJECT, 1,
                        "A holding: the instrument, the trades that built it, and its risk as of "
                                + "the slice. The middle rung of the drill chain. revalBudget and "
                                + "lastFullReval are the two-speed machinery made honest (P2) — the "
                                + "position carries the evidence rather than a caption.",
                        List.of(rel("Position", "id", "PositionId", Cardinality.ONE, "identity"),
                                rel("Position", "instrument", "InstrumentId", Cardinality.ONE, "what"),
                                rel("Position", "portfolio", "PortfolioNodeId", Cardinality.ONE,
                                        "the leaf that holds it"),
                                rel("Position", "contributingTrades", "TradeId", Cardinality.ONE_OR_MANY,
                                        "position → trade drill"),
                                rel("Position", "greeks", "Greeks", Cardinality.ONE, "its risk"),
                                rel("Position", "revalBudget", "Fraction", Cardinality.ONE,
                                        "two-speed freshness"),
                                rel("Position", "lineage", "Lineage", Cardinality.ONE, "P1")),
                        List.of(use("portfolio", "Portfolio", "book", UsageRole.RENDERS,
                                        "position level, the anchor widget"),
                                use("risk-blotter", "Risk Blotter", "trader", UsageRole.DERIVES_FROM,
                                        "bucket rows are summed positions"),
                                use("barrier-watch", "Barrier Watch", "trader", UsageRole.DERIVES_FROM,
                                        "positions with a barrier feature"),
                                use("concentrations", "Concentrations", "risk", UsageRole.DERIVES_FROM,
                                        "density bands are bucketed positions"),
                                use("scenario-workbench", "Scenarios", "risk", UsageRole.DERIVES_FROM,
                                        "scenario results are repriced positions"))),
                new DataType(id("OptionType"), "book.OptionType", Stratum.BOOK, OntologyMarker.ENUM, 1,
                        "What kind of contract — first-generation FX options coverage.",
                        List.of(), List.of()),
                new DataType(id("BarrierType"), "book.BarrierType", Stratum.BOOK, OntologyMarker.ENUM, 1,
                        "KO / KI / NT / OT.", List.of(), List.of()),
                new DataType(id("Monitoring"), "book.Monitoring", Stratum.BOOK, OntologyMarker.ENUM, 1,
                        "How a barrier is observed; drives the determination workflow later.",
                        List.of(), List.of()));
    }

    // ------------------------------------------------------- stratum 5: journal

    private List<DataType> journal() {
        return List.of(
                new DataType(id("JournalEntry"), "journal.JournalEntry", Stratum.JOURNAL,
                        OntologyMarker.VALUE_OBJECT, 1,
                        "One immutable fact: who did what, when. This is the reason nothing in the "
                                + "dataset is Mutable — every change is another entry, and current "
                                + "state is a fold. It is also what leaves time travel (P4) as a "
                                + "matter of folding to an earlier instant.",
                        List.of(rel("JournalEntry", "id", "JournalEntryId", Cardinality.ONE, "identity"),
                                rel("JournalEntry", "journal", "JournalId", Cardinality.ONE, "which journal"),
                                rel("JournalEntry", "actor", "ActorId", Cardinality.ONE, "who"),
                                rel("JournalEntry", "payload", "JournalPayload", Cardinality.ONE, "what")),
                        List.of(use("audit-explorer", "Audit Explorer", "audit", UsageRole.RENDERS,
                                        "the tape is the union of journals"),
                                use("trade-blotter", "Trade Blotter", "book", UsageRole.RENDERS,
                                        "amendment history unfolds under a trade"))),
                new DataType(id("JournalPayload"), "journal.JournalPayload", Stratum.JOURNAL,
                        OntologyMarker.SEALED_INTERFACE, 1,
                        "What an entry says. Sealed on purpose: every fold over history switches "
                                + "over these kinds, and sealing makes the compiler reject a fold that "
                                + "forgets one. The permits clause grows era by era — a precise record "
                                + "of what the desk became able to do.",
                        List.of(), List.of()),
                new DataType(id("TradeBooked"), "journal.TradeBooked", Stratum.JOURNAL,
                        OntologyMarker.VALUE_OBJECT, 1,
                        "A trade was booked, at this premium, off this lineage. Premium and lineage "
                                + "are repeated rather than only referenced: a journal fact must stay "
                                + "evidential on its own, without resolving anything's current state.",
                        List.of(rel("TradeBooked", "trade", "TradeId", Cardinality.ONE, "subject"),
                                rel("TradeBooked", "lineage", "Lineage", Cardinality.ONE, "priced off")),
                        List.of(use("audit-explorer", "Audit Explorer", "audit", UsageRole.RENDERS,
                                "the Era-1 tape is entirely this kind"))));
    }

    // ------------------------------------------------------- stratum 6: derived

    private List<DataType> derived() {
        return List.of(
                new DataType(id("BucketRow"), "derive.BucketRow", Stratum.DERIVED,
                        OntologyMarker.VALUE_OBJECT, 1,
                        "A risk-blotter row: the book bucketed pair ▸ tenor. Derived, never "
                                + "authored — the row can only come from Derivations.buckets(...), so "
                                + "it cannot disagree with the positions it summarises.",
                        List.of(rel("BucketRow", "pair", "PairId", Cardinality.ONE, "the group"),
                                rel("BucketRow", "greeks", "Greeks", Cardinality.ONE, "summed risk")),
                        List.of(use("risk-blotter", "Risk Blotter", "trader", UsageRole.RENDERS,
                                "every row of the blotter"))),
                new DataType(id("DeskTotals"), "derive.DeskTotals", Stratum.DERIVED,
                        OntologyMarker.VALUE_OBJECT, 1,
                        "Totals over a scope (all positions, or a selected subtree). The invariant "
                                + "that positions sum to buckets sum to totals is enforced by the gate, "
                                + "not by an author remembering three places.",
                        List.of(rel("DeskTotals", "greeks", "Greeks", Cardinality.ONE, "summed risk")),
                        List.of(use("risk-blotter", "Risk Blotter", "trader", UsageRole.RENDERS,
                                        "the totals strip"),
                                use("summary-dashboard", "Summary", "summary", UsageRole.RENDERS,
                                        "a projection, never a parallel truth (P5)"))));
    }

    // ----------------------------------------------------- stratum 7: behaviour

    private List<DataType> behaviour() {
        return List.of(
                new DataType(id("DeskDataset"), "DeskDataset", Stratum.BEHAVIOUR,
                        OntologyMarker.FUNCTIONAL_OBJECT, 1,
                        "The dataset root: all the facts, plus the resolution functions "
                                + "parameterised by them. Every lookup goes through here, which is what "
                                + "makes the join contract a contract — a feed cannot resolve an id any "
                                + "way other than the way every other feed does.",
                        List.of(), List.of()),
                new DataType(id("Derivations"), "Derivations", Stratum.BEHAVIOUR,
                        OntologyMarker.STATELESS_FUNCTIONAL_OBJECT, 1,
                        "Every aggregate the screens show, computed from positions. Zero instance "
                                + "fields: there is exactly one implementation of \"the desk's vega\" "
                                + "and no screen can hold a private opinion.",
                        List.of(), List.of()),
                new DataType(id("DataOntology"), "meta.DataOntology", Stratum.BEHAVIOUR,
                        OntologyMarker.STATELESS_FUNCTIONAL_OBJECT, 1,
                        "This catalogue: the ontology of the ontology. Declaring the type graph as "
                                + "data is what lets the demo browse its own data model, and turns "
                                + "\"which screens break if I change this type?\" into a query.",
                        List.of(), List.of()));
    }
}
