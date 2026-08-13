package hue.captains.singapura.js.homing.findash.summary;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * The management summary (study §13): a glanceable rollup — P&amp;L, risk vs
 * limits, quoting posture, open overrides past threshold age, pending Ring-2
 * approvals, system health. Every tile is a <b>projection of the owning
 * persona's data</b> (P5 — no numbers computed specially for management) and
 * names the workspace that owns it; the landing page is the door.
 */
public final class SummaryDashboardWidget
        extends WorkspaceWidget<WorkspaceWidget._None, SummaryDashboardWidget> {

    public static final SummaryDashboardWidget INSTANCE = new SummaryDashboardWidget();

    private SummaryDashboardWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, SummaryDashboardWidget> {}

    @Override protected _Construct<_None, SummaryDashboardWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Summary"; }
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
            "    var head = fdk.el('div', 'display:flex;align-items:baseline;gap:10px;flex-wrap:wrap;');",
            "    head.appendChild(fdk.el('span', 'font-weight:700;font-size:14px;letter-spacing:0.3px;',",
            "        'MANAGEMENT SUMMARY'));",
            "    var stampSlot = fdk.el('span', '');",
            "    head.appendChild(stampSlot);",
            "    root.appendChild(head);",
            "    root.appendChild(fdk.el('div', 'color:var(--color-text-primary);font-size:11.5px;margin:2px 0 8px;',",
            "        'every tile a projection of the owning persona\\u2019s data (P5) \\u00b7 the landing page is the door'));",
            "",
            "    var gridBox = fdk.el('div', 'display:grid;grid-template-columns:repeat(auto-fill,minmax(240px,1fr));'",
            "        + 'gap:10px;');",
            "    root.appendChild(gridBox);",
            "",
            "    function tileCard(t) {",
            "        var card = fdk.el('div', 'border:1px solid var(--color-border);border-radius:10px;padding:12px 14px;');",
            "        card.appendChild(fdk.el('div', 'color:var(--color-text-muted);font-size:11px;font-weight:600;'",
            "            + 'letter-spacing:0.3px;', t.title));",
            "        var v = fdk.el('div', 'display:flex;gap:8px;align-items:center;margin:4px 0;');",
            "        v.appendChild(fdk.el('span', 'font-size:20px;font-weight:700;', t.value));",
            "        v.appendChild(fdk.chip(t.severity, t.severity === 'good' ? 'ok' : (t.severity === 'serious' ? 'attend' : 'watch')));",
            "        card.appendChild(v);",
            "        card.appendChild(fdk.el('div', 'color:var(--color-text-primary);font-size:11.5px;', t.detail));",
            "        card.appendChild(fdk.el('div', 'color:var(--color-text-muted);font-size:10.5px;margin-top:6px;',",
            "            'owned by: ' + t.owner));",
            "        return card;",
            "    }",
            "",
            "    fetch('/fx/summary')",
            "        .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
            "        .then(function (d) {",
            "            stampSlot.appendChild(fdk.stamp({ slice: d.slice }));",
            "            for (var i = 0; i < d.tiles.length; i++) gridBox.appendChild(tileCard(d.tiles[i]));",
            "        })",
            "        .catch(function (e) {",
            "            gridBox.appendChild(fdk.el('div', 'color:#a8502a;',",
            "                'summary load failed: ' + (e && e.message ? e.message : e)));",
            "        });",
            "",
            "    return {",
            "        root: root,",
            "        setActive: function (active) {},",
            "        partyDeregister: function () {}",
            "    };");
    }
}
