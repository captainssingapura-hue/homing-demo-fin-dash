package hue.captains.singapura.js.homing.findash.trader;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.css.FdDataCss;
import hue.captains.singapura.js.homing.findash.core.css.FdTextCss;
import hue.captains.singapura.js.homing.relgrid.protocol.RelGridProtocolModule;

import java.util.List;

/**
 * What the ladder puts in the slots a Relation Grid group offers between its
 * tables (RFC 0050 · Episode 2): a pair caption with its fold toggle above
 * each detail table, and the as-of stamp in the trailing slot.
 *
 * <p>The point of a fence is that <b>its look is the domain's</b>. The group
 * mints the slot and places whatever {@code fenceElement()} returns; it draws
 * nothing in it and reads nothing back. So the Minesweeper bevel the Episode 1
 * section row settled on — the caption an unopened cell, the tables under it
 * opened ones — is carried over here as the same typed class, unchanged. Body
 * in the co-located {@code LadderFenceModule.js}.</p>
 */
public record LadderFenceModule() implements DomModule<LadderFenceModule> {

    public static final LadderFenceModule INSTANCE = new LadderFenceModule();

    /** Exported: {@code createPairFence(pair, {branch, tell, folded?})}. */
    public record createPairFence()  implements Exportable._Constant<LadderFenceModule> {}
    /** Exported: {@code createStampFence(feed, {branch})}. */
    public record createStampFence() implements Exportable._Constant<LadderFenceModule> {}

    @Override
    public ImportsFor<LadderFenceModule> imports() {
        return ImportsFor.<LadderFenceModule>builder()
                .add(new ModuleImports<>(List.of(new RelGridProtocolModule.RelGridGroupFold()),
                        RelGridProtocolModule.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new FdTextCss.fd_dense(),
                        new FdTextCss.fd_caption(),
                        new FdTextCss.fd_muted()),
                        FdTextCss.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new FdDataCss.fd_section_row(),
                        new FdDataCss.fd_fence(),
                        new FdDataCss.fd_fence_toggle(),
                        new FdDataCss.fd_fence_name()),
                        FdDataCss.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<LadderFenceModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new createPairFence(), new createStampFence()));
    }
}
