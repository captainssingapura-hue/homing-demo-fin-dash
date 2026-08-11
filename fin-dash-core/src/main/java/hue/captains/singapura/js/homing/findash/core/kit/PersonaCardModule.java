package hue.captains.singapura.js.homing.findash.core.kit;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * The persona home-card renderer — the shared DOM builder every persona
 * workspace's starter widget imports while its real screens are being built.
 * Renders the persona's mission, ring writes, cadence, and planned screens
 * (straight from the UI study's participant map, §2), plus the two
 * cross-cutting patterns every fin-dash widget must carry from day one:
 *
 * <ul>
 *   <li><b>P1 — every number explains itself:</b> a lineage stamp
 *       (slice / epoch / model+version), here a static example;</li>
 *   <li><b>P2 — degraded state is loud:</b> a status chip in the shared
 *       visual language, here the "scaffold" state.</li>
 * </ul>
 *
 * <p>Pure DOM builder: takes a plain info object, returns a detached element
 * the caller appends into branch-owned DOM. No branch access, no lookups, no
 * HTML literals — {@code document.createElement} + {@code textContent} only,
 * so it holds under the PRIMITIVE rule set. Body lives in the co-located
 * {@code PersonaCardModule.js} resource.</p>
 */
public record PersonaCardModule() implements DomModule<PersonaCardModule> {

    public static final PersonaCardModule INSTANCE = new PersonaCardModule();

    /** Exported function: {@code personaCard(info)} → detached card element. */
    public record personaCard() implements Exportable._Constant<PersonaCardModule> {}

    @Override
    public ImportsFor<PersonaCardModule> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<PersonaCardModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new personaCard()));
    }
}
