package hue.captains.singapura.js.homing.findash.book;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * The Trade Blotter — the trade-journal anchor shared across workspaces
 * (middle office, trader, product control, audit): the journal rendered as a
 * blotter (study §10 — the journal IS the audit trail; the UI just renders
 * it). Every trade carries the slice it was priced on (P1), and amended
 * trades unfold their full journaled amendment history (who, what, P&amp;L
 * impact, four-eyes). Follows {@code InstrumentChanged} (filters by pair) and
 * drives — clicking a trade broadcasts its pair.
 */
public final class TradeBlotterWidget
        extends WorkspaceWidget<WorkspaceWidget._None, TradeBlotterWidget> {

    public static final TradeBlotterWidget INSTANCE = new TradeBlotterWidget();

    private TradeBlotterWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, TradeBlotterWidget> {}

    @Override protected _Construct<_None, TradeBlotterWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Trade Blotter"; }
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
            "        + 'font-family:system-ui,sans-serif;font-size:13px;color:#0b0b0b;';",
            "",
            "    var state = { all: [], filter: null, portfolio: null, open: {} };",
            "    function clear(n) { while (n.firstChild) n.removeChild(n.firstChild); }",
            "",
            "    var head = fdk.el('div', 'display:flex;align-items:center;gap:10px;flex-wrap:wrap;');",
            "    head.appendChild(fdk.el('span', 'font-weight:700;font-size:14px;letter-spacing:0.3px;',",
            "        'TRADE BLOTTER'));",
            "    head.appendChild(fdk.el('span', 'color:#52514e;font-size:11.5px;',",
            "        'the journal, rendered \\u00b7 amendments unfold'));",
            "    var filterSlot = fdk.el('span', 'display:inline-flex;gap:6px;align-items:center;');",
            "    head.appendChild(filterSlot);",
            "    root.appendChild(head);",
            "",
            "    var list = fdk.el('div', 'margin-top:8px;');",
            "    root.appendChild(list);",
            "",
            "    var party = (workspaceCtx && workspaceCtx.deskParty) ? workspaceCtx.deskParty : null;",
            "    var actorId = null;",
            "",
            "    function renderFilter() {",
            "        clear(filterSlot);",
            "        if (!state.filter && !state.portfolio) return;",
            "        if (state.portfolio) {",
            "            filterSlot.appendChild(fdk.chip('neutral', '\\ud83d\\udcc1 ' + state.portfolio.label));",
            "        }",
            "        if (state.filter) {",
            "            filterSlot.appendChild(fdk.chip('neutral', state.filter));",
            "        }",
            "        var x = document.createElement('button');",
            "        x.textContent = 'clear';",
            "        x.style.cssText = 'padding:1px 8px;font-size:10.5px;border:1px solid #c3c2b7;'",
            "            + 'border-radius:4px;background:#fcfcfb;color:#52514e;cursor:pointer;';",
            "        x.onclick = function () { state.filter = null; state.portfolio = null; render(); };",
            "        filterSlot.appendChild(x);",
            "    }",
            "",
            "    function tradeRow(t) {",
            "        var wrap = fdk.el('div', 'border-bottom:1px solid #e1e0d9;');",
            "        var line = fdk.el('div', 'display:flex;gap:10px;align-items:center;padding:7px 4px;'",
            "            + 'cursor:pointer;font-size:12.5px;');",
            "        var amended = t.amendments.length > 0;",
            "        line.appendChild(amended ? fdk.chip('warn', 'amended (' + t.amendments.length + ')')",
            "                                 : fdk.chip('good', 'booked'));",
            "        var main = fdk.el('div', 'flex:1;');",
            "        var top = fdk.el('div', '');",
            "        top.appendChild(fdk.el('span', 'color:#898781;font-variant-numeric:tabular-nums;'",
            "            + 'font-size:11px;', t.time + ' \\u00b7 ' + t.id + ' \\u00b7 ' + t.trader + '  '));",
            "        top.appendChild(fdk.el('span', 'font-weight:600;font-family:' + fdk.tokens.mono + ';'",
            "            + 'font-size:12px;', t.ticket));",
            "        main.appendChild(top);",
            "        var sub = fdk.el('div', 'display:flex;gap:10px;align-items:baseline;flex-wrap:wrap;');",
            "        sub.appendChild(fdk.el('span', 'color:#52514e;font-size:11.5px;',",
            "            t.notional + ' \\u00b7 PV at booking ' + t.pvAtBooking));",
            "        sub.appendChild(fdk.stamp({ slice: t.stamp }));",
            "        main.appendChild(sub);",
            "        line.appendChild(main);",
            "        if (amended) {",
            "            line.appendChild(fdk.el('span', 'color:#898781;font-size:11px;',",
            "                state.open[t.id] ? '\\u25be history' : '\\u25b8 history'));",
            "        }",
            "        line.onclick = function () {",
            "            if (amended) { state.open[t.id] = !state.open[t.id]; render(); }",
            "            if (party && actorId) {",
            "                party.tellFrom(actorId, { kind: 'InstrumentSelected',",
            "                    instrument: { pair: t.pair, ref: t.id } });",
            "            }",
            "        };",
            "        wrap.appendChild(line);",
            "        if (amended && state.open[t.id]) {",
            "            for (var a = 0; a < t.amendments.length; a++) {",
            "                var am = t.amendments[a];",
            "                var box = fdk.el('div', 'margin:0 4px 8px 34px;padding:7px 10px;'",
            "                    + 'background:#fdf3dc;border-radius:6px;font-size:11.5px;line-height:1.6;');",
            "                box.appendChild(fdk.el('div', 'font-weight:600;color:#9a6b1f;',",
            "                    am.when + ' \\u00b7 ' + am.who));",
            "                box.appendChild(fdk.el('div', 'color:#52514e;', am.what));",
            "                box.appendChild(fdk.el('div', 'color:#52514e;',",
            "                    am.pnlImpact + ' \\u00b7 ' + am.approval));",
            "                wrap.appendChild(box);",
            "            }",
            "        }",
            "        return wrap;",
            "    }",
            "",
            "    function render() {",
            "        renderFilter();",
            "        clear(list);",
            "        var shown = 0;",
            "        for (var i = 0; i < state.all.length; i++) {",
            "            var t = state.all[i];",
            "            if (state.portfolio && state.portfolio.leafIds.indexOf(t.portfolioId) < 0) continue;",
            "            if (state.filter && t.pair !== state.filter) continue;",
            "            shown++;",
            "            list.appendChild(tradeRow(t));",
            "        }",
            "        if (!shown) list.appendChild(fdk.el('div', 'color:#898781;font-size:12px;padding:6px 4px;',",
            "            'no trades for this filter'));",
            "    }",
            "",
            "    if (party) {",
            "        actorId = 'book/trades-' + Math.random().toString(36).slice(2, 8);",
            "        party.joinActor({",
            "            id: actorId,",
            "            parentSecretary: 'desk',",
            "            reactors: {",
            "                InstrumentChanged: function (msg) {",
            "                    var ins = msg.instrument || {};",
            "                    if (!ins.pair || ins.ref) return;   // ignore our own trade broadcasts",
            "                    state.filter = ins.pair;",
            "                    render();",
            "                },",
            "                PortfolioChanged: function (msg) {",
            "                    var pf = msg.portfolio || {};",
            "                    if (!pf.leafIds) return;",
            "                    state.portfolio = pf;",
            "                    render();",
            "                }",
            "            }",
            "        });",
            "        party.tellFrom(actorId, { kind: 'CurrentPortfolioRequested' });",
            "    }",
            "",
            "    fetch('/fx/trades')",
            "        .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
            "        .then(function (d) { state.all = d.trades; render(); })",
            "        .catch(function (e) {",
            "            list.appendChild(fdk.el('div', 'color:#a8502a;',",
            "                'trades load failed: ' + (e && e.message ? e.message : e)));",
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
