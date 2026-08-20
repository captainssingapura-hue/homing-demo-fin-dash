package hue.captains.singapura.js.homing.findash.data.qty;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.time.Instant;
import java.util.Objects;

/**
 * Stratum 1 — the instant the dataset is pinned to, with the desk zone it is
 * read in. R4: there is exactly one of these, and nothing in the module calls
 * {@code Instant.now()} — every age, freshness, and countdown is a difference
 * against this value, so the same build always produces the same bytes.
 */
public record AsOf(Instant instant, String deskZone) implements ValueObject {

    public AsOf {
        Objects.requireNonNull(instant, "AsOf.instant");
        Objects.requireNonNull(deskZone, "AsOf.deskZone");
    }

    /** Seconds between an earlier instant and this as-of (never negative in a sane dataset). */
    public long secondsSince(final Instant earlier) {
        return instant.getEpochSecond() - earlier.getEpochSecond();
    }
}
