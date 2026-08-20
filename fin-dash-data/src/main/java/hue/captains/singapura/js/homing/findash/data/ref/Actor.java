package hue.captains.singapura.js.homing.findash.data.ref;

import hue.captains.singapura.js.homing.findash.data.id.ActorId;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/**
 * Stratum 2 — whoever can appear in a journal entry's {@code actor} field.
 * Automations are actors too ("rule QW-3 widened the spread" is an action with
 * an author), which is what lets the study's requirement hold: automatic and
 * manual changes land in the same stream and are read the same way.
 */
public record Actor(ActorId id, String displayName, Persona persona) implements ValueObject {

    public Actor {
        Objects.requireNonNull(id, "Actor.id");
        Objects.requireNonNull(displayName, "Actor.displayName");
        Objects.requireNonNull(persona, "Actor.persona");
    }
}
