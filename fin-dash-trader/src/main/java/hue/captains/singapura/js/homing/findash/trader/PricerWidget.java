package hue.captains.singapura.js.homing.findash.trader;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * W1 — the trader pricer / deal ticket. Desk shorthand in ({@code eurusd 3m
 * 1.0850 ko 1.1200 10}, keyboard-first: Enter prices, no mouse needed),
 * decomposed bid/offer out (mid · spread · skew — never an unexplained
 * number), Greeks with provenance, what-if ladder computed by the same server
 * contracts as risk (P5), model badge + slice stamp always visible (P1, P3).
 * "Book" journals with the priced slice — demo: shown, not persisted.
 *
 * <p>Joins the desk party: an {@code InstrumentChanged} (e.g. blotter row
 * click) prefills the ticket with that pair/tenor for completion.</p>
 */
public final class PricerWidget extends WorkspaceWidget<WorkspaceWidget._None, PricerWidget> {

    public static final PricerWidget INSTANCE = new PricerWidget();

    private PricerWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, PricerWidget> {}

    @Override protected _Construct<_None, PricerWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Pricer"; }
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
            "    head.appendChild(fdk.el('span', 'font-weight:700;font-size:14px;letter-spacing:0.3px;', 'PRICER'));",
            "    var stampSlot = fdk.el('span', '');",
            "    head.appendChild(stampSlot);",
            "    root.appendChild(head);",
            "",
            "    var input = document.createElement('input');",
            "    input.type = 'text';",
            "    input.value = 'eurusd 3m 1.0850 ko 1.1200 10';",
            "    input.style.cssText = 'width:100%;box-sizing:border-box;margin:10px 0 4px;padding:7px 10px;'",
            "        + 'font-family:' + fdk.tokens.mono + ';font-size:13px;border:1px solid var(--color-border);'",
            "        + 'border-radius:6px;background:var(--color-surface);color:var(--color-text-primary);outline-color:#2a78d6;';",
            "    root.appendChild(input);",
            "    root.appendChild(fdk.el('div', 'color:var(--color-text-muted);font-size:11px;',",
            "        'desk shorthand · Enter prices · \\u23ce books  \\u00b7  premium ccy / delta convention inherited from PairDef (Ring 2, read-only)'));",
            "",
            "    var out = fdk.el('div', 'margin-top:10px;');",
            "    root.appendChild(out);",
            "    var status = fdk.el('div', 'margin-top:8px;font-size:11px;color:var(--color-text-muted);');",
            "    root.appendChild(status);",
            "",
            "    function clear(node) { while (node.firstChild) node.removeChild(node.firstChild); }",
            "",
            "    function greekRow(grid, label, value) {",
            "        grid.appendChild(fdk.el('span', 'color:var(--color-text-muted);', label));",
            "        grid.appendChild(fdk.el('span', 'font-variant-numeric:tabular-nums;', value));",
            "    }",
            "",
            "    function render(d) {",
            "        clear(out); clear(stampSlot);",
            "        if (d.error) {",
            "            out.appendChild(fdk.chip('warn', 'cannot price'));",
            "            out.appendChild(fdk.el('div', 'margin-top:6px;color:#a8502a;font-size:12px;', d.error));",
            "            return;",
            "        }",
            "        stampSlot.appendChild(fdk.stamp({ slice: d.slice, surface: d.epoch, model: d.model }));",
            "",
            "        var t = d.ticket;",
            "        var parsed = fdk.el('div', 'margin:8px 0;');",
            "        parsed.appendChild(fdk.kv('Type', t.type));",
            "        parsed.appendChild(fdk.kv('Strike / Barrier', t.strike + (t.barrier ? ' / ' + t.barrier : ' / \\u2014')));",
            "        parsed.appendChild(fdk.kv('Tenor', t.tenor + ' \\u00b7 NY 10am cut'));",
            "        parsed.appendChild(fdk.kv('Notional', 'EUR ' + t.notionalM + ',000,000'));",
            "        out.appendChild(parsed);",
            "",
            "        out.appendChild(fdk.sectionTitle('Price'));",
            "        out.appendChild(fdk.el('div', 'font-size:22px;font-weight:700;',",
            "            'EUR ' + fdk.fmt.amount(d.pvAmount)));",
            "        out.appendChild(fdk.el('div', 'color:var(--color-text-primary);font-size:12px;',",
            "            fdk.fmt.num(d.pvPct, 4) + ' % EUR \\u00b7 vol used ' + d.vol));",
            "        var bo = fdk.el('div', 'display:flex;gap:14px;margin-top:4px;font-variant-numeric:tabular-nums;');",
            "        bo.appendChild(fdk.el('span', 'font-weight:600;', 'bid ' + fdk.fmt.num(d.bid, 3)));",
            "        bo.appendChild(fdk.el('span', 'font-weight:600;', 'offer ' + fdk.fmt.num(d.offer, 3)));",
            "        out.appendChild(bo);",
            "        out.appendChild(fdk.el('div', 'color:var(--color-text-muted);font-size:11px;',",
            "            'mid ' + fdk.fmt.num(d.mid, 4) + ' \\u00b7 spread \\u00b1' + fdk.fmt.num(d.spread, 3)",
            "            + ' \\u00b7 inventory skew +' + fdk.fmt.num(d.skew, 3) + '  (decomposed \\u2014 P1)'));",
            "",
            "        out.appendChild(fdk.sectionTitle('Greeks'));",
            "        var grid = fdk.el('div', 'display:grid;grid-template-columns:auto auto auto auto;'",
            "            + 'gap:2px 18px;font-size:12.5px;max-width:420px;');",
            "        greekRow(grid, '\\u0394', d.greeks.delta);  greekRow(grid, '\\u0393', d.greeks.gamma);",
            "        greekRow(grid, 'Vega', d.greeks.vega);      greekRow(grid, 'Vanna', d.greeks.vanna);",
            "        greekRow(grid, 'Volga', d.greeks.volga);    greekRow(grid, '\\u0398', d.greeks.theta);",
            "        out.appendChild(grid);",
            "        out.appendChild(fdk.el('div', 'color:var(--color-text-muted);font-size:11px;margin-top:2px;',",
            "            'provenance: analytic (' + d.model + ') \\u00b7 ticks with market'));",
            "        if (d.barrierNote) {",
            "            out.appendChild(fdk.el('div', 'color:var(--color-text-primary);font-size:11.5px;margin-top:4px;', d.barrierNote));",
            "        }",
            "",
            "        out.appendChild(fdk.sectionTitle('What-if (same contracts as risk \\u2014 P5)'));",
            "        var ladder = fdk.el('div', 'font-variant-numeric:tabular-nums;font-size:12px;max-width:280px;');",
            "        for (var i = 0; i < d.spotLadder.length; i++) {",
            "            var rung = d.spotLadder[i];",
            "            var row = fdk.el('div', 'display:flex;justify-content:space-between;'",
            "                + 'padding:1px 0;border-bottom:1px solid var(--color-border);');",
            "            var s = rung.shift;",
            "            row.appendChild(fdk.el('span', 'color:var(--color-text-muted);',",
            "                'spot ' + (s === 0 ? '0' : (s > 0 ? '+' : '\\u2212') + Math.abs(s) + '%')));",
            "            var koFlag = d.ticket.barrier && i === d.spotLadder.length - 1;",
            "            row.appendChild(fdk.el('span', koFlag ? 'color:#9a6b1f;font-weight:600;' : '',",
            "                'PV ' + fdk.fmt.compact(rung.pv) + (koFlag ? ' \\u25b2KO' : '')));",
            "            ladder.appendChild(row);",
            "        }",
            "        out.appendChild(ladder);",
            "        out.appendChild(fdk.el('div', 'color:var(--color-text-primary);font-size:11.5px;margin-top:4px;',",
            "            'vol +1.0 \\u2192 ' + fdk.fmt.compact(d.volUp) + '  \\u00b7  vol \\u22121.0 \\u2192 ' + fdk.fmt.compact(d.volDown)));",
            "",
            "        var book = document.createElement('button');",
            "        book.textContent = 'Book trade \\u23ce';",
            "        book.style.cssText = 'margin-top:12px;padding:6px 14px;font-size:12.5px;font-weight:600;'",
            "            + 'color:var(--color-surface);background:#2a78d6;border:none;border-radius:6px;cursor:pointer;';",
            "        book.onclick = function () {",
            "            status.textContent = 'journaled with priced slice ' + d.slice + ' \\u00b7 ' + d.epoch",
            "                + ' \\u00b7 risk + P&L explain will agree with this screen  (demo \\u2014 not persisted)';",
            "        };",
            "        out.appendChild(book);",
            "    }",
            "",
            "    function priceNow() {",
            "        status.textContent = 'pricing\\u2026';",
            "        fetch('/fx/price?ticket=' + encodeURIComponent(input.value))",
            "            .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
            "            .then(function (d) {",
            "                render(d);",
            "                status.textContent = d.error ? 'fix the ticket and press Enter'",
            "                    : 'priced \\u00b7 Enter re-prices';",
            "            })",
            "            .catch(function (e) { status.textContent = 'price failed: ' + (e && e.message ? e.message : e); });",
            "    }",
            "    input.addEventListener('keydown', function (ev) { if (ev.key === 'Enter') priceNow(); });",
            "    priceNow();",
            "",
            "    var party = (workspaceCtx && workspaceCtx.deskParty) ? workspaceCtx.deskParty : null;",
            "    var actorId = null;",
            "    if (party) {",
            "        actorId = 'trader/pricer-' + Math.random().toString(36).slice(2, 8);",
            "        party.joinActor({",
            "            id: actorId,",
            "            parentSecretary: 'desk',",
            "            reactors: {",
            "                InstrumentChanged: function (msg) {",
            "                    var ins = msg.instrument || {};",
            "                    if (!ins.pair) return;",
            "                    input.value = String(ins.pair).toLowerCase() + ' ' + (ins.tenor || '3m').toLowerCase() + ' ';",
            "                    input.focus();",
            "                }",
            "            }",
            "        });",
            "        party.tellFrom(actorId, { kind: 'CurrentInstrumentRequested' });",
            "    }",
            "",
            "    return {",
            "        root: root,",
            "        setActive: function (active) { if (active) input.focus(); },",
            "        partyDeregister: function () {",
            "            if (actorId && party) { try { party.leave(actorId); } catch (e) {} }",
            "        }",
            "    };");
    }
}
