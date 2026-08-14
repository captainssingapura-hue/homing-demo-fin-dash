package hue.captains.singapura.js.homing.findash.risk;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashGridModule;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;
import hue.captains.singapura.js.homing.findash.core.css.FdFrameCss;
import hue.captains.singapura.js.homing.findash.core.css.FdStatusCss;
import hue.captains.singapura.js.homing.findash.core.css.FdTextCss;

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
                new ModuleImports<>(List.of(new FinDashGridModule.fdGrid()), FinDashGridModule.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdFrameCss.fd_cluster(),
                        new FdFrameCss.fd_header_row(),
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
            "    var head = fdk.el(branch, 'head-1', 'div', fd_header_row);",
            "    head.appendChild(fdk.el(branch, 'title-1', 'span', fd_title, ",
            "        'RISK VIEWS \\u2014 limits & utilization'));",
            "    var asOfSlot = fdk.el(branch, 'slot-1', 'span', null);",
            "    head.appendChild(asOfSlot);",
            "    root.appendChild(head);",
            "",
            "    var gridSlot = fdk.el(branch, 'sect-1', 'div', fd_section);",
            "    root.appendChild(gridSlot);",
            "",
            "    root.appendChild(fdk.sectionTitle(branch, 'sect-1', 'Breach / warning worklist'));",
            "    var worklist = fdk.el(branch, 'slot-2', 'div', null);",
            "    root.appendChild(worklist);",
            "    root.appendChild(fdk.el(branch, 'cap-1', 'div', [fd_caption, fd_muted, fd_section], ",
            "        'no mark or model controls here \\u2014 independence of the view is the point \\u00b7 '",
            "        + 'annotations are journaled and visible to the desk (demo)'));",
            "",
            "    function breachRow(b) {",
            "        var line = fdk.el(branch, 'row-1', 'div', fd_row",
            "            + 'border-bottom:1px solid var(--color-border);font-size:12.5px;');",
            "        line.appendChild(fdk.chip(branch, 'chip-1', b.severity, b.severity === 'serious' ? 'breach risk' : 'warning'));",
            "        var main = fdk.el(branch, 'spacer-1', 'div', fd_spacer);",
            "        main.appendChild(fdk.el(branch, 'strong-1', 'div', fd_strong, b.what));",
            "        main.appendChild(fdk.el(branch, 'cap-2', 'div', fd_caption, b.detail));",
            "        line.appendChild(main);",
            "        if (b.state === 'unacknowledged') {",
            "            var ack = document.createElement('button');",
            "            ack.textContent = 'acknowledge';",
            "            ack.style.cssText = 'padding:2px 8px;font-size:11px;border:1px solid var(--color-border);'",
            "                + 'border-radius:4px;background:var(--color-surface);color:var(--color-text-primary);cursor:pointer;';",
            "            ack.onclick = function () {",
            "                while (line.lastChild !== main) line.removeChild(line.lastChild);",
            "                line.appendChild(fdk.el(branch, 'cap-3', 'span', [fd_caption, fd_muted], ",
            "                    'acknowledged now \\u00b7 journaled (demo)'));",
            "            };",
            "            line.appendChild(ack);",
            "        } else {",
            "            line.appendChild(fdk.el(branch, 'cap-4', 'span', [fd_caption, fd_muted], b.state));",
            "        }",
            "        return line;",
            "    }",
            "",
            "    fetch('/fx/risk')",
            "        .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
            "        .then(function (d) {",
            "            asOfSlot.appendChild(fdk.chip(branch, 'chip-2', 'neutral', 'as-of ' + d.asOf + ' \\u2014 NOT live'));",
            "            var grid = fdGrid(branch, {",
            "                compact: true,",
            "                columns: [",
            "                    { key: 'book', label: 'Book' },",
            "                    { key: 'metric', label: 'Metric' },",
            "                    { key: 'usage', label: 'Usage', align: 'right' },",
            "                    { key: 'limit', label: 'Limit', align: 'right' },",
            "                    { key: 'frac', label: 'Utilization', render: function (v, row) {",
            "                        var cell = fdk.el(branch, 'cluster-1', 'span', fd_cluster);",
            "                        cell.appendChild(fdk.meter(v, { width: 70 }));",
            "                        cell.appendChild(fdk.el('span', 'font-size:11px;color:var(--color-text-primary);', ",
            "                            Math.round(v * 100) + '%'));",
            "                        return cell;",
            "                    } },",
            "                    { key: 'note', label: 'Note', render: function (v, row) {",
            "                        var cell = fdk.el(branch, 'cluster-2', 'span', fd_cluster);",
            "                        if (v) cell.appendChild(fdk.el('span', 'color:#9a6b1f;font-size:11px;', v));",
            "                        var note = document.createElement('button');",
            "                        note.textContent = '+ note';",
            "                        note.style.cssText = 'padding:1px 6px;font-size:10.5px;border:1px solid var(--color-border);'",
            "                            + 'border-radius:4px;background:var(--color-surface);color:var(--color-text-muted);cursor:pointer;';",
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
            "            gridSlot.appendChild(fdk.el(branch, 'err-1', 'div', fd_error_text, ",
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
