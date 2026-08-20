package hue.captains.singapura.js.homing.findash.data.book;

import hue.captains.singapura.js.homing.findash.data.id.InstrumentId;
import hue.captains.singapura.js.homing.findash.data.id.PortfolioNodeId;
import hue.captains.singapura.js.homing.findash.data.id.PositionId;
import hue.captains.singapura.js.homing.findash.data.id.TradeId;
import hue.captains.singapura.js.homing.findash.data.qty.Fraction;
import hue.captains.singapura.js.homing.findash.data.qty.Greeks;
import hue.captains.singapura.js.homing.findash.data.qty.Money;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Stratum 4 — <b>a holding</b>: the instrument, the trades that built it, and
 * its risk as of the slice. The middle rung of the universal drill chain
 * (aggregate → position → trade → explain).
 *
 * <p>{@code revalBudget} and {@code lastFullReval} are the two-speed machinery
 * made honest (P2): a screen can always say whether a number is freshly exact
 * or Taylor-tracked, because the position carries the evidence rather than a
 * caption.</p>
 */
public record Position(PositionId id, InstrumentId instrument, PortfolioNodeId portfolio,
                       List<TradeId> contributingTrades, Money pv, Greeks greeks,
                       Fraction revalBudget, Instant lastFullReval,
                       Lineage lineage) implements ValueObject {

    public Position {
        Objects.requireNonNull(id, "Position.id");
        Objects.requireNonNull(instrument, "Position.instrument");
        Objects.requireNonNull(portfolio, "Position.portfolio");
        contributingTrades = List.copyOf(
                Objects.requireNonNull(contributingTrades, "Position.contributingTrades"));
        Objects.requireNonNull(pv, "Position.pv");
        Objects.requireNonNull(greeks, "Position.greeks");
        Objects.requireNonNull(revalBudget, "Position.revalBudget");
        Objects.requireNonNull(lastFullReval, "Position.lastFullReval");
        Objects.requireNonNull(lineage, "Position.lineage");
        if (contributingTrades.isEmpty()) {
            throw new IllegalArgumentException("Position " + id.value() + " has no contributing trades");
        }
    }
}
