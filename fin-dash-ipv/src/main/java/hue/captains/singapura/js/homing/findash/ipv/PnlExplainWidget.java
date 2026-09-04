package hue.captains.singapura.js.homing.findash.ipv;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;
import hue.captains.singapura.js.homing.findash.core.css.FdFrameCss;
import hue.captains.singapura.js.homing.findash.core.css.FdStatusCss;
import hue.captains.singapura.js.homing.findash.core.css.FdTextCss;
import hue.captains.singapura.js.homing.findash.core.css.FdSurfaceCss;
import hue.captains.singapura.js.homing.findash.core.css.FdControlCss;

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
                List.of(new FinDashKitModule.fdk()), FinDashKitModule.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdFrameCss.fd_hidden(),
                        new FdFrameCss.fd_section(),
                        new FdFrameCss.fd_split_row(),
                        new FdFrameCss.fd_cluster(),
                        new FdFrameCss.fd_header_row(),
                        new FdFrameCss.fd_row(),
                        new FdFrameCss.fd_widget_root()),
                        FdFrameCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdStatusCss.fd_error_text()),
                        FdStatusCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdTextCss.fd_strongest(),
                        new FdTextCss.fd_dense(),
                        new FdTextCss.fd_display(),
                        new FdTextCss.fd_muted(),
                        new FdTextCss.fd_num(),
                        new FdTextCss.fd_caption(),
                        new FdTextCss.fd_strong(),
                        new FdTextCss.fd_title()),
                        FdTextCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdSurfaceCss.fd_rule()),
                        FdSurfaceCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdControlCss.fd_btn()),
                        FdControlCss.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
            "    var root = branch.createElement('root', 'div');",
            "    css.setClass(root, fd_widget_root);",
            "",
            "    var head = fdk.el(branch, 'head-1', 'div', fd_header_row);",
            "    head.appendChild(fdk.el(branch, 'title-1', 'span', fd_title, ",
            "        'P&L EXPLAIN'));",
            "    var totalSlot = fdk.el(branch, 'slot-1', 'span', null);",
            "    head.appendChild(totalSlot);",
            "    root.appendChild(head);",
            "",
            "    var terms = fdk.el(branch, 'el-1', 'div', [fd_num, fd_section]);",
            "    root.appendChild(terms);",
            "",
            "    root.appendChild(fdk.sectionTitle(branch, 'sect-1', 'IPV workbench \\u2014 desk vs independent'));",
            "    var ipv = fdk.el(branch, 'slot-2', 'div', null);",
            "    root.appendChild(ipv);",
            "",
            "    root.appendChild(fdk.sectionTitle(branch, 'sect-2', 'Official EOD marks'));",
            "    var marks = fdk.el(branch, 'head-1-2', 'div', [fd_dense, fd_header_row]);",
            "    root.appendChild(marks);",
            "",
            "    function termRow(rk, t, tol) {",
            "        var row = fdk.el(branch, 'row-1-' + rk, 'div', [fd_split_row, fd_rule, fd_dense]);",
            "        // The unexplained term is the one that starts an argument, so it",
            "        // carries weight rather than blending into the attribution list.",
            "        if (t.emphasis) css.addClass(row, fd_strongest);",
            "        row.appendChild(fdk.el(branch, 'txt-1-' + rk, 'span', null, t.term",
            "            + (t.emphasis ? '  (tol ' + tol + ')' : '')));",
            "        var amt = fdk.el(branch, 'slot-3-' + rk, 'span', null, t.amount);",
            "        if (t.emphasis) {",
            "            var within = t.amount.indexOf('\\u2212') < 0;",  // demo: +6k within
            "            row.appendChild(fdk.el(branch, 'cluster-1-' + rk, 'span', fd_cluster));",
            "            row.lastChild.appendChild(amt);",
            "            row.lastChild.appendChild(fdk.chip(branch, 'chip-1-' + rk, 'good', 'within tol'));",
            "        } else {",
            "            row.appendChild(amt);",
            "        }",
            "        return row;",
            "    }",
            "",
            "    function ipvRow(rk, r) {",
            "        var line = fdk.el(branch, 'ipv-' + rk, 'div', [fd_row, fd_rule]);",
            "        line.appendChild(fdk.chip(branch, 'chip-2-' + rk, r.severity, r.variance));",
            "        var main = fdk.el(branch, 'slot-4-' + rk, 'div', null);",
            "        main.appendChild(fdk.el(branch, 'strong-1-' + rk, 'div', fd_strong, ",
            "            r.scope + '  \\u00b7  desk ' + r.desk + ' vs indep ' + r.independent));",
            "        main.appendChild(fdk.el(branch, 'cap-1-' + rk, 'div', fd_caption, r.note));",
            "        line.appendChild(main);",
            "        return line;",
            "    }",
            "",
            "    fdk.load('/fx/pnl', { branch: branch, host: terms, what: 'pnl' }, function (d) {",
            "            totalSlot.appendChild(fdk.el(branch, 'display-1', 'span', fd_display, d.total));",
            "            totalSlot.appendChild(fdk.el(branch, 'cap-1', 'span', [fd_muted, fd_caption], ",
            "                'every term from the journaled slices the desk traded on (P5)'));",
            "            for (var i = 0; i < d.terms.length; i++) {",
            "                terms.appendChild(termRow(i, d.terms[i], d.unexplainedTol));",
            "            }",
            "            for (i = 0; i < d.ipv.length; i++) ipv.appendChild(ipvRow(i, d.ipv[i]));",
            "            marks.appendChild(fdk.el(branch, 'marks-state', 'span', null, d.marksState));",
            "            var sign = fdk.el(branch, 'sign', 'button', fd_btn, 'Sign off official marks');",
            "            // The signed-off strip is built up front and revealed, rather than",
            "            // tearing the marks row's children out from under the branch.",
            "            var signed = fdk.el(branch, 'signed', 'span', [fd_cluster, fd_hidden]);",
            "            signed.appendChild(fdk.chip(branch, 'signed-chip', 'good', 'S513/87 stamped OFFICIAL'));",
            "            signed.appendChild(fdk.el(branch, 'signed-note', 'span', [fd_caption, fd_muted], ",
            "                'journaled approval \\u00b7 the same epoch machinery, with a governance bit (demo)'));",
            "            sign.onclick = function () {",
            "                css.addClass(sign, fd_hidden);",
            "                css.removeClass(signed, fd_hidden);",
            "            };",
            "            marks.appendChild(sign);",
            "            marks.appendChild(signed);",
"    });",
            "",
            "    return {",
            "        root: root,",
            "        setActive: function (active) {},",
            "        partyDeregister: function () {}",
            "    };");
    }
}
