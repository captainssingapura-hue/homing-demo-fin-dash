package hue.captains.singapura.js.homing.findash.data.journal;

import hue.captains.singapura.tao.ontology.Immutable;

/**
 * Stratum 5 — what a journal entry <i>says</i>.
 *
 * <p><b>Sealed on purpose.</b> Every fold over history — the audit tape, the
 * current-state derivation, the amendment history under a trade — switches
 * over these kinds, and a sealed hierarchy makes the compiler reject a fold
 * that forgets one. (The same sealed-versus-open reasoning the framework
 * applies to {@code StandardJsModuleType} against the open {@code JsModuleType}:
 * closed where exhaustiveness is the point, open where extension is.)</p>
 *
 * <p>Each era adds its own kinds to this permits clause, and that list is a
 * precise record of what the desk became able to do:</p>
 * <ul>
 *   <li><b>Era 1</b> — {@link TradeBooked}. A desk of traders can do exactly
 *       one thing that history must remember: put on a trade.</li>
 * </ul>
 *
 * <p>The interface is marked {@link Immutable} — the weakest claim that is
 * still true of every payload, and the one holders rely on: {@code
 * JournalEntry} may declare a {@code JournalPayload} field only because the
 * abstraction itself promises immutability. It is deliberately <i>not</i>
 * {@code ValueObject}: that marker asserts value-equality over declared
 * fields, which an interface has none of. The <i>records</i> that implement
 * this are the objects with being, and each is a {@code ValueObject}.</p>
 */
public sealed interface JournalPayload extends Immutable permits TradeBooked {

    /** Discriminator for exports and (later) the audit tape's journal filter. */
    String kind();
}
