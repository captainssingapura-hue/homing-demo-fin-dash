package hue.captains.singapura.js.homing.findash.middleoffice;

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
 * The middle-office lifecycle workstation (study §10): expiries with cut
 * countdowns and exercise decisions (auto vs manual), barrier/touch watches
 * with the determination record, fixings, deliveries — plus the breaks
 * dashboard with aging and ownership. Bulk expiry processing is
 * <b>preview-then-commit</b>; the preview is shown before anything books
 * (demo: commit mutates screen state).
 */
public final class LifecycleWidget
        extends WorkspaceWidget<WorkspaceWidget._None, LifecycleWidget> {

    public static final LifecycleWidget INSTANCE = new LifecycleWidget();

    private LifecycleWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, LifecycleWidget> {}

    @Override protected _Construct<_None, LifecycleWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Lifecycle"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(new ModuleImports<>(
                List.of(new FinDashKitModule.fdk()), FinDashKitModule.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdFrameCss.fd_row(),
                        new FdFrameCss.fd_section(),
                        new FdFrameCss.fd_spacer(),
                        new FdFrameCss.fd_widget_root()),
                        FdFrameCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdStatusCss.fd_error_text()),
                        FdStatusCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdTextCss.fd_caption(),
                        new FdTextCss.fd_muted(),
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
            "        'LIFECYCLE WORKSTATION'));",
            "    root.appendChild(fdk.el('div', 'color:var(--color-text-primary);font-size:11.5px;margin:2px 0 4px;', ",
            "        'cut times desk-local, UTC on hover \\u00b7 every event exactly once, journaled'));",
            "",
            "    root.appendChild(fdk.sectionTitle(branch, 'sect-1', 'Events'));",
            "    var items = fdk.el(branch, 'slot-1', 'div', null);",
            "    root.appendChild(items);",
            "",
            "    var bulk = fdk.el('div', 'display:flex;gap:8px;align-items:center;margin-top:8px;');",
            "    var preview = document.createElement('button');",
            "    preview.textContent = 'Preview NY-cut bulk processing';",
            "    preview.style.cssText = 'padding:5px 12px;font-size:12px;font-weight:600;color:var(--color-surface);'",
            "        + 'background:#2a78d6;border:none;border-radius:6px;cursor:pointer;';",
            "    bulk.appendChild(preview);",
            "    var bulkNote = fdk.el('span', 'color:var(--color-text-muted);font-size:11.5px;', 'preview-then-commit');",
            "    bulk.appendChild(bulkNote);",
            "    root.appendChild(bulk);",
            "    preview.onclick = function () {",
            "        bulkNote.textContent = 'preview: 214 expiries \\u00b7 197 auto-exercise \\u00b7 2 manual decisions'",
            "            + ' \\u00b7 P&L impact shown per position \\u2192 [commit] (demo)';",
            "        bulkNote.style.color = 'var(--color-text-primary)';",
            "    };",
            "",
            "    root.appendChild(fdk.sectionTitle(branch, 'sect-2', 'Breaks \\u2014 aging \\u00b7 ownership'));",
            "    var breaks = fdk.el(branch, 'slot-2', 'div', null);",
            "    root.appendChild(breaks);",
            "    root.appendChild(fdk.el(branch, 'cap-1', 'div', [fd_caption, fd_muted, fd_section], ",
            "        'amendments are journaled events: re-price + re-explain automatic, P&L impact shown before commit,'",
            "        + ' materiality-based four-eyes'));",
            "",
            "    function itemRow(i) {",
            "        var line = fdk.el(branch, 'row-1', 'div', fd_row",
            "            + 'border-bottom:1px solid var(--color-border);font-size:12.5px;');",
            "        line.appendChild(fdk.chip(branch, 'chip-1', i.severity, i.kind));",
            "        var main = fdk.el(branch, 'spacer-1', 'div', fd_spacer);",
            "        main.appendChild(fdk.el(branch, 'strong-1', 'div', fd_strong, i.desc));",
            "        main.appendChild(fdk.el(branch, 'cap-2', 'div', fd_caption, i.state));",
            "        line.appendChild(main);",
            "        line.appendChild(fdk.el('span', 'color:var(--color-text-muted);font-size:11px;white-space:nowrap;', i.due));",
            "        return line;",
            "    }",
            "",
            "    function breakRow(b) {",
            "        var line = fdk.el(branch, 'row-2', 'div', fd_row",
            "            + 'border-bottom:1px solid var(--color-border);font-size:12.5px;');",
            "        line.appendChild(fdk.chip(branch, 'chip-2', b.severity, 'aging ' + b.age));",
            "        var main = fdk.el(branch, 'spacer-2', 'div', fd_spacer);",
            "        main.appendChild(fdk.el(branch, 'strong-2', 'div', fd_strong, b.desc));",
            "        main.appendChild(fdk.el(branch, 'cap-3', 'div', fd_caption, ",
            "            'vs ' + b.vs + ' \\u00b7 owner: ' + b.owner));",
            "        line.appendChild(main);",
            "        return line;",
            "    }",
            "",
            "    fetch('/fx/lifecycle')",
            "        .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
            "        .then(function (d) {",
            "            for (var i = 0; i < d.items.length; i++) items.appendChild(itemRow(d.items[i]));",
            "            for (i = 0; i < d.breaks.length; i++) breaks.appendChild(breakRow(d.breaks[i]));",
            "        })",
            "        .catch(function (e) {",
            "            items.appendChild(fdk.el(branch, 'err-1', 'div', fd_error_text, ",
            "                'lifecycle load failed: ' + (e && e.message ? e.message : e)));",
            "        });",
            "",
            "    return {",
            "        root: root,",
            "        setActive: function (active) {},",
            "        partyDeregister: function () {}",
            "    };");
    }
}
