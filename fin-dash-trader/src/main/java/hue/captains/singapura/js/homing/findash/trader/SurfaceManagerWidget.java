package hue.captains.singapura.js.homing.findash.trader;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;
import hue.captains.singapura.js.homing.findash.core.kit.SmileChartModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

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
                new ModuleImports<>(List.of(new SmileChartModule.smileChart()), SmileChartModule.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
            "    var root = branch.createElement('root', 'div');",
            "    root.style.cssText = 'height:100%;overflow:auto;box-sizing:border-box;padding:14px;'",
            "        + 'font-family:system-ui,sans-serif;font-size:13px;color:#0b0b0b;';",
            "",
            "    var head = fdk.el('div', 'display:flex;align-items:baseline;gap:10px;flex-wrap:wrap;');",
            "    var title = fdk.el('span', 'font-weight:700;font-size:14px;letter-spacing:0.3px;', 'SURFACE MANAGER');",
            "    head.appendChild(title);",
            "    var stateSlot = fdk.el('span', 'display:inline-flex;gap:8px;align-items:baseline;');",
            "    head.appendChild(stateSlot);",
            "    root.appendChild(head);",
            "    var tabs = fdk.el('div', 'display:flex;gap:6px;margin:8px 0;flex-wrap:wrap;');",
            "    root.appendChild(tabs);",
            "    var body = fdk.el('div', '');",
            "    root.appendChild(body);",
            "",
            "    var current = { pair: 'EURUSD', tenorIdx: -1, data: null, override: null };",
            "    function clear(n) { while (n.firstChild) n.removeChild(n.firstChild); }",
            "",
            "    function tabBtn(label, selected, onClick) {",
            "        var b = document.createElement('button');",
            "        b.textContent = label;",
            "        b.style.cssText = 'padding:3px 10px;font-size:11.5px;border-radius:999px;cursor:pointer;'",
            "            + 'border:1px solid ' + (selected ? '#2a78d6' : '#c3c2b7') + ';'",
            "            + (selected ? 'background:#cde2fb;color:#0b0b0b;font-weight:600;' : 'background:#fcfcfb;color:#52514e;');",
            "        b.onclick = onClick;",
            "        return b;",
            "    }",
            "",
            "    function renderTenor(d, idx) {",
            "        current.tenorIdx = idx;",
            "        var t = d.tenors[idx];",
            "        var pane = fdk.el('div', '');",
            "",
            "        var strip = fdk.el('div', 'display:flex;gap:6px;flex-wrap:wrap;margin-bottom:8px;');",
            "        for (var i = 0; i < d.tenors.length; i++) {",
            "            (function (i2) {",
            "                var tn = d.tenors[i2];",
            "                strip.appendChild(tabBtn((tn.flagged ? '\\u25b2 ' : '') + tn.tenor, i2 === idx,",
            "                    function () { clear(body); body.appendChild(renderTenor(d, i2)); }));",
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
            "        var residLine = fdk.el('div', 'display:flex;gap:8px;align-items:center;margin:6px 0;');",
            "        residLine.appendChild(fdk.chip(residBad ? 'warn' : 'good',",
            "            'fit resid ' + t.resid.toFixed(2) + (residBad ? ' > tol ' : ' \\u2264 tol ') + t.tol.toFixed(2)));",
            "        if (t.flagged) residLine.appendChild(fdk.chip('warn', t.flagNote));",
            "        pane.appendChild(residLine);",
            "",
            "        pane.appendChild(fdk.sectionTitle(t.tenor + ' pillars'));",
            "        var tbl = fdk.el('div', 'display:grid;grid-template-columns:60px 70px 70px 70px;'",
            "            + 'gap:2px 10px;font-size:12px;font-variant-numeric:tabular-nums;max-width:320px;');",
            "        ['', 'Mkt', 'Fit', 'Ovr'].forEach(function (h) {",
            "            tbl.appendChild(fdk.el('span', 'color:#898781;font-size:10.5px;font-weight:600;', h));",
            "        });",
            "        for (var p = 0; p < t.pillars.length; p++) {",
            "            var pr = t.pillars[p];",
            "            var isOv = over && over.tenor === t.tenor && over.name === pr.name;",
            "            tbl.appendChild(fdk.el('span', 'color:#52514e;', pr.name));",
            "            tbl.appendChild(fdk.el('span', '', pr.mkt.toFixed(2)));",
            "            tbl.appendChild(fdk.el('span', '', pr.fit.toFixed(2)));",
            "            tbl.appendChild(isOv",
            "                ? fdk.el('span', 'color:#9a6b1f;font-weight:600;', '\\u25b2 ' + over.value)",
            "                : fdk.el('span', 'color:#898781;', '\\u2014'));",
            "        }",
            "        pane.appendChild(tbl);",
            "",
            "        pane.appendChild(fdk.sectionTitle('Override \\u2014 Ring 3 \\u00b7 audited \\u00b7 expiring'));",
            "        var ov = fdk.el('div', 'display:flex;gap:6px;align-items:center;flex-wrap:wrap;font-size:12px;');",
            "        ov.appendChild(fdk.el('span', 'color:#52514e;', 'RR25 \\u2192'));",
            "        var ovVal = document.createElement('input');",
            "        ovVal.type = 'text'; ovVal.value = '\\u22121.10'; ovVal.style.cssText =",
            "            'width:64px;padding:3px 6px;font-family:' + fdk.tokens.mono + ';font-size:12px;'",
            "            + 'border:1px solid #c3c2b7;border-radius:4px;';",
            "        ov.appendChild(ovVal);",
            "        var reason = document.createElement('select');",
            "        ['stale broker run', 'event repricing', 'client axe', 'fat quote filtered'].forEach(function (r) {",
            "            var o = document.createElement('option'); o.textContent = r; reason.appendChild(o);",
            "        });",
            "        reason.style.cssText = 'padding:3px 6px;font-size:12px;border:1px solid #c3c2b7;border-radius:4px;';",
            "        ov.appendChild(reason);",
            "        var apply = document.createElement('button');",
            "        apply.textContent = 'Apply \\u23ce';",
            "        apply.style.cssText = 'padding:4px 12px;font-size:12px;font-weight:600;color:#fcfcfb;'",
            "            + 'background:#2a78d6;border:none;border-radius:5px;cursor:pointer;';",
            "        ov.appendChild(apply);",
            "        pane.appendChild(ov);",
            "        var audit = fdk.el('div', 'margin-top:5px;font-size:11px;color:#898781;',",
            "            'expires 17:00 SGT \\u00b7 flags 3 consumers \\u00b7 audit record shown on commit');",
            "        pane.appendChild(audit);",
            "        apply.onclick = function () {",
            "            current.override = { tenor: t.tenor, name: 'RR25', value: ovVal.value };",
            "            clear(audit);",
            "            audit.appendChild(fdk.chip('warn', 'OVERRIDE live'));",
            "            audit.appendChild(fdk.el('span', 'margin-left:8px;',",
            "                'RR25 \\u2192 ' + ovVal.value + ' \\u00b7 reason: ' + reason.value",
            "                + ' \\u00b7 expires 17:00 SGT \\u00b7 flags 3 consumers  (demo \\u2014 not journaled)'));",
            "            clear(body); body.appendChild(renderTenor(d, idx));",
            "        };",
            "",
            "        pane.appendChild(fdk.sectionTitle('Surface facts'));",
            "        pane.appendChild(fdk.kv('quote provenance', d.provenance));",
            "        pane.appendChild(fdk.kv('event vols', d.eventVols));",
            "        pane.appendChild(fdk.kv('arb gates', d.arbGates));",
            "        pane.appendChild(fdk.kv('anchor drift', d.anchorDrift));",
            "        return pane;",
            "    }",
            "",
            "    function render(d) {",
            "        current.data = d;",
            "        title.textContent = 'SURFACE MANAGER \\u2014 ' + d.pair;",
            "        clear(stateSlot);",
            "        stateSlot.appendChild(fdk.stamp({ slice: d.slice, surface: d.epoch, model: d.model }));",
            "        stateSlot.appendChild(d.stale ? fdk.chip('warn', 'stale \\u2014 ' + d.staleNote) : fdk.chip('live'));",
            "        var start = 0;",
            "        for (var i = 0; i < d.tenors.length; i++) { if (d.tenors[i].tenor === '3M') start = i; }",
            "        clear(body); body.appendChild(renderTenor(d, start));",
            "    }",
            "",
            "    function load(pair) {",
            "        current.pair = pair; current.override = null;",
            "        fetch('/fx/surface?pair=' + encodeURIComponent(pair))",
            "            .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
            "            .then(render)",
            "            .catch(function (e) {",
            "                clear(body);",
            "                body.appendChild(fdk.el('div', 'color:#a8502a;', 'surface load failed: ' + (e && e.message ? e.message : e)));",
            "            });",
            "    }",
            "",
            "    fetch('/fx/surface').then(function (r) { return r.json(); }).then(function (list) {",
            "        clear(tabs);",
            "        for (var i = 0; i < list.pairs.length; i++) {",
            "            (function (p) {",
            "                tabs.appendChild(tabBtn(p.pair + (p.stale ? ' \\u25b2' : ''), p.pair === current.pair,",
            "                    function () {",
            "                        load(p.pair);",
            "                        var kids = tabs.childNodes;",
            "                        for (var k = 0; k < kids.length; k++) kids[k].style.fontWeight = '400';",
            "                    }));",
            "            })(list.pairs[i]);",
            "        }",
            "    });",
            "    load('EURUSD');",
            "",
            "    var party = (workspaceCtx && workspaceCtx.deskParty) ? workspaceCtx.deskParty : null;",
            "    var actorId = null;",
            "    if (party) {",
            "        actorId = 'trader/surface-' + Math.random().toString(36).slice(2, 8);",
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
            "            if (actorId && party) { try { party.leave(actorId); } catch (e) {} }",
            "        }",
            "    };");
    }
}
