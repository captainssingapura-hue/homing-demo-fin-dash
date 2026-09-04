package hue.captains.singapura.js.homing.findash.trader;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;
import hue.captains.singapura.js.homing.findash.core.kit.SmileChartModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;
import hue.captains.singapura.js.homing.findash.core.css.FdFrameCss;
import hue.captains.singapura.js.homing.findash.core.css.FdStatusCss;
import hue.captains.singapura.js.homing.findash.core.css.FdTextCss;
import hue.captains.singapura.js.homing.findash.core.css.FdControlCss;

import java.util.List;

/**
 * W2 — the surface manager: fitted smile vs raw market quotes per pillar
 * (delta space), per-tenor gate/quality flags, the two-speed state (epoch +
 * staleness, and what recalibration is waiting on) always visible (P2), and
 * Ring-3 pillar override entry that shows its audit consequence at entry time
 * (P3) — reason required, auto-expiring, flags downstream consumers. Demo:
 * the override is displayed, not journaled.
 *
 * <p>Joins the desk party: {@code InstrumentChanged} switches the pair.</p>
 */
public final class SurfaceManagerWidget
        extends WorkspaceWidget<WorkspaceWidget._None, SurfaceManagerWidget> {

    public static final SurfaceManagerWidget INSTANCE = new SurfaceManagerWidget();

    private SurfaceManagerWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, SurfaceManagerWidget> {}

    @Override protected _Construct<_None, SurfaceManagerWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Surface Manager"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(
                new ModuleImports<>(List.of(new FinDashKitModule.fdk()), FinDashKitModule.INSTANCE),
                new ModuleImports<>(List.of(new SmileChartModule.smileChart()), SmileChartModule.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdFrameCss.fd_cluster(),
                        new FdFrameCss.fd_grid_pairs(),
                        new FdFrameCss.fd_section(),
                        new FdFrameCss.fd_header_row(),
                        new FdFrameCss.fd_widget_root()),
                        FdFrameCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdStatusCss.fd_status_warn(),
                        new FdStatusCss.fd_error_text()),
                        FdStatusCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdTextCss.fd_caption(),
                        new FdTextCss.fd_dense(),
                        new FdTextCss.fd_mono(),
                        new FdTextCss.fd_num(),
                        new FdTextCss.fd_strong(),
                        new FdTextCss.fd_muted(),
                        new FdTextCss.fd_title()),
                        FdTextCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdControlCss.fd_btn(),
                        new FdControlCss.fd_input(),
                        new FdControlCss.fd_tab(),
                        new FdControlCss.fd_tab_active()),
                        FdControlCss.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
            "    var root = branch.createElement('root', 'div');",
            "    css.setClass(root, fd_widget_root);",
            "",
            "    var head = fdk.el(branch, 'head-1', 'div', fd_header_row);",
            "    var title = fdk.el(branch, 'title-1', 'span', fd_title, 'SURFACE MANAGER');",
            "    head.appendChild(title);",
            "    var stateSlot = fdk.el(branch, 'state', 'span', fd_cluster);",
            "    head.appendChild(stateSlot);",
            "    root.appendChild(head);",
            "    var tabs = fdk.el(branch, 'tabs', 'div', [fd_header_row, fd_section]);",
            "    root.appendChild(tabs);",
            "    var body = fdk.el(branch, 'slot-1', 'div', null);",
            "    root.appendChild(body);",
            "",
            "    var current = { pair: 'EURUSD', tenorIdx: -1, data: null, override: null };",
            "",
            "    // Three regions re-render independently: the state strip (per load),",
            "    // the pair tabs (once), and the tenor pane (per tab click). Each owns a",
            "    // sub-branch that is dissolved first — names are unique for a branch's",
            "    // life, and dissolve() detaches the old DOM, so no clear() loops.",
            "    var stateBranch = null, tabsBranch = null, paneBranch = null;",
            "",
            "    function tabBtn(b, key, label, selected, onClick) {",
            "        var el = fdk.el(b, key, 'button', fd_tab, label);",
            "        if (selected) css.addClass(el, fd_tab_active);",
            "        el.onclick = onClick;",
            "        return el;",
            "    }",
            "",
            "    function renderTenor(d, idx) {",
            "        current.tenorIdx = idx;",
            "        if (paneBranch) paneBranch.dissolve();",
            "        paneBranch = branch.createBranch('pane');",
            "        paneBranch.activate(root);",
            "        var b = paneBranch;",
            "        var t = d.tenors[idx];",
            "        var pane = fdk.el(b, 'pane', 'div', null);",
            "",
            "        var strip = fdk.el(b, 'tenor-strip', 'div', [fd_header_row, fd_section]);",
            "        for (var i = 0; i < d.tenors.length; i++) {",
            "            (function (i2) {",
            "                var tn = d.tenors[i2];",
            "                strip.appendChild(tabBtn(b, 'tenor-' + i2,",
            "                    (tn.flagged ? '\\u25b2 ' : '') + tn.tenor, i2 === idx,",
            "                    function () { body.appendChild(renderTenor(d, i2)); }));",
            "            })(i);",
            "        }",
            "        pane.appendChild(strip);",
            "",
            "        var over = current.override;",
            "        var pts = [];",
            "        for (var s = 0; s < t.smile.length; s++) {",
            "            pts.push({ label: t.smile[s].label, mkt: t.smile[s].mkt, fit: t.smile[s].fit });",
            "        }",
            "        pane.appendChild(smileChart({ width: 420, height: 190, pillars: pts }));",
            "",
            "        var residBad = t.resid > t.tol;",
            "        var residLine = fdk.el(b, 'resid', 'div', [fd_cluster, fd_section]);",
            "        residLine.appendChild(fdk.chip(b, 'resid-chip', residBad ? 'warn' : 'good',",
            "            'fit resid ' + t.resid.toFixed(2) + (residBad ? ' > tol ' : ' \\u2264 tol ') + t.tol.toFixed(2)));",
            "        if (t.flagged) residLine.appendChild(fdk.chip(b, 'flag-chip', 'warn', t.flagNote));",
            "        pane.appendChild(residLine);",
            "",
            "        pane.appendChild(fdk.sectionTitle(b, 'sect-pillars', t.tenor + ' pillars'));",
            "        var tbl = fdk.el(b, 'pillars', 'div', [fd_grid_pairs, fd_dense, fd_num]);",
            "        ['', 'Mkt', 'Fit', 'Ovr'].forEach(function (h, hi) {",
            "            tbl.appendChild(fdk.el(b, 'ph-' + hi, 'span', [fd_caption, fd_muted, fd_strong], h));",
            "        });",
            "        for (var p = 0; p < t.pillars.length; p++) {",
            "            var pr = t.pillars[p];",
            "            var isOv = over && over.tenor === t.tenor && over.name === pr.name;",
            "            tbl.appendChild(fdk.el(b, 'p-name-' + p, 'span', null, pr.name));",
            "            tbl.appendChild(fdk.el(b, 'p-mkt-' + p, 'span', null, pr.mkt.toFixed(2)));",
            "            tbl.appendChild(fdk.el(b, 'p-fit-' + p, 'span', null, pr.fit.toFixed(2)));",
            "            // An overridden pillar is a state — icon plus warning ink, not colour alone (P2).",
            "            tbl.appendChild(isOv",
            "                ? fdk.el(b, 'p-ovr-' + p, 'span', [fd_strong, fd_status_warn], '\\u25b2 ' + over.value)",
            "                : fdk.el(b, 'p-ovr-' + p, 'span', fd_muted, '\\u2014'));",
            "        }",
            "        pane.appendChild(tbl);",
            "",
            "        pane.appendChild(fdk.sectionTitle(b, 'sect-ovr', 'Override \\u2014 Ring 3 \\u00b7 audited \\u00b7 expiring'));",
            "        var ov = fdk.el(b, 'ovr-row', 'div', [fd_cluster, fd_dense]);",
            "        ov.appendChild(fdk.el(b, 'ovr-label', 'span', null, 'RR25 \\u2192'));",
            "        var ovVal = fdk.el(b, 'ovr-value', 'input', [fd_input, fd_mono]);",
            "        ovVal.type = 'text'; ovVal.value = '\\u22121.10';",
            "        ov.appendChild(ovVal);",
            "        var reason = fdk.el(b, 'ovr-reason', 'select', fd_input);",
            "        ['stale broker run', 'event repricing', 'client axe', 'fat quote filtered']",
            "            .forEach(function (r, ri) {",
            "                reason.appendChild(fdk.el(b, 'ovr-opt-' + ri, 'option', null, r));",
            "            });",
            "        ov.appendChild(reason);",
            "        var apply = fdk.el(b, 'ovr-apply', 'button', fd_btn, 'Apply \\u23ce');",
            "        ov.appendChild(apply);",
            "        pane.appendChild(ov);",
            "        // Built once in the right state rather than written then wiped —",
            "        // the pane is dissolved and rebuilt on every change anyway.",
            "        var live = over && over.tenor === t.tenor;",
            "        var audit = fdk.el(b, 'audit', 'div', [fd_caption, fd_muted, fd_section],",
            "            live ? null",
            "                 : 'expires 17:00 SGT \\u00b7 flags 3 consumers \\u00b7 audit record shown on commit');",
            "        pane.appendChild(audit);",
            "        apply.onclick = function () {",
            "            current.override = { tenor: t.tenor, name: 'RR25', value: ovVal.value };",
            "            // renderTenor dissolves this whole pane, so the audit line is",
            "            // rebuilt from scratch rather than patched in place.",
            "            body.appendChild(renderTenor(d, idx));",
            "        };",
            "        if (live) {",
            "            audit.appendChild(fdk.chip(b, 'audit-chip', 'warn', 'OVERRIDE live'));",
            "            audit.appendChild(fdk.el(b, 'audit-note', 'span', null,",
            "                '  RR25 \\u2192 ' + over.value + ' \\u00b7 expires 17:00 SGT'",
            "                + ' \\u00b7 flags 3 consumers  (demo \\u2014 not journaled)'));",
            "        }",
            "",
            "        pane.appendChild(fdk.sectionTitle(b, 'sect-facts', 'Surface facts'));",
            "        pane.appendChild(fdk.kv(b, 'kv-prov', 'quote provenance', d.provenance));",
            "        pane.appendChild(fdk.kv(b, 'kv-event', 'event vols', d.eventVols));",
            "        pane.appendChild(fdk.kv(b, 'kv-arb', 'arb gates', d.arbGates));",
            "        pane.appendChild(fdk.kv(b, 'kv-drift', 'anchor drift', d.anchorDrift));",
            "        return pane;",
            "    }",
            "",
            "    function render(d) {",
            "        current.data = d;",
            "        title.textContent = 'SURFACE MANAGER \\u2014 ' + d.pair;",
            "        if (stateBranch) stateBranch.dissolve();",
            "        stateBranch = branch.createBranch('state');",
            "        stateBranch.activate(root);",
            "        stateSlot.appendChild(fdk.stamp(stateBranch, 'stamp',",
            "            { slice: d.slice, surface: d.epoch, model: d.model }));",
            "        stateSlot.appendChild(d.stale",
            "            ? fdk.chip(stateBranch, 'state-chip', 'warn', 'stale \\u2014 ' + d.staleNote)",
            "            : fdk.chip(stateBranch, 'state-chip', 'live'));",
            "        var start = 0;",
            "        for (var i = 0; i < d.tenors.length; i++) { if (d.tenors[i].tenor === '3M') start = i; }",
            "        body.appendChild(renderTenor(d, start));",
            "    }",
            "",
            "    function load(pair) {",
            "        current.pair = pair; current.override = null;",
            "        fdk.load('/fx/surface?pair=' + encodeURIComponent(pair),",
            "            { branch: branch, host: body, what: 'surface' }, render);",
            "    }",
            "",
            "    function renderPairTabs(pairs) {",
            "        if (tabsBranch) tabsBranch.dissolve();",
            "        tabsBranch = branch.createBranch('tabs');",
            "        tabsBranch.activate(root);",
            "        for (var i = 0; i < pairs.length; i++) {",
            "            (function (p) {",
            "                tabs.appendChild(tabBtn(tabsBranch, 'pair-' + p.pair,",
            "                    p.pair + (p.stale ? ' \\u25b2' : ''), p.pair === current.pair,",
            "                    function () { load(p.pair); renderPairTabs(pairs); }));",
            "            })(pairs[i]);",
            "        }",
            "    }",
            "",
            "    fdk.load('/fx/surface', { branch: branch, host: body, what: 'surface pairs' }, function (list) {",
            "        renderPairTabs(list.pairs);",
            "    });",
            "    load('EURUSD');",
            "",
            "    var party = (workspaceCtx && workspaceCtx.deskParty) ? workspaceCtx.deskParty : null;",
            "    var actorId = null;",
            "    if (party) {",
            "        actorId = fdk.actorId('trader/surface', branch);",
            "        party.joinActor({",
            "            id: actorId,",
            "            parentSecretary: 'desk',",
            "            reactors: {",
            "                InstrumentChanged: function (msg) {",
            "                    var ins = msg.instrument || {};",
            "                    if (ins.pair && ins.pair !== current.pair) load(ins.pair);",
            "                }",
            "            }",
            "        });",
            "    }",
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
