package hue.captains.singapura.js.homing.findash.core.bus;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * The Secretary for the fin-dash <b>DeskParty</b> — the cross-widget selection
 * bus (UI study: "portfolio / scenario selected" → every widget refreshes for
 * that selection). Blotter row click → {@code InstrumentSelected} → the
 * secretary rebroadcasts {@code InstrumentChanged} to every member; scenarios
 * flow the same way; late joiners sync via {@code CurrentInstrumentRequested}.
 *
 * <p>Per the Diligent Secretaries doctrine: a pure {@code (state, envelope) →
 * Step} behavior + initial state, exported as one {@code DeskSecretary} object
 * — importable in isolation for a GraalVM JUnit harness, no DOM at all (held
 * to the SECRETARY rule set). Body in the co-located
 * {@code DeskSecretaryModule.js}.</p>
 */
public record DeskSecretaryModule() implements DomModule<DeskSecretaryModule> {

    public static final DeskSecretaryModule INSTANCE = new DeskSecretaryModule();

    /** The exported {@code DeskSecretary} object: {@code initial} + {@code behavior}. */
    public record DeskSecretary() implements Exportable._Constant<DeskSecretaryModule> {}

    @Override
    public ImportsFor<DeskSecretaryModule> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<DeskSecretaryModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new DeskSecretary()));
    }
}
