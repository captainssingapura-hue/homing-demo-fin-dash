package hue.captains.singapura.js.homing.findash.risk;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashGridModule;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * Risk views (study §8): the book re-cut by risk's own hierarchy with limits
 * and utilization inline, and the breach worklist with acknowledgment. Risk
 * consumes the same epochs as everyone (P5) but on <b>snapshot cadence</b> —
 * the as-of is explicit and loud, so a snapshot is never mistaken for live.
 * No mark or model controls (independence is the point); what risk does get
 * is <b>annotation</b> — attach a note to any row, visible to the desk,
 * journaled (demo: screen state).
 */
public final class RiskViewsWidget
        extends WorkspaceWidget<WorkspaceWidget._None, RiskViewsWidget> {

    public static final RiskViewsWidget INSTANCE = new RiskViewsWidget();

    private RiskViewsWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, RiskViewsWidget> {}

    @Override protected _Construct<_None, RiskViewsWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Risk Views"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(
                new ModuleImports<>(List.of(new FinDashKitModule.fdk()), FinDashKitModule.INSTANCE),
                new ModuleImports<>(List.of(new FinDashGridModule.fdGrid()), FinDashGridModule.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
            "    var root = branch.createElement('root', 'div');",
            "    root.style.cssText = 'height:100%;overflow:auto;box-sizing:border-box;padding:14px;'",
            "        + 'font-family:system-ui,sans-serif;font-size:13px;color:#0b0b0b;';",
            "",
            "    var head = fdk.el('div', 'display:flex;align-items:baseline;gap:10px;flex-wrap:wrap;');",
            "    head.appendChild(fdk.el('span', 'font-weight:700;font-size:14px;letter-spacing:0.3px;',",
            "        'RISK VIEWS \\u2014 limits & utilization'));",
            "    var asOfSlot = fdk.el('span', '');",
            "    head.appendChild(asOfSlot);",
            "    root.appendChild(head);",
            "",
            "    var gridSlot = fdk.el('div', 'margin-top:8px;');",
            "    root.appendChild(gridSlot);",
            "",
            "    root.appendChild(fdk.sectionTitle('Breach / warning worklist'));",
            "    var worklist = fdk.el('div', '');",
            "    root.appendChild(worklist);",
            "    root.appendChild(fdk.el('div', 'color:#898781;font-size:11px;margin-top:6px;',",
            "        'no mark or model controls here \\u2014 independence of the view is the point \\u00b7 '",
            "        + 'annotations are journaled and visible to the desk (demo)'));",
            "",
            "    function breachRow(b) {",
            "        var line = fdk.el('div', 'display:flex;gap:10px;align-items:center;padding:6px 4px;'",
            "            + 'border-bottom:1px solid #e1e0d9;font-size:12.5px;');",
            "        line.appendChild(fdk.chip(b.severity, b.severity === 'serious' ? 'breach risk' : 'warning'));",
            "        var main = fdk.el('div', 'flex:1;');",
            "        main.appendChild(fdk.el('div', 'font-weight:600;', b.what));",
            "        main.appendChild(fdk.el('div', 'color:#52514e;font-size:11.5px;', b.detail));",
            "        line.appendChild(main);",
            "        if (b.state === 'unacknowledged') {",
            "            var ack = document.createElement('button');",
            "            ack.textContent = 'acknowledge';",
            "            ack.style.cssText = 'padding:2px 8px;font-size:11px;border:1px solid #c3c2b7;'",
            "                + 'border-radius:4px;background:#fcfcfb;color:#52514e;cursor:pointer;';",
            "            ack.onclick = function () {",
            "                while (line.lastChild !== main) line.removeChild(line.lastChild);",
            "                line.appendChild(fdk.el('span', 'color:#898781;font-size:11px;',",
            "                    'acknowledged now \\u00b7 journaled (demo)'));",
            "            };",
            "            line.appendChild(ack);",
            "        } else {",
            "            line.appendChild(fdk.el('span', 'color:#898781;font-size:11px;', b.state));",
            "        }",
            "        return line;",
            "    }",
            "",
            "    fetch('/fx/risk')",
            "        .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
            "        .then(function (d) {",
            "            asOfSlot.appendChild(fdk.chip('neutral', 'as-of ' + d.asOf + ' \\u2014 NOT live'));",
            "            var grid = fdGrid({",
            "                compact: true,",
            "                columns: [",
            "                    { key: 'book', label: 'Book' },",
            "                    { key: 'metric', label: 'Metric' },",
            "                    { key: 'usage', label: 'Usage', align: 'right' },",
            "                    { key: 'limit', label: 'Limit', align: 'right' },",
            "                    { key: 'frac', label: 'Utilization', render: function (v, row) {",
            "                        var cell = fdk.el('span', 'display:inline-flex;gap:6px;align-items:center;');",
            "                        cell.appendChild(fdk.meter(v, { width: 70 }));",
            "                        cell.appendChild(fdk.el('span', 'font-size:11px;color:#52514e;',",
            "                            Math.round(v * 100) + '%'));",
            "                        return cell;",
            "                    } },",
            "                    { key: 'note', label: 'Note', render: function (v, row) {",
            "                        var cell = fdk.el('span', 'display:inline-flex;gap:6px;align-items:center;');",
            "                        if (v) cell.appendChild(fdk.el('span', 'color:#9a6b1f;font-size:11px;', v));",
            "                        var note = document.createElement('button');",
            "                        note.textContent = '+ note';",
            "                        note.style.cssText = 'padding:1px 6px;font-size:10.5px;border:1px solid #c3c2b7;'",
            "                            + 'border-radius:4px;background:#fcfcfb;color:#898781;cursor:pointer;';",
            "                        note.onclick = function () {",
            "                            note.textContent = 'noted \\u2713 (journaled, visible to desk)';",
            "                            note.disabled = true;",
            "                        };",
            "                        cell.appendChild(note);",
            "                        return cell;",
            "                    } }",
            "                ],",
            "                rows: d.limits",
            "            });",
            "            gridSlot.appendChild(grid.root);",
            "            for (var i = 0; i < d.breaches.length; i++) worklist.appendChild(breachRow(d.breaches[i]));",
            "        })",
            "        .catch(function (e) {",
            "            gridSlot.appendChild(fdk.el('div', 'color:#a8502a;',",
            "                'risk load failed: ' + (e && e.message ? e.message : e)));",
            "        });",
            "",
            "    return {",
            "        root: root,",
            "        setActive: function (active) {},",
            "        partyDeregister: function () {}",
            "    };");
    }
}
