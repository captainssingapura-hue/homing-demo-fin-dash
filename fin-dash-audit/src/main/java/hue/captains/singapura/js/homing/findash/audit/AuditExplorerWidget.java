package hue.captains.singapura.js.homing.findash.audit;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;
import hue.captains.singapura.js.homing.findash.core.css.FdFrameCss;
import hue.captains.singapura.js.homing.findash.core.css.FdStatusCss;
import hue.captains.singapura.js.homing.findash.core.css.FdTextCss;
import hue.captains.singapura.js.homing.findash.core.css.FdControlCss;
import hue.captains.singapura.js.homing.findash.core.css.FdSurfaceCss;

import java.util.List;

/**
 * The audit explorer (study §13): read-only cross-journal search — quotes,
 * trades, overrides, config promotions, operational actions — with the
 * time-travel affordance (P4) as its core interaction: set the as-of, filter
 * by journal, and every hit carries the stamp that makes the reconstruction
 * evidential. Entitlement reporting is a standing report, not archaeology.
 */
public final class AuditExplorerWidget
        extends WorkspaceWidget<WorkspaceWidget._None, AuditExplorerWidget> {

    public static final AuditExplorerWidget INSTANCE = new AuditExplorerWidget();

    private AuditExplorerWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, AuditExplorerWidget> {}

    @Override protected _Construct<_None, AuditExplorerWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Audit Explorer"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(new ModuleImports<>(
                List.of(new FinDashKitModule.fdk()), FinDashKitModule.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdFrameCss.fd_cluster(),
                        new FdFrameCss.fd_header_row(),
                        new FdFrameCss.fd_section(),
                        new FdFrameCss.fd_spacer(),
                        new FdFrameCss.fd_widget_root()),
                        FdFrameCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdStatusCss.fd_error_text()),
                        FdStatusCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdTextCss.fd_dense(),
                        new FdTextCss.fd_mono(),
                        new FdTextCss.fd_muted(),
                        new FdTextCss.fd_num(),
                        new FdTextCss.fd_caption(),
                        new FdTextCss.fd_strong(),
                        new FdTextCss.fd_title()),
                        FdTextCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdControlCss.fd_input()),
                        FdControlCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdSurfaceCss.fd_rule()),
                        FdSurfaceCss.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
            "    var root = branch.createElement('root', 'div');",
            "    css.setClass(root, fd_widget_root);",
            "",
            "    root.appendChild(fdk.el(branch, 'title-1', 'div', fd_title, ",
            "        'AUDIT EXPLORER'));",
            "    root.appendChild(fdk.el(branch, 'cap-1', 'div', [fd_caption, fd_section], ",
            "        'read-only \\u00b7 cross-journal \\u00b7 reconstruct any screen any participant saw (P4)'));",
            "",
            "    var controls = fdk.el(branch, 'head-1', 'div', [fd_header_row, fd_section]);",
            "    controls.appendChild(fdk.el(branch, 'cap-1-2', 'span', [fd_caption, fd_muted], 'as-of'));",
            "    var asOf = fdk.el(branch, 'asof', 'input', [fd_input, fd_mono]);",
            "    asOf.type = 'text'; asOf.value = 'now (live)';",
            "    controls.appendChild(asOf);",
            "    controls.appendChild(fdk.el(branch, 'cap-2', 'span', [fd_caption, fd_muted], 'journal'));",
            "    var journal = fdk.el(branch, 'journal', 'select', fd_input);",
            "    ['all', 'quotes', 'quoting', 'trades', 'marks', 'data-ops', 'config', 'risk'].forEach(function (j) {",
            "        journal.appendChild(fdk.el(branch, 'journal-opt-' + j, 'option', null, j));",
            "    });",
            "    controls.appendChild(journal);",
            "    root.appendChild(controls);",
            "",
            "    var list = fdk.el(branch, 'slot-1', 'div', null);",
            "    root.appendChild(list);",
            "    root.appendChild(fdk.el(branch, 'cap-1-3', 'div', [fd_muted, fd_caption, fd_section], ",
            "        'set an as-of like \"14:31:07\" to reconstruct the moment \\u00b7 the reconstruction is itself evidential'));",
            "",
            "    var state = { events: [] };",
            "",
            "    // The event list rebuilds on every as-of / journal change, so it owns",
            "    // a sub-branch that is dissolved first, and each row is keyed by index.",
            "    var listBranch = null;",
            "",
            "    function render() {",
            "        if (listBranch) listBranch.dissolve();",
            "        listBranch = branch.createBranch('events');",
            "        listBranch.activate(root);",
            "        var b = listBranch;",
            "        var j = journal.value;",
            "        var shown = 0;",
            "        for (var i = 0; i < state.events.length; i++) {",
            "            var e = state.events[i];",
            "            if (j !== 'all' && e.journal !== j) continue;",
            "            shown++;",
            "            var line = fdk.el(b, 'ev-' + i, 'div', [fd_cluster, fd_rule, fd_dense]);",
            "            line.appendChild(fdk.el(b, 'ev-time-' + i, 'span', [fd_muted, fd_num], e.time));",
            "            line.appendChild(fdk.chip(b, 'ev-chip-' + i, 'neutral', e.journal));",
            "            var main = fdk.el(b, 'ev-main-' + i, 'div', fd_spacer);",
            "            main.appendChild(fdk.el(b, 'ev-actor-' + i, 'span', fd_strong, e.actor + '  '));",
            "            main.appendChild(fdk.el(b, 'ev-text-' + i, 'span', null, e.text));",
            "            line.appendChild(main);",
            "            line.appendChild(fdk.stamp(b, 'ev-stamp-' + i, { slice: e.stamp }));",
            "            list.appendChild(line);",
            "        }",
            "        if (!shown) list.appendChild(fdk.el(b, 'empty', 'div', [fd_dense, fd_muted], 'no events in this journal'));",
            "    }",
            "    journal.onchange = render;",
            "",
            "    fetch('/fx/audit')",
            "        .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
            "        .then(function (d) { state.events = d.events; render(); })",
            "        .catch(function (e) {",
            "            list.appendChild(fdk.el(branch, 'err-1', 'div', fd_error_text, ",
            "                'audit load failed: ' + (e && e.message ? e.message : e)));",
            "        });",
            "",
            "    return {",
            "        root: root,",
            "        setActive: function (active) {},",
            "        partyDeregister: function () {}",
            "    };");
    }
}
