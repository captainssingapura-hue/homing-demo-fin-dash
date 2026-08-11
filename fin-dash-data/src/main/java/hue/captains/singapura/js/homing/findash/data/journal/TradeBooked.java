package hue.captains.singapura.js.homing.findash.data.journal;

import hue.captains.singapura.js.homing.findash.data.book.Lineage;
import hue.captains.singapura.js.homing.findash.data.id.TradeId;
import hue.captains.singapura.js.homing.findash.data.qty.Money;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/**
 * Stratum 5, Era 1 — a trade was booked, at this premium, off this lineage.
 *
 * <p>The premium and lineage are repeated here rather than only referenced
 * through the trade, deliberately: a journal fact must remain readable and
 * evidential on its own, without resolving the current state of anything.
 * That is what lets the tape be replayed to reconstruct a past view (P4).</p>
 */
public record TradeBooked(TradeId trade, Money premium, Lineage lineage)
        implements JournalPayload, ValueObject {

    public TradeBooked {
        Objects.requireNonNull(trade, "TradeBooked.trade");
        Objects.requireNonNull(premium, "TradeBooked.premium");
        Objects.requireNonNull(lineage, "TradeBooked.lineage");
    }

    @Override
    public String kind() { return "TradeBooked"; }
}
