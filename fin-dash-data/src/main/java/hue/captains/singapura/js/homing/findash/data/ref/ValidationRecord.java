package hue.captains.singapura.js.homing.findash.data.ref;

import hue.captains.singapura.js.homing.findash.data.id.ActorId;
import hue.captains.singapura.js.homing.findash.data.id.ModelRef;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Stratum 2, Era 3 — <b>an independent validation of a model version</b>: who
 * decided, when, with what outcome, against which document, and until when.
 *
 * <p>This is the type that makes {@link ModelStatus#APPROVED} mean something.
 * Without it, "approved" is an adjective somebody typed; with it, approval is a
 * fact with an author, a date, and evidence — and the auditor's question ("on
 * what basis may this model price?") is a lookup rather than an archaeology
 * project.</p>
 *
 * <p>Deliberately keyed on {@link ModelRef}, not on {@code Model}: validation
 * approves a <b>version</b>. Shipping v2.4 does not inherit v2.3's approval,
 * and the type makes that impossible to fudge.</p>
 *
 * <p>{@code nextReview} is what the study's revalidation worklist ages against
 * — an approval with no expiry is how a model quietly outlives its evidence.</p>
 */
public record ValidationRecord(ModelRef model, ActorId validator, ValidationOutcome outcome,
                               LocalDate decidedOn, String documentRef,
                               List<String> conditions,
                               Optional<LocalDate> nextReview) implements ValueObject {

    public ValidationRecord {
        Objects.requireNonNull(model, "ValidationRecord.model");
        Objects.requireNonNull(validator, "ValidationRecord.validator");
        Objects.requireNonNull(outcome, "ValidationRecord.outcome");
        Objects.requireNonNull(decidedOn, "ValidationRecord.decidedOn");
        Objects.requireNonNull(documentRef, "ValidationRecord.documentRef");
        conditions = List.copyOf(Objects.requireNonNull(conditions, "ValidationRecord.conditions"));
        Objects.requireNonNull(nextReview, "ValidationRecord.nextReview (use Optional.empty())");
        if (outcome == ValidationOutcome.APPROVED_WITH_CONDITIONS && conditions.isEmpty()) {
            throw new IllegalArgumentException(
                    "APPROVED_WITH_CONDITIONS must state the conditions — unstated limits are forgotten limits");
        }
    }

    public boolean isOverdue(final LocalDate asOfDate) {
        return nextReview.map(asOfDate::isAfter).orElse(false);
    }
}
