package hue.captains.singapura.js.homing.findash.core.bus;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * The Secretary for the fin-dash <b>TradeParty</b> — the second selection bus,
 * carrying the <b>entity</b> level of the drill-down where {@link
 * DeskSecretaryModule} carries the <b>scope</b> level.
 *
 * <h2>Why two buses and not one</h2>
 *
 * <p>The desk bus answers "what am I looking at" — a portfolio, an instrument,
 * a scenario. This one answers "which record" — one specific trade. Before the
 * split, the blotter published a trade selection as an
 * {@code InstrumentSelected} carrying an extra {@code ref} field, and every
 * consumer had to filter it back out ({@code if (ins.ref) return;} — including
 * the blotter, to avoid reacting to itself). That is one channel carrying two
 * scopes, and the filtering was the symptom.</p>
 *
 * <p>Split apart, each bus has one meaning: selecting a trade no longer looks
 * like selecting an instrument, so nothing has to distinguish them after the
 * fact. The blotter still publishes {@code InstrumentSelected} when a trade is
 * picked — a trade genuinely does identify an instrument, and the pricer and
 * surface manager should follow — but that is now a deliberate second
 * statement rather than a payload smuggled inside the first.</p>
 *
 * <p>Per the Diligent Secretaries doctrine: a pure {@code (state, envelope) →
 * Step} behavior + initial state, no DOM (held to the SECRETARY rule set).
 * Body in the co-located {@code TradeSecretaryModule.js}.</p>
 */
public record TradeSecretaryModule() implements DomModule<TradeSecretaryModule> {

    public static final TradeSecretaryModule INSTANCE = new TradeSecretaryModule();

    /** The exported {@code TradeSecretary} object: {@code initial} + {@code behavior}. */
    public record TradeSecretary() implements Exportable._Constant<TradeSecretaryModule> {}

    @Override
    public ImportsFor<TradeSecretaryModule> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<TradeSecretaryModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new TradeSecretary()));
    }
}
