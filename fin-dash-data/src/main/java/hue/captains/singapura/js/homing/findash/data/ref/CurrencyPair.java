package hue.captains.singapura.js.homing.findash.data.ref;

import hue.captains.singapura.js.homing.findash.data.id.PairId;
import hue.captains.singapura.js.homing.findash.data.qty.Ccy;
import hue.captains.singapura.js.homing.findash.data.qty.Pips;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/**
 * Stratum 2 — a tradable pair and its conventions. Ring-2 reference data: the
 * trader reads it, only governance changes it (the UI renders these fields
 * read-only, which is P3 made structural rather than styled).
 *
 * <p>{@code pipSize} is here so barrier proximity is <b>derived</b> (R1) rather
 * than authored: no screen may hand-write "12 pips".</p>
 */
public record CurrencyPair(PairId id, Ccy base, Ccy quote, double pipSize,
                           Ccy premiumCcy, DeltaConvention deltaConvention,
                           Cut standardCut) implements ValueObject {

    public CurrencyPair {
        Objects.requireNonNull(id, "CurrencyPair.id");
        Objects.requireNonNull(base, "CurrencyPair.base");
        Objects.requireNonNull(quote, "CurrencyPair.quote");
        Objects.requireNonNull(premiumCcy, "CurrencyPair.premiumCcy");
        Objects.requireNonNull(deltaConvention, "CurrencyPair.deltaConvention");
        Objects.requireNonNull(standardCut, "CurrencyPair.standardCut");
        if (pipSize <= 0) {
            throw new IllegalArgumentException("CurrencyPair.pipSize must be positive");
        }
    }

    /** Distance between two levels of this pair, in pips. The only way pips are produced. */
    public Pips pipsBetween(final double a, final double b) {
        return new Pips(Math.abs(a - b) / pipSize);
    }
}
