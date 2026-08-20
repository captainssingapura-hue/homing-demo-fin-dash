package hue.captains.singapura.js.homing.findash.data.journal;

import hue.captains.singapura.js.homing.findash.data.id.ActorId;
import hue.captains.singapura.js.homing.findash.data.id.JournalEntryId;
import hue.captains.singapura.js.homing.findash.data.id.JournalId;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Stratum 5 — one immutable fact in an append-only journal: who did what, when.
 *
 * <p>This record is the reason nothing in the dataset is {@code Mutable}
 * (requirements §3.1). Every change the desk can make is expressed as another
 * entry; "current state" is a fold over these, never a stored mutable object.
 * That is what makes the audit tape a union rather than a maintained artefact,
 * and what leaves time travel (P4) as a matter of folding to an earlier
 * instant instead of a redesign.</p>
 *
 * <p>{@code reason} is domain content, not presentation: a Ring-3 action is
 * required to capture why, and the auditor reads exactly what the actor
 * typed.</p>
 */
public record JournalEntry(JournalEntryId id, JournalId journal, Instant at,
                           ActorId actor, JournalPayload payload,
                           Optional<String> reason) implements ValueObject {

    public JournalEntry {
        Objects.requireNonNull(id, "JournalEntry.id");
        Objects.requireNonNull(journal, "JournalEntry.journal");
        Objects.requireNonNull(at, "JournalEntry.at");
        Objects.requireNonNull(actor, "JournalEntry.actor");
        Objects.requireNonNull(payload, "JournalEntry.payload");
        Objects.requireNonNull(reason, "JournalEntry.reason (use Optional.empty())");
    }
}
