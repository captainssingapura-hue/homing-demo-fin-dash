package hue.captains.singapura.js.homing.findash.book;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.css.FdControlCss;
import hue.captains.singapura.js.homing.findash.core.css.FdFrameCss;
import hue.captains.singapura.js.homing.findash.core.css.FdStatusCss;
import hue.captains.singapura.js.homing.findash.core.css.FdSurfaceCss;
import hue.captains.singapura.js.homing.findash.core.css.FdTextCss;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;

import java.util.List;

/**
 * One line of the trade journal, and the amendment history that unfolds
 * beneath it.
 *
 * <p>Split out of {@link TradeBlotterWidget} when the blotter grew keyboard
 * navigation and crossed the effective-line limit. The division is the one the
 * code already had: the widget owns <em>which</em> trades are shown — filtering,
 * grouping, selection, the party bus — and this module owns what one of them
 * <em>looks like</em>. Nothing here reads widget state; everything it needs
 * arrives in the options object, which is what makes it testable in isolation
 * and reusable by any other journal view.</p>
 *
 * <p>Body in the co-located {@code TradeRowModule.js}.</p>
 */
public record TradeRowModule() implements DomModule<TradeRowModule> {

    public static final TradeRowModule INSTANCE = new TradeRowModule();

    /**
     * Exported function: {@code tradeRow(o)} → the row element, where
     * {@code o} is {@code { branch, key, trade, open, selected, onSelect }}.
     * {@code key} makes every element name unique within the render pass;
     * {@code onSelect(trade, amended)} is called on click.
     */
    public record tradeRow() implements Exportable._Constant<TradeRowModule> {}

    @Override
    public ImportsFor<TradeRowModule> imports() {
        return ImportsFor.<TradeRowModule>builder()
                .add(new ModuleImports<>(List.of(new FinDashKitModule.fdk()),
                        FinDashKitModule.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new FdFrameCss.fd_header_row(),
                        new FdFrameCss.fd_row(),
                        new FdFrameCss.fd_spacer()),
                        FdFrameCss.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new FdTextCss.fd_caption(),
                        new FdTextCss.fd_mono(),
                        new FdTextCss.fd_muted(),
                        new FdTextCss.fd_num(),
                        new FdTextCss.fd_strong()),
                        FdTextCss.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new FdStatusCss.fd_status_warn(),
                        new FdStatusCss.fd_status_warn_bg()),
                        FdStatusCss.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new FdControlCss.fd_clickable(),
                        new FdControlCss.fd_selected()),
                        FdControlCss.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new FdSurfaceCss.fd_panel(),
                        new FdSurfaceCss.fd_rule()),
                        FdSurfaceCss.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<TradeRowModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new tradeRow()));
    }
}
