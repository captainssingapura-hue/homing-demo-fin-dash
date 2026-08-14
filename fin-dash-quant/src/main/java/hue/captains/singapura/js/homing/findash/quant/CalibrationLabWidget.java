package hue.captains.singapura.js.homing.findash.quant;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;
import hue.captains.singapura.js.homing.findash.core.css.FdFrameCss;
import hue.captains.singapura.js.homing.findash.core.css.FdStatusCss;
import hue.captains.singapura.js.homing.findash.core.css.FdTextCss;

import java.util.List;

/**
 * The calibration lab (study §7): per-pair fit diagnostics — residuals by
 * pillar, arb-gate <b>margins</b> (how close, not just pass/fail), solver
 * iteration trends (the early-warning signal for a deteriorating regime),
 * fast-path vs slow-path agreement, and model basis. Read-only, live —
 * evolution (Ring-2 proposals) happens in the replay lab / change console.
 */
public final class CalibrationLabWidget
        extends WorkspaceWidget<WorkspaceWidget._None, CalibrationLabWidget> {

    public static final CalibrationLabWidget INSTANCE = new CalibrationLabWidget();

    private CalibrationLabWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, CalibrationLabWidget> {}

    @Override protected _Construct<_None, CalibrationLabWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Calibration Lab"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(new ModuleImports<>(
                List.of(new FinDashKitModule.fdk()), FinDashKitModule.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdFrameCss.fd_section(),
                        new FdFrameCss.fd_widget_root()),
                        FdFrameCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdStatusCss.fd_error_text()),
                        FdStatusCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdTextCss.fd_caption(),
                        new FdTextCss.fd_muted(),
                        new FdTextCss.fd_strongest(),
                        new FdTextCss.fd_title()),
                        FdTextCss.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
            "    var root = branch.createElement('root', 'div');",
            "    css.setClass(root, fd_widget_root);",
            "",
            "    root.appendChild(fdk.el(branch, 'title-1', 'div', fd_title, ",
            "        'CALIBRATION LAB'));",
            "    root.appendChild(fdk.el(branch, 'cap-1', 'div', [fd_caption, fd_section], ",
            "        'diagnostics are read-only + live \\u00b7 every value is a time series \\u00b7 '",
            "        + 'evolution goes through the replay lab \\u2192 change console (Ring 2)'));",
            "",
            "    var list = fdk.el(branch, 'slot-1', 'div', null);",
            "    root.appendChild(list);",
            "",
            "    function diagCard(c) {",
            "        var card = fdk.el('div', 'border:1px solid var(--color-border);border-radius:8px;padding:10px 12px;'",
            "            + 'margin-bottom:8px;');",
            "        var head = fdk.el('div', 'display:flex;gap:10px;align-items:baseline;flex-wrap:wrap;');",
            "        head.appendChild(fdk.el(branch, 'strong-1', 'span', fd_strongest, c.pair));",
            "        head.appendChild(fdk.stamp(branch, 'stamp-1', { surface: c.epoch }));",
            "        head.appendChild(fdk.chip(branch, 'chip-1', c.severity, c.severity === 'good' ? 'healthy regime'",
            "            : (c.severity === 'warn' ? 'watch' : 'deteriorating')));",
            "        card.appendChild(head);",
            "        card.appendChild(fdk.kv(branch, 'kv-1', 'residuals', c.residByPillar));",
            "        card.appendChild(fdk.kv(branch, 'kv-2', 'arb-gate margins', c.arbMargins));",
            "        card.appendChild(fdk.kv(branch, 'kv-3', 'solver iterations', c.solverTrend));",
            "        card.appendChild(fdk.kv(branch, 'kv-4', 'fast vs slow path', c.pathAgreement));",
            "        card.appendChild(fdk.kv(branch, 'kv-5', 'model basis', c.modelBasis));",
            "        return card;",
            "    }",
            "",
            "    fetch('/fx/calibration')",
            "        .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
            "        .then(function (d) {",
            "            for (var i = 0; i < d.diags.length; i++) list.appendChild(diagCard(d.diags[i]));",
            "            list.appendChild(fdk.el(branch, 'cap-2', 'div', [fd_caption, fd_muted], ",
            "                'export to research preserves lineage \\u00b7 cross-pair comparison + full history in the lab'));",
            "        })",
            "        .catch(function (e) {",
            "            list.appendChild(fdk.el(branch, 'err-1', 'div', fd_error_text, ",
            "                'calibration load failed: ' + (e && e.message ? e.message : e)));",
            "        });",
            "",
            "    return {",
            "        root: root,",
            "        setActive: function (active) {},",
            "        partyDeregister: function () {}",
            "    };");
    }
}
