package hue.captains.singapura.js.homing.findash.data;

import hue.captains.singapura.js.homing.findash.data.book.Instrument;
import hue.captains.singapura.js.homing.findash.data.book.Position;
import hue.captains.singapura.js.homing.findash.data.book.Trade;
import hue.captains.singapura.js.homing.findash.data.id.InstrumentId;
import hue.captains.singapura.js.homing.findash.data.id.PairId;
import hue.captains.singapura.js.homing.findash.data.id.PortfolioNodeId;
import hue.captains.singapura.js.homing.findash.data.id.PositionId;
import hue.captains.singapura.js.homing.findash.data.id.TenorId;
import hue.captains.singapura.js.homing.findash.data.id.TradeId;
import hue.captains.singapura.js.homing.findash.data.journal.JournalEntry;
import hue.captains.singapura.js.homing.findash.data.market.MarketSlice;
import hue.captains.singapura.js.homing.findash.data.ref.Actor;
import hue.captains.singapura.js.homing.findash.data.ref.CurrencyPair;
import hue.captains.singapura.js.homing.findash.data.ref.PortfolioNode;
import hue.captains.singapura.js.homing.findash.data.ref.Tenor;
import hue.captains.singapura.tao.ontology.FunctionalObject;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Stratum 7 — <b>the dataset root</b>: all the facts, plus the resolution
 * functions parameterised by them. A {@code FunctionalObject} rather than a
 * {@code ValueObject} because it exists to be <i>asked questions</i>; the
 * facts are its immutable configuration.
 *
 * <p>Every lookup a consumer needs goes through here, which is what makes the
 * join contract (R6) a real contract: a feed cannot resolve an id any way
 * other than the way every other feed resolves it. Nothing in this class
 * computes an aggregate — aggregation lives in {@code Derivations} so that
 * "derived, never authored" (R1) has a single enforcement point.</p>
 *
 * <p>Era 1 holds the trader's world: pairs, tenors, the book tree, actors,
 * one market slice, instruments, trades, positions, and the trade journal.</p>
 *
 * <p><b>No facts yet.</b> This is the shape and the resolution contract; the
 * dataset is populated only once the type graph has been browsed and validated
 * end-to-end in the Data Ontology workspace. Building the shape first is
 * deliberate: a join that cannot be expressed here is a modelling defect, and
 * it is far cheaper to find that now than after authoring a hundred
 * positions.</p>
 */
public final class DeskDataset implements FunctionalObject {

    private final MarketSlice slice;
    private final PortfolioNode bookTree;
    private final List<CurrencyPair> pairs;
    private final List<Tenor> tenors;
    private final List<Actor> actors;
    private final List<Instrument> instruments;
    private final List<Trade> trades;
    private final List<Position> positions;
    private final List<JournalEntry> journal;

    public DeskDataset(final MarketSlice slice, final PortfolioNode bookTree,
                       final List<CurrencyPair> pairs, final List<Tenor> tenors,
                       final List<Actor> actors, final List<Instrument> instruments,
                       final List<Trade> trades, final List<Position> positions,
                       final List<JournalEntry> journal) {
        this.slice = Objects.requireNonNull(slice, "slice");
        this.bookTree = Objects.requireNonNull(bookTree, "bookTree");
        this.pairs = List.copyOf(Objects.requireNonNull(pairs, "pairs"));
        this.tenors = List.copyOf(Objects.requireNonNull(tenors, "tenors"));
        this.actors = List.copyOf(Objects.requireNonNull(actors, "actors"));
        this.instruments = List.copyOf(Objects.requireNonNull(instruments, "instruments"));
        this.trades = List.copyOf(Objects.requireNonNull(trades, "trades"));
        this.positions = List.copyOf(Objects.requireNonNull(positions, "positions"));
        this.journal = List.copyOf(Objects.requireNonNull(journal, "journal"));
    }

    // ---------------------------------------------------------------- facts

    public MarketSlice slice()             { return slice; }
    public PortfolioNode bookTree()        { return bookTree; }
    public List<CurrencyPair> pairs()      { return pairs; }
    public List<Tenor> tenors()            { return tenors; }
    public List<Actor> actors()            { return actors; }
    public List<Instrument> instruments()  { return instruments; }
    public List<Trade> trades()            { return trades; }
    public List<Position> positions()      { return positions; }
    public List<JournalEntry> journal()    { return journal; }

    // ------------------------------------------------- the join contract (R6)

    public Optional<CurrencyPair> pair(final PairId id) {
        return pairs.stream().filter(p -> p.id().equals(id)).findFirst();
    }

    public Optional<Tenor> tenor(final TenorId id) {
        return tenors.stream().filter(t -> t.id().equals(id)).findFirst();
    }

    public Optional<Instrument> instrument(final InstrumentId id) {
        return instruments.stream().filter(i -> i.id().equals(id)).findFirst();
    }

    public Optional<Trade> trade(final TradeId id) {
        return trades.stream().filter(t -> t.id().equals(id)).findFirst();
    }

    public Optional<Position> position(final PositionId id) {
        return positions.stream().filter(p -> p.id().equals(id)).findFirst();
    }

    public Optional<PortfolioNode> portfolio(final PortfolioNodeId id) {
        return bookTree.find(id);
    }

    /**
     * The selection primitive: the leaf-portfolio ids beneath any node. A
     * consumer that has these can filter positions and trades by plain
     * membership, whatever level the user actually picked.
     */
    public List<PortfolioNodeId> leavesOf(final PortfolioNodeId id) {
        return bookTree.find(id).map(PortfolioNode::leafIds).orElse(List.of());
    }

    /** Positions whose leaf portfolio is in the given set (the portfolio-selection join). */
    public List<Position> positionsIn(final List<PortfolioNodeId> leaves) {
        return positions.stream().filter(p -> leaves.contains(p.portfolio())).toList();
    }

    /** Trades whose leaf portfolio is in the given set. */
    public List<Trade> tradesIn(final List<PortfolioNodeId> leaves) {
        return trades.stream().filter(t -> leaves.contains(t.portfolio())).toList();
    }

    /** Positions on one pair (the instrument carries the pair, so this is a two-hop join). */
    public List<Position> positionsOn(final PairId pair) {
        return positions.stream()
                .filter(p -> instrument(p.instrument()).map(i -> i.pair().equals(pair)).orElse(false))
                .toList();
    }

    /** The trades that built a position — the position → trade rung of the drill chain. */
    public List<Trade> tradesFor(final PositionId id) {
        return position(id)
                .map(p -> p.contributingTrades().stream()
                        .map(this::trade).flatMap(Optional::stream).toList())
                .orElse(List.of());
    }

    /**
     * Everything priced by a model version — the reverse query auditors ask and
     * systems rarely answer. It is a plain join here because lineage is
     * structure, not a display string (R5).
     */
    public List<Position> pricedBy(final String model, final String version) {
        return positions.stream()
                .filter(p -> p.lineage().model().model().equals(model)
                        && p.lineage().model().version().equals(version))
                .toList();
    }

    /** Journal entries for one journal, oldest first — the tape's building block. */
    public List<JournalEntry> journal(final String journalId) {
        return journal.stream()
                .filter(e -> e.journal().value().equals(journalId))
                .sorted((a, b) -> a.at().compareTo(b.at()))
                .toList();
    }
}
