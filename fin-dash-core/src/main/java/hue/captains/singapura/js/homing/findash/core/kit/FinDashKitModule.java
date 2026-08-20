package hue.captains.singapura.js.homing.findash.core.kit;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.css.FdDataCss;
import hue.captains.singapura.js.homing.findash.core.css.FdStatusCss;
import hue.captains.singapura.js.homing.findash.core.css.FdTextCss;

import java.util.List;

/**
 * The fin-dash UI kit — format helpers and the two cross-cutting patterns every
 * widget carries (UI study P1 + P2): the status <b>chip</b> (state visible,
 * icon + label, never colour alone) and the lineage <b>stamp</b> (every number
 * explains itself), plus the severity {@code meter}.
 *
 * <p><b>Consumer, not primitive.</b> This module builds content, so it carries
 * the full discipline: elements come from the caller's {@code branch} and
 * styling is typed CSS classes. It was previously declared {@code PRIMITIVE} —
 * a type meant for structural machinery like SplitPane — which opted it out of
 * the CSS rules and hid its styling from the gate while all 27 importing
 * widgets inherited it.</p>
 *
 * <p>Every builder therefore takes {@code (branch, name, …)}: the branch owns
 * the element, the name makes it addressable and releasable. Colour lives
 * entirely in {@link FdStatusCss} — the kit no longer knows any hex.</p>
 */
public record FinDashKitModule() implements DomModule<FinDashKitModule> {

    public static final FinDashKitModule INSTANCE = new FinDashKitModule();

    /** The exported {@code fdk} object: el, fmt, chip, stamp, meter, kv, sectionTitle. */
    public record fdk() implements Exportable._Constant<FinDashKitModule> {}

    @Override
    public ImportsFor<FinDashKitModule> imports() {
        return ImportsFor.<FinDashKitModule>builder()
                .add(new ModuleImports<>(List.of(
                        new FdStatusCss.fd_chip(),
                        new FdStatusCss.fd_chip_good(),
                        new FdStatusCss.fd_chip_warn(),
                        new FdStatusCss.fd_chip_serious(),
                        new FdStatusCss.fd_chip_critical(),
                        new FdStatusCss.fd_chip_neutral(),
                        new FdStatusCss.fd_status_good_bg(),
                        new FdStatusCss.fd_status_warn_bg(),
                        new FdStatusCss.fd_status_critical_bg()),
                        FdStatusCss.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new FdDataCss.fd_meter(),
                        new FdDataCss.fd_meter_fill(),
                        new FdDataCss.fd_stamp(),
                        new FdDataCss.fd_kv_row(),
                        new FdDataCss.fd_kv_label(),
                        new FdDataCss.fd_kv_value()),
                        FdDataCss.INSTANCE))
                .add(new ModuleImports<>(List.of(new FdTextCss.fd_section_title()),
                        FdTextCss.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<FinDashKitModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new fdk()));
    }
}
