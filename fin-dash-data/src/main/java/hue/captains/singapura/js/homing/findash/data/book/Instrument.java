package hue.captains.singapura.js.homing.findash.data.book;

import hue.captains.singapura.js.homing.findash.data.id.InstrumentId;
import hue.captains.singapura.js.homing.findash.data.id.PairId;
import hue.captains.singapura.js.homing.findash.data.id.TenorId;
import hue.captains.singapura.js.homing.findash.data.qty.Money;
import hue.captains.singapura.js.homing.findash.data.ref.Cut;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

/**
 * Stratum 4 — <b>what was traded</b>: the contract definition, independent of
 * who holds it or when it was booked. Separating the instrument from the trade
 * is what lets several trades roll into one position, and lets the pricer,
 * blotter, barrier watch, and expiry clusters all speak about the same object.
 */
public record Instrument(InstrumentId id, PairId pair, TenorId tenor, OptionType type,
                         double strike, Optional<Barrier> barrier,
                         LocalDate expiry, Cut cut, Money notional) implements ValueObject {

    public Instrument {
        Objects.requireNonNull(id, "Instrument.id");
        Objects.requireNonNull(pair, "Instrument.pair");
        Objects.requireNonNull(tenor, "Instrument.tenor");
        Objects.requireNonNull(type, "Instrument.type");
        Objects.requireNonNull(barrier, "Instrument.barrier (use Optional.empty())");
        Objects.requireNonNull(expiry, "Instrument.expiry");
        Objects.requireNonNull(cut, "Instrument.cut");
        Objects.requireNonNull(notional, "Instrument.notional");
    }

    public boolean isBarrier() { return barrier.isPresent(); }
}
