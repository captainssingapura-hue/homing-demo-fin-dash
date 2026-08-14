package hue.captains.singapura.js.homing.findash.trader;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.css.FdFrameCss;
import hue.captains.singapura.js.homing.findash.core.css.FdStatusCss;
import hue.captains.singapura.js.homing.findash.core.css.FdSurfaceCss;
import hue.captains.singapura.js.homing.findash.core.css.FdTextCss;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashGridModule;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * W4 — the position &amp; risk blotter: the book bucketed the way the desk
 * thinks (pair ▸ tenor), smile-bucket vega columns, and the two-speed
 * machinery made honest (P2): every row shows its revaluation freshness —
 * error-budget consumption as a meter, last full reval age. Clicking a row
 * publishes {@code InstrumentSelected} on the desk party — the pricer and
 * surface manager follow the selection (the UI study's cross-widget flow).
 * Barrier proximity and expiry/pin clusters live in their own widgets
 * ({@link BarrierWatchWidget}, {@link ExpiryClustersWidget}).
 *
 * <p><b>Typed-CSS pilot.</b> First widget migrated off inline styling: every
 * element comes from {@code branch.createElement} with a stable name and is
 * styled with {@code css.setClass} against the shared vocabulary in
 * {@code findash.core.css} — no style strings, no literal colours. See
 * {@code docs/css-class-inventory.md}.</p>
 */
public final class RiskBlotterWidget
        extends WorkspaceWidget<WorkspaceWidget._None, RiskBlotterWidget> {

    public static final RiskBlotterWidget INSTANCE = new RiskBlotterWidget();

    private RiskBlotterWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, RiskBlotterWidget> {}

    @Override protected _Construct<_None, RiskBlotterWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Risk Blotter"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(
                new ModuleImports<>(List.of(new FinDashKitModule.fdk()), FinDashKitModule.INSTANCE),
                new ModuleImports<>(List.of(new FinDashGridModule.fdGrid()), FinDashGridModule.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdFrameCss.fd_widget_root(),
                        new FdFrameCss.fd_header_row(),
                        new FdFrameCss.fd_cluster(),
                        new FdFrameCss.fd_section()),
                        FdFrameCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdTextCss.fd_title(),
                        new FdTextCss.fd_caption(),
                        new FdTextCss.fd_muted(),
                        new FdTextCss.fd_strong()),
                        FdTextCss.INSTANCE),
                new ModuleImports<>(List.of(new FdSurfaceCss.fd_panel()), FdSurfaceCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdStatusCss.fd_status_warn(),
                        new FdStatusCss.fd_error_text()),
                        FdStatusCss.INSTANCE));
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
            "        actorId = 'trader/blotter-' + Math.random().toString(36).slice(2, 8);",
            "        party.joinActor({ id: actorId, parentSecretary: 'desk', reactors: {} });",
            "    }",
            "",
            "    var head = branch.createElement('head', 'div');",
            "    css.setClass(head, fd_header_row);",
            "    var titleEl = branch.createElement('title', 'span');",
            "    css.setClass(titleEl, fd_title);",
            "    titleEl.textContent = 'RISK \\u2014 FXO BOOK';",
            "    head.appendChild(titleEl);",
            "    var asOf = branch.createElement('as-of', 'span');",
            "    css.setClass(asOf, fd_caption);",
            "    asOf.textContent = 'as-of: live';",
            "    head.appendChild(asOf);",
            "    var stampSlot = branch.createElement('stamp-slot', 'span');",
            "    head.appendChild(stampSlot);",
            "    root.appendChild(head);",
            "",
            "    var totals = branch.createElement('totals', 'div');",
            "    css.setClass(totals, fd_panel);",
            "    css.addClass(totals, fd_header_row);",
            "    css.addClass(totals, fd_section);",
            "    root.appendChild(totals);",
            "",
            "    var gridSlot = branch.createElement('grid-slot', 'div');",
            "    root.appendChild(gridSlot);",
            "    var footer = branch.createElement('footer', 'div');",
            "    css.setClass(footer, fd_caption);",
            "    css.addClass(footer, fd_muted);",
            "    footer.textContent = 'drill: bucket \\u2192 position \\u2192 trade \\u2192 pricing explain (P1)'",
            "        + ' \\u00b7 click a row to broadcast the selection'",
            "        + ' \\u00b7 barriers + expiries in their own widgets';",
            "    root.appendChild(footer);",
            "",
            "    // Each totals item needs its own branch-owned pair, so the name is",
            "    // keyed rather than positional — stable across re-render.",
            "    function totalsItem(key, label, valueNode) {",
            "        var box = branch.createElement('totals-item-' + key, 'span');",
            "        css.setClass(box, fd_cluster);",
            "        var lab = branch.createElement('totals-label-' + key, 'span');",
            "        css.setClass(lab, fd_muted);",
            "        lab.textContent = label;",
            "        box.appendChild(lab);",
            "        box.appendChild(valueNode);",
            "        return box;",
            "    }",
            "",
            "    function totalsValue(key, text) {",
            "        var v = branch.createElement('totals-value-' + key, 'span');",
            "        css.setClass(v, fd_strong);",
            "        v.textContent = text;",
            "        return v;",
            "    }",
            "",
            "    function render(d) {",
            "        stampSlot.appendChild(fdk.stamp({ slice: d.slice, model: d.model }));",
            "        var t = d.totals;",
            "        totals.appendChild(totalsItem('delta', '\\u0394',",
            "            totalsValue('delta', '\\u20ac' + fdk.fmt.compact(t.delta))));",
            "        totals.appendChild(totalsItem('vega', 'Vega',",
            "            totalsValue('vega', '\\u20ac' + fdk.fmt.compact(t.vega))));",
            "        totals.appendChild(totalsItem('theta', '\\u0398',",
            "            totalsValue('theta', '\\u2212\\u20ac' + fdk.fmt.compact(Math.abs(t.theta)))));",
            "        totals.appendChild(totalsItem('vega-limit',",
            "            'vega limit ' + Math.round(t.vegaLimitFrac * 100) + '%',",
            "            fdk.meter(t.vegaLimitFrac)));",
            "        totals.appendChild(totalsItem('reval-wave',",
            "            'reval wave ' + Math.round(t.revalWaveFrac * 100) + '%',",
            "            fdk.meter(t.revalWaveFrac, { warnAt: 2 })));",
            "",
            "        var rows = [];",
            "        for (var i = 0; i < d.rows.length; i++) {",
            "            var r = d.rows[i];",
            "            rows.push({",
            "                book: r.group ? r.pair : r.tenor, _group: r.group, _indent: r.group ? 0 : 1,",
            "                pair: r.pair, tenor: r.tenor,",
            "                delta: fdk.fmt.compact(r.delta), vAtm: fdk.fmt.compact(r.vAtm),",
            "                vRr: fdk.fmt.compact(r.vRr), vBf: fdk.fmt.compact(r.vBf),",
            "                theta: fdk.fmt.compact(r.theta),",
            "                freshSecs: r.freshSecs, budgetFrac: r.budgetFrac, note: r.note",
            "            });",
            "        }",
            "        var grid = fdGrid({",
            "            compact: true,",
            "            columns: [",
            "                { key: 'book', label: 'Book (pair \\u25b8 tenor)' },",
            "                { key: 'delta', label: '\\u0394', align: 'right' },",
            "                { key: 'vAtm', label: 'vATM', align: 'right' },",
            "                { key: 'vRr', label: 'vRR', align: 'right' },",
            "                { key: 'vBf', label: 'vBF', align: 'right' },",
            "                { key: 'theta', label: '\\u0398', align: 'right' },",
            "                { key: 'freshSecs', label: 'freshness', render: function (v, row) {",
            "                    var rowKey = row.pair + '-' + row.tenor;",
            "                    var cell = branch.createElement('fresh-' + rowKey, 'span');",
            "                    css.setClass(cell, fd_cluster);",
            "                    cell.appendChild(fdk.meter(row.budgetFrac, { width: 44 }));",
            "                    var age = branch.createElement('fresh-age-' + rowKey, 'span');",
            "                    // Stale past 10s is a state, not decoration — it reads as a",
            "                    // warning rather than muted secondary text (P2).",
            "                    css.setClass(age, fd_caption);",
            "                    css.addClass(age, v > 10 ? fd_status_warn : fd_muted);",
            "                    age.textContent = v + 's';",
            "                    cell.appendChild(age);",
            "                    if (row.note) cell.appendChild(fdk.chip('warn', row.note));",
            "                    return cell;",
            "                } }",
            "            ],",
            "            rows: rows,",
            "            onRowClick: function (row) {",
            "                if (party && actorId) {",
            "                    party.tellFrom(actorId, { kind: 'InstrumentSelected',",
            "                        instrument: { pair: row.pair, tenor: row.tenor } });",
            "                }",
            "            }",
            "        });",
            "        gridSlot.appendChild(grid.root);",
            "    }",
            "",
            "    fetch('/fx/book')",
            "        .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
            "        .then(render)",
            "        .catch(function (e) {",
            "            var msg = branch.createElement('load-error', 'div');",
            "            css.setClass(msg, fd_error_text);",
            "            msg.textContent = 'book load failed: ' + (e && e.message ? e.message : e);",
            "            gridSlot.appendChild(msg);",
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
