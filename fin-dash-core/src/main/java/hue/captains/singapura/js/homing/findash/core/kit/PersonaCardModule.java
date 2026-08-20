package hue.captains.singapura.js.homing.findash.core.kit;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.css.FdDataCss;
import hue.captains.singapura.js.homing.findash.core.css.FdFrameCss;
import hue.captains.singapura.js.homing.findash.core.css.FdStatusCss;
import hue.captains.singapura.js.homing.findash.core.css.FdTextCss;

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
 * <p>Consumer view code: it builds content, so elements come from the caller's
 * {@code branch} and styling is typed CSS classes. Body lives in the co-located
 * {@code PersonaCardModule.js} resource.</p>
 */
public record PersonaCardModule() implements DomModule<PersonaCardModule> {

    public static final PersonaCardModule INSTANCE = new PersonaCardModule();

    /** Exported function: {@code personaCard(branch, info)} → card element. */
    public record personaCard() implements Exportable._Constant<PersonaCardModule> {}

    @Override
    public ImportsFor<PersonaCardModule> imports() {
        return ImportsFor.<PersonaCardModule>builder()
                .add(new ModuleImports<>(List.of(
                        new FdFrameCss.fd_stack(),
                        new FdFrameCss.fd_section()),
                        FdFrameCss.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new FdTextCss.fd_display(),
                        new FdTextCss.fd_body(),
                        new FdTextCss.fd_caption(),
                        new FdTextCss.fd_muted(),
                        new FdTextCss.fd_strong()),
                        FdTextCss.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new FdStatusCss.fd_chip(),
                        new FdStatusCss.fd_chip_warn()),
                        FdStatusCss.INSTANCE))
                .add(new ModuleImports<>(List.of(new FdDataCss.fd_stamp()), FdDataCss.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<PersonaCardModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new personaCard()));
    }
}
