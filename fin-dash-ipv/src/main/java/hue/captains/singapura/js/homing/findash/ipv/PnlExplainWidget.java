package hue.captains.singapura.js.homing.findash.ipv;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * Product control (study §11): the P&amp;L explain — every attribution term
 * computed from the same journaled slices the desk traded on (P5), with
 * unexplained-above-tolerance opening a drill-down, not an argument — plus
 * the IPV workbench (desk marks vs independent sources, materiality-weighted)
 * and the official EOD marks sign-off (a journaled approval that stamps the
 * epoch official; demo: screen state).
 */
public final class PnlExplainWidget
        extends WorkspaceWidget<WorkspaceWidget._None, PnlExplainWidget> {

    public static final PnlExplainWidget INSTANCE = new PnlExplainWidget();

    private PnlExplainWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, PnlExplainWidget> {}

    @Override protected _Construct<_None, PnlExplainWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "P&L Explain / IPV"; }
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
            "        'P&L EXPLAIN'));",
            "    var totalSlot = fdk.el('span', '');",
            "    head.appendChild(totalSlot);",
            "    root.appendChild(head);",
            "",
            "    var terms = fdk.el('div', 'max-width:380px;margin-top:6px;font-variant-numeric:tabular-nums;');",
            "    root.appendChild(terms);",
            "",
            "    root.appendChild(fdk.sectionTitle('IPV workbench \\u2014 desk vs independent'));",
            "    var ipv = fdk.el('div', '');",
            "    root.appendChild(ipv);",
            "",
            "    root.appendChild(fdk.sectionTitle('Official EOD marks'));",
            "    var marks = fdk.el('div', 'display:flex;gap:8px;align-items:center;flex-wrap:wrap;font-size:12px;');",
            "    root.appendChild(marks);",
            "",
            "    function termRow(t, tol) {",
            "        var row = fdk.el('div', 'display:flex;justify-content:space-between;padding:2px 0;'",
            "            + 'border-bottom:1px solid var(--color-border);font-size:12.5px;'",
            "            + (t.emphasis ? 'font-weight:700;' : ''));",
            "        row.appendChild(fdk.el('span', 'color:var(--color-text-primary);', t.term",
            "            + (t.emphasis ? '  (tol ' + tol + ')' : '')));",
            "        var amt = fdk.el('span', '', t.amount);",
            "        if (t.emphasis) {",
            "            var within = t.amount.indexOf('\\u2212') < 0;",  // demo: +6k within
            "            row.appendChild(fdk.el('span', 'display:inline-flex;gap:6px;align-items:center;'));",
            "            row.lastChild.appendChild(amt);",
            "            row.lastChild.appendChild(fdk.chip('good', 'within tol'));",
            "        } else {",
            "            row.appendChild(amt);",
            "        }",
            "        return row;",
            "    }",
            "",
            "    function ipvRow(r) {",
            "        var line = fdk.el('div', 'display:flex;gap:10px;align-items:center;padding:6px 4px;'",
            "            + 'border-bottom:1px solid var(--color-border);font-size:12.5px;');",
            "        line.appendChild(fdk.chip(r.severity, r.variance));",
            "        var main = fdk.el('div', '');",
            "        main.appendChild(fdk.el('div', 'font-weight:600;',",
            "            r.scope + '  \\u00b7  desk ' + r.desk + ' vs indep ' + r.independent));",
            "        main.appendChild(fdk.el('div', 'color:var(--color-text-primary);font-size:11.5px;', r.note));",
            "        line.appendChild(main);",
            "        return line;",
            "    }",
            "",
            "    fetch('/fx/pnl')",
            "        .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
            "        .then(function (d) {",
            "            totalSlot.appendChild(fdk.el('span', 'font-size:18px;font-weight:700;', d.total));",
            "            totalSlot.appendChild(fdk.el('span', 'color:var(--color-text-muted);font-size:11px;margin-left:8px;',",
            "                'every term from the journaled slices the desk traded on (P5)'));",
            "            for (var i = 0; i < d.terms.length; i++) {",
            "                terms.appendChild(termRow(d.terms[i], d.unexplainedTol));",
            "            }",
            "            for (i = 0; i < d.ipv.length; i++) ipv.appendChild(ipvRow(d.ipv[i]));",
            "            marks.appendChild(fdk.el('span', 'color:var(--color-text-primary);', d.marksState));",
            "            var sign = document.createElement('button');",
            "            sign.textContent = 'Sign off official marks';",
            "            sign.style.cssText = 'padding:5px 12px;font-size:12px;font-weight:600;color:var(--color-surface);'",
            "                + 'background:#2a78d6;border:none;border-radius:6px;cursor:pointer;';",
            "            sign.onclick = function () {",
            "                while (marks.firstChild) marks.removeChild(marks.firstChild);",
            "                marks.appendChild(fdk.chip('good', 'S513/87 stamped OFFICIAL'));",
            "                marks.appendChild(fdk.el('span', 'color:var(--color-text-muted);font-size:11.5px;',",
            "                    'journaled approval \\u00b7 the same epoch machinery, with a governance bit (demo)'));",
            "            };",
            "            marks.appendChild(sign);",
            "        })",
            "        .catch(function (e) {",
            "            terms.appendChild(fdk.el('div', 'color:#a8502a;',",
            "                'pnl load failed: ' + (e && e.message ? e.message : e)));",
            "        });",
            "",
            "    return {",
            "        root: root,",
            "        setActive: function (active) {},",
            "        partyDeregister: function () {}",
            "    };");
    }
}
