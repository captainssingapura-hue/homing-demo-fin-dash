package hue.captains.singapura.js.homing.findash.quant;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

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
                List.of(new FinDashKitModule.fdk()), FinDashKitModule.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
            "    var root = branch.createElement('root', 'div');",
            "    root.style.cssText = 'height:100%;overflow:auto;box-sizing:border-box;padding:14px;'",
            "        + 'font-family:system-ui,sans-serif;font-size:13px;color:var(--color-text-primary);';",
            "",
            "    root.appendChild(fdk.el('div', 'font-weight:700;font-size:14px;letter-spacing:0.3px;',",
            "        'CALIBRATION LAB'));",
            "    root.appendChild(fdk.el('div', 'color:var(--color-text-primary);font-size:11.5px;margin:2px 0 8px;',",
            "        'diagnostics are read-only + live \\u00b7 every value is a time series \\u00b7 '",
            "        + 'evolution goes through the replay lab \\u2192 change console (Ring 2)'));",
            "",
            "    var list = fdk.el('div', '');",
            "    root.appendChild(list);",
            "",
            "    function diagCard(c) {",
            "        var card = fdk.el('div', 'border:1px solid var(--color-border);border-radius:8px;padding:10px 12px;'",
            "            + 'margin-bottom:8px;');",
            "        var head = fdk.el('div', 'display:flex;gap:10px;align-items:baseline;flex-wrap:wrap;');",
            "        head.appendChild(fdk.el('span', 'font-weight:700;', c.pair));",
            "        head.appendChild(fdk.stamp({ surface: c.epoch }));",
            "        head.appendChild(fdk.chip(c.severity, c.severity === 'good' ? 'healthy regime'",
            "            : (c.severity === 'warn' ? 'watch' : 'deteriorating')));",
            "        card.appendChild(head);",
            "        card.appendChild(fdk.kv('residuals', c.residByPillar));",
            "        card.appendChild(fdk.kv('arb-gate margins', c.arbMargins));",
            "        card.appendChild(fdk.kv('solver iterations', c.solverTrend));",
            "        card.appendChild(fdk.kv('fast vs slow path', c.pathAgreement));",
            "        card.appendChild(fdk.kv('model basis', c.modelBasis));",
            "        return card;",
            "    }",
            "",
            "    fetch('/fx/calibration')",
            "        .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
            "        .then(function (d) {",
            "            for (var i = 0; i < d.diags.length; i++) list.appendChild(diagCard(d.diags[i]));",
            "            list.appendChild(fdk.el('div', 'color:var(--color-text-muted);font-size:11px;',",
            "                'export to research preserves lineage \\u00b7 cross-pair comparison + full history in the lab'));",
            "        })",
            "        .catch(function (e) {",
            "            list.appendChild(fdk.el('div', 'color:#a8502a;',",
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
