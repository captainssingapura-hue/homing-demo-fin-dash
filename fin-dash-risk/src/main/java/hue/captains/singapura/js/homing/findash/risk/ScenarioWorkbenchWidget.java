package hue.captains.singapura.js.homing.findash.risk;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;
import hue.captains.singapura.js.homing.findash.core.css.FdFrameCss;
import hue.captains.singapura.js.homing.findash.core.css.FdStatusCss;
import hue.captains.singapura.js.homing.findash.core.css.FdTextCss;
import hue.captains.singapura.js.homing.findash.core.css.FdControlCss;
import hue.captains.singapura.js.homing.findash.core.css.FdDataCss;

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
                        new FdFrameCss.fd_cluster(),
                        new FdFrameCss.fd_header_row(),
                        new FdFrameCss.fd_section(),
                        new FdFrameCss.fd_widget_root()),
                        FdFrameCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdStatusCss.fd_status_warn(),
                        new FdStatusCss.fd_error_text()),
                        FdStatusCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdTextCss.fd_dense(),
                        new FdTextCss.fd_muted(),
                        new FdTextCss.fd_num(),
                        new FdTextCss.fd_strong(),
                        new FdTextCss.fd_caption(),
                        new FdTextCss.fd_title()),
                        FdTextCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdControlCss.fd_tab(),
                        new FdControlCss.fd_tab_active()),
                        FdControlCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdDataCss.fd_table(),
                        new FdDataCss.fd_td(),
                        new FdDataCss.fd_td_num(),
                        new FdDataCss.fd_th()),
                        FdDataCss.INSTANCE));
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
            "        actorId = fdk.actorId('risk/scenarios', branch);",
            "        party.joinActor({ id: actorId, parentSecretary: 'desk', reactors: {} });",
            "    }",
            "",
            "    root.appendChild(fdk.el(branch, 'title-1', 'div', fd_title, ",
            "        'SCENARIO WORKBENCH'));",
            "    root.appendChild(fdk.el(branch, 'cap-1', 'div', [fd_caption, fd_section], ",
            "        'scenario definitions are governed (Ring 2) \\u00b7 picking one broadcasts it on the desk party'));",
            "",
            "    var tabs = fdk.el(branch, 'tabs', 'div', [fd_header_row, fd_section]);",
            "    root.appendChild(tabs);",
            "    var body = fdk.el(branch, 'slot-1', 'div', null);",
            "    root.appendChild(body);",
            "",
            "    var state = { scenarios: [], selected: 0 };",
            "",
            "    // The whole view rebuilds on every scenario pick, so it lives in a",
            "    // sub-branch that is dissolved first — element names are unique for a",
            "    // branch's life, and dissolve() detaches the previous DOM.",
            "    var viewBranch = null;",
            "",
            "    function render() {",
            "        if (viewBranch) viewBranch.dissolve();",
            "        viewBranch = branch.createBranch('view');",
            "        viewBranch.activate(root);",
            "        var vb = viewBranch;",
            "        for (var i = 0; i < state.scenarios.length; i++) {",
            "            (function (i2) {",
            "                var sc = state.scenarios[i2];",
            "                var btn = fdk.el(vb, 'tab-' + i2, 'button', fd_tab, sc.label);",
            "                if (i2 === state.selected) css.addClass(btn, fd_tab_active);",
            "                btn.onclick = function () {",
            "                    state.selected = i2; render();",
            "                    if (party && actorId) party.tellFrom(actorId,",
            "                        { kind: 'ScenarioSelected', scenario: { id: sc.id, label: sc.label } });",
            "                };",
            "                tabs.appendChild(btn);",
            "            })(i);",
            "        }",
            "        var s = state.scenarios[state.selected];",
            "        if (!s) return;",
            "        var draft = s.governance.indexOf('DRAFT') >= 0;",
            "        var gov = fdk.el(vb, 'gov', 'div', [fd_cluster, fd_section]);",
            "        gov.appendChild(fdk.chip(vb, 'gov-chip', draft ? 'warn' : 'good', s.governance));",
            "        body.appendChild(gov);",
            "        var table = fdk.el(vb, 'table', 'table', [fd_table, fd_dense, fd_num]);",
            "        var hr = fdk.el(vb, 'thead-row', 'tr', null);",
            "        for (var c = 0; c < s.cols.length; c++) {",
            "            hr.appendChild(fdk.el(vb, 'th-' + c, 'th', [fd_th, fd_td_num], s.cols[c]));",
            "        }",
            "        table.appendChild(hr);",
            "        for (var r = 0; r < s.rows.length; r++) {",
            "            var tr = fdk.el(vb, 'tr-' + r, 'tr', null);",
            "            for (c = 0; c < s.rows[r].length; c++) {",
            "                var val = s.rows[r][c];",
            "                var td = fdk.el(vb, 'td-' + r + '-' + c, 'td', [fd_td, fd_td_num], val);",
            "                // First column is the row label; a knock-out or warning cell is",
            "                // a state, so it carries status ink rather than a bare colour.",
            "                if (c === 0) css.addClass(td, fd_muted, fd_strong);",
            "                if (val.indexOf('\\u26a0') >= 0 || val.indexOf('KO') >= 0) {",
            "                    css.addClass(td, fd_status_warn, fd_strong);",
            "                }",
            "                tr.appendChild(td);",
            "            }",
            "            table.appendChild(tr);",
            "        }",
            "        body.appendChild(table);",
            "        body.appendChild(fdk.el(vb, 'worst', 'div', [fd_caption, fd_section], ",
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
            "            if (actorId && party) { party.leave(actorId); }",
            "        }",
            "    };");
    }
}
