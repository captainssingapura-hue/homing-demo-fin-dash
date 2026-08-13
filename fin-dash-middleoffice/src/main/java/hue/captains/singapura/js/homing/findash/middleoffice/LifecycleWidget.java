package hue.captains.singapura.js.homing.findash.middleoffice;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

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
            "        'LIFECYCLE WORKSTATION'));",
            "    root.appendChild(fdk.el('div', 'color:var(--color-text-primary);font-size:11.5px;margin:2px 0 4px;',",
            "        'cut times desk-local, UTC on hover \\u00b7 every event exactly once, journaled'));",
            "",
            "    root.appendChild(fdk.sectionTitle('Events'));",
            "    var items = fdk.el('div', '');",
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
            "    root.appendChild(fdk.sectionTitle('Breaks \\u2014 aging \\u00b7 ownership'));",
            "    var breaks = fdk.el('div', '');",
            "    root.appendChild(breaks);",
            "    root.appendChild(fdk.el('div', 'color:var(--color-text-muted);font-size:11px;margin-top:6px;',",
            "        'amendments are journaled events: re-price + re-explain automatic, P&L impact shown before commit,'",
            "        + ' materiality-based four-eyes'));",
            "",
            "    function itemRow(i) {",
            "        var line = fdk.el('div', 'display:flex;gap:10px;align-items:center;padding:6px 4px;'",
            "            + 'border-bottom:1px solid var(--color-border);font-size:12.5px;');",
            "        line.appendChild(fdk.chip(i.severity, i.kind));",
            "        var main = fdk.el('div', 'flex:1;');",
            "        main.appendChild(fdk.el('div', 'font-weight:600;', i.desc));",
            "        main.appendChild(fdk.el('div', 'color:var(--color-text-primary);font-size:11.5px;', i.state));",
            "        line.appendChild(main);",
            "        line.appendChild(fdk.el('span', 'color:var(--color-text-muted);font-size:11px;white-space:nowrap;', i.due));",
            "        return line;",
            "    }",
            "",
            "    function breakRow(b) {",
            "        var line = fdk.el('div', 'display:flex;gap:10px;align-items:center;padding:6px 4px;'",
            "            + 'border-bottom:1px solid var(--color-border);font-size:12.5px;');",
            "        line.appendChild(fdk.chip(b.severity, 'aging ' + b.age));",
            "        var main = fdk.el('div', 'flex:1;');",
            "        main.appendChild(fdk.el('div', 'font-weight:600;', b.desc));",
            "        main.appendChild(fdk.el('div', 'color:var(--color-text-primary);font-size:11.5px;',",
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
            "            items.appendChild(fdk.el('div', 'color:#a8502a;',",
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
