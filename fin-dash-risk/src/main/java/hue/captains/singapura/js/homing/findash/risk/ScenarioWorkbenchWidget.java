package hue.captains.singapura.js.homing.findash.risk;

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
 * The scenario workbench (study §8): spot/vol matrices, historical stress
 * replays, and custom scenarios — scenario <b>definitions are governed</b>
 * (Ring 2: reviewed, versioned, shared; a draft is loudly not-approved).
 * Picking a scenario publishes {@code ScenarioSelected} on the desk party so
 * other widgets can follow the what-if.
 */
public final class ScenarioWorkbenchWidget
        extends WorkspaceWidget<WorkspaceWidget._None, ScenarioWorkbenchWidget> {

    public static final ScenarioWorkbenchWidget INSTANCE = new ScenarioWorkbenchWidget();

    private ScenarioWorkbenchWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, ScenarioWorkbenchWidget> {}

    @Override protected _Construct<_None, ScenarioWorkbenchWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Scenario Workbench"; }
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
                        new FdTextCss.fd_title()),
                        FdTextCss.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
            "    var root = branch.createElement('root', 'div');",
            "    css.setClass(root, fd_widget_root);",
            "",
            "    var party = (workspaceCtx && workspaceCtx.deskParty) ? workspaceCtx.deskParty : null;",
            "    var actorId = null;",
            "    if (party) {",
            "        actorId = 'risk/scenarios-' + Math.random().toString(36).slice(2, 8);",
            "        party.joinActor({ id: actorId, parentSecretary: 'desk', reactors: {} });",
            "    }",
            "",
            "    root.appendChild(fdk.el(branch, 'title-1', 'div', fd_title, ",
            "        'SCENARIO WORKBENCH'));",
            "    root.appendChild(fdk.el(branch, 'cap-1', 'div', [fd_caption, fd_section], ",
            "        'scenario definitions are governed (Ring 2) \\u00b7 picking one broadcasts it on the desk party'));",
            "",
            "    var tabs = fdk.el('div', 'display:flex;gap:6px;flex-wrap:wrap;margin-bottom:8px;');",
            "    root.appendChild(tabs);",
            "    var body = fdk.el(branch, 'slot-1', 'div', null);",
            "    root.appendChild(body);",
            "",
            "    var state = { scenarios: [], selected: 0 };",
            "    function clear(n) { while (n.firstChild) n.removeChild(n.firstChild); }",
            "",
            "    function render() {",
            "        clear(tabs); clear(body);",
            "        for (var i = 0; i < state.scenarios.length; i++) {",
            "            (function (i2) {",
            "                var s = state.scenarios[i2];",
            "                var b = document.createElement('button');",
            "                b.textContent = s.label;",
            "                b.style.cssText = 'padding:3px 10px;font-size:11.5px;border-radius:999px;cursor:pointer;'",
            "                    + 'border:1px solid ' + (i2 === state.selected ? '#2a78d6' : 'var(--color-border)') + ';'",
            "                    + (i2 === state.selected ? 'background:#cde2fb;font-weight:600;' : 'background:var(--color-surface);color:var(--color-text-primary);');",
            "                b.onclick = function () {",
            "                    state.selected = i2; render();",
            "                    if (party && actorId) party.tellFrom(actorId,",
            "                        { kind: 'ScenarioSelected', scenario: { id: s.id, label: s.label } });",
            "                };",
            "                tabs.appendChild(b);",
            "            })(i);",
            "        }",
            "        var s = state.scenarios[state.selected];",
            "        if (!s) return;",
            "        var draft = s.governance.indexOf('DRAFT') >= 0;",
            "        var gov = fdk.el('div', 'display:flex;gap:8px;align-items:center;margin-bottom:6px;');",
            "        gov.appendChild(fdk.chip(branch, 'chip-1', draft ? 'warn' : 'good', s.governance));",
            "        body.appendChild(gov);",
            "        var table = document.createElement('table');",
            "        table.style.cssText = 'border-collapse:collapse;font-size:12px;font-variant-numeric:tabular-nums;';",
            "        var hr = document.createElement('tr');",
            "        for (var c = 0; c < s.cols.length; c++) {",
            "            var th = document.createElement('th');",
            "            th.style.cssText = 'padding:3px 12px;font-size:10.5px;color:var(--color-text-muted);text-align:right;';",
            "            th.textContent = s.cols[c];",
            "            hr.appendChild(th);",
            "        }",
            "        table.appendChild(hr);",
            "        for (var r = 0; r < s.rows.length; r++) {",
            "            var tr = document.createElement('tr');",
            "            for (c = 0; c < s.rows[r].length; c++) {",
            "                var td = document.createElement('td');",
            "                var val = s.rows[r][c];",
            "                var isWorst = s.worst.indexOf(val) >= 0 && val.length > 3;",
            "                td.style.cssText = 'padding:3px 12px;text-align:right;border-top:1px solid var(--color-border);'",
            "                    + (c === 0 ? 'color:var(--color-text-muted);font-weight:600;' : 'color:var(--color-text-primary);')",
            "                    + (val.indexOf('\\u26a0') >= 0 || val.indexOf('KO') >= 0 ? 'color:#9a6b1f;font-weight:600;' : '');",
            "                td.textContent = val;",
            "                tr.appendChild(td);",
            "            }",
            "            table.appendChild(tr);",
            "        }",
            "        body.appendChild(table);",
            "        body.appendChild(fdk.el('div', 'color:var(--color-text-primary);font-size:11.5px;margin-top:6px;', ",
            "            'worst: ' + s.worst + ' \\u00b7 computed by the batch path, stamped (P5)'));",
            "    }",
            "",
            "    fetch('/fx/risk')",
            "        .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
            "        .then(function (d) { state.scenarios = d.scenarios; render(); })",
            "        .catch(function (e) {",
            "            body.appendChild(fdk.el(branch, 'err-1', 'div', fd_error_text, ",
            "                'scenarios load failed: ' + (e && e.message ? e.message : e)));",
            "        });",
            "",
            "    return {",
            "        root: root,",
            "        setActive: function (active) {},",
            "        partyDeregister: function () {",
            "            if (actorId && party) { try { party.leave(actorId); } catch (e) {} }",
            "        }",
            "    };");
    }
}
