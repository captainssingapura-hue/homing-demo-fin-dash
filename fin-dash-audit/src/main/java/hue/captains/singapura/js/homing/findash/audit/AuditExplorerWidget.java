package hue.captains.singapura.js.homing.findash.audit;

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
                        new FdFrameCss.fd_section(),
                        new FdFrameCss.fd_spacer(),
                        new FdFrameCss.fd_widget_root()),
                        FdFrameCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdStatusCss.fd_error_text()),
                        FdStatusCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdTextCss.fd_caption(),
                        new FdTextCss.fd_strong(),
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
            "        'AUDIT EXPLORER'));",
            "    root.appendChild(fdk.el(branch, 'cap-1', 'div', [fd_caption, fd_section], ",
            "        'read-only \\u00b7 cross-journal \\u00b7 reconstruct any screen any participant saw (P4)'));",
            "",
            "    var controls = fdk.el('div', 'display:flex;gap:8px;align-items:center;flex-wrap:wrap;margin-bottom:8px;');",
            "    controls.appendChild(fdk.el('span', 'color:var(--color-text-muted);font-size:11.5px;', 'as-of'));",
            "    var asOf = document.createElement('input');",
            "    asOf.type = 'text'; asOf.value = 'now (live)';",
            "    asOf.style.cssText = 'width:130px;padding:3px 8px;font-family:' + fdk.tokens.mono",
            "        + ';font-size:12px;border:1px solid var(--color-border);border-radius:4px;';",
            "    controls.appendChild(asOf);",
            "    controls.appendChild(fdk.el('span', 'color:var(--color-text-muted);font-size:11.5px;', 'journal'));",
            "    var journal = document.createElement('select');",
            "    ['all', 'quotes', 'quoting', 'trades', 'marks', 'data-ops', 'config', 'risk'].forEach(function (j) {",
            "        var o = document.createElement('option'); o.textContent = j; journal.appendChild(o);",
            "    });",
            "    journal.style.cssText = 'padding:3px 8px;font-size:12px;border:1px solid var(--color-border);border-radius:4px;';",
            "    controls.appendChild(journal);",
            "    root.appendChild(controls);",
            "",
            "    var list = fdk.el(branch, 'slot-1', 'div', null);",
            "    root.appendChild(list);",
            "    root.appendChild(fdk.el('div', 'color:var(--color-text-muted);font-size:11px;margin-top:8px;', ",
            "        'set an as-of like \"14:31:07\" to reconstruct the moment \\u00b7 the reconstruction is itself evidential'));",
            "",
            "    var state = { events: [] };",
            "    function clear(n) { while (n.firstChild) n.removeChild(n.firstChild); }",
            "",
            "    function render() {",
            "        clear(list);",
            "        var j = journal.value;",
            "        var shown = 0;",
            "        for (var i = 0; i < state.events.length; i++) {",
            "            var e = state.events[i];",
            "            if (j !== 'all' && e.journal !== j) continue;",
            "            shown++;",
            "            var line = fdk.el('div', 'display:flex;gap:10px;align-items:baseline;padding:5px 4px;'",
            "                + 'border-bottom:1px solid var(--color-border);font-size:12px;');",
            "            line.appendChild(fdk.el('span', 'color:var(--color-text-muted);font-variant-numeric:tabular-nums;'",
            "                + 'min-width:64px;', e.time));",
            "            line.appendChild(fdk.chip('neutral', e.journal));",
            "            var main = fdk.el(branch, 'spacer-1', 'div', fd_spacer);",
            "            main.appendChild(fdk.el(branch, 'strong-1', 'span', fd_strong, e.actor + '  '));",
            "            main.appendChild(fdk.el(branch, 'txt-1', 'span', null, e.text));",
            "            line.appendChild(main);",
            "            line.appendChild(fdk.stamp(branch, 'stamp-1', { slice: e.stamp }));",
            "            list.appendChild(line);",
            "        }",
            "        if (!shown) list.appendChild(fdk.el('div', 'color:var(--color-text-muted);font-size:12px;', 'no events in this journal'));",
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
