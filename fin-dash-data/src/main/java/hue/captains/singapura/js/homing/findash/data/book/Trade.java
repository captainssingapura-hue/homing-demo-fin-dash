package hue.captains.singapura.js.homing.findash.data.book;

import hue.captains.singapura.js.homing.findash.data.id.ActorId;
import hue.captains.singapura.js.homing.findash.data.id.InstrumentId;
import hue.captains.singapura.js.homing.findash.data.id.PortfolioNodeId;
import hue.captains.singapura.js.homing.findash.data.id.TradeId;
import hue.captains.singapura.js.homing.findash.data.qty.Money;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.time.Instant;
import java.util.Objects;

/**
 * Stratum 4 — <b>an execution</b>: this instrument, into this portfolio, by
 * this actor, at this instant, priced off this lineage.
 *
 * <p>The trade is the demo's anchor fact. Its {@code lineage} is captured at
 * booking and never recomputed — that is what makes the study's promise true,
 * that the risk system and the eventual P&amp;L explain agree with what the
 * trader saw at execution.</p>
 *
 * <p>A trade is immutable. Amendment is not a mutation of this record but a
 * later journal fact about it (Era 3) — see requirements R8.</p>
 */
public record Trade(TradeId id, InstrumentId instrument, PortfolioNodeId portfolio,
                    ActorId bookedBy, Instant bookedAt, Money premium,
                    Lineage lineage) implements ValueObject {

    public Trade {
        Objects.requireNonNull(id, "Trade.id");
        Objects.requireNonNull(instrument, "Trade.instrument");
        Objects.requireNonNull(portfolio, "Trade.portfolio");
        Objects.requireNonNull(bookedBy, "Trade.bookedBy");
        Objects.requireNonNull(bookedAt, "Trade.bookedAt");
        Objects.requireNonNull(premium, "Trade.premium");
        Objects.requireNonNull(lineage, "Trade.lineage");
    }
}
