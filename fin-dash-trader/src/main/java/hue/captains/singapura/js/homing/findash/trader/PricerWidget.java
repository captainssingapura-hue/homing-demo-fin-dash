package hue.captains.singapura.js.homing.findash.trader;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;
import hue.captains.singapura.js.homing.findash.core.css.FdFrameCss;
import hue.captains.singapura.js.homing.findash.core.css.FdTextCss;
import hue.captains.singapura.js.homing.findash.core.css.FdControlCss;
import hue.captains.singapura.js.homing.findash.core.css.FdStatusCss;
import hue.captains.singapura.js.homing.findash.core.css.FdSurfaceCss;

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
                List.of(new FinDashKitModule.fdk()), FinDashKitModule.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdFrameCss.fd_grid_pairs(),
                        new FdFrameCss.fd_split_row(),
                        new FdFrameCss.fd_cluster(),
                        new FdFrameCss.fd_section(),
                        new FdFrameCss.fd_header_row(),
                        new FdFrameCss.fd_widget_root()),
                        FdFrameCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdTextCss.fd_mono(),
                        new FdTextCss.fd_dense(),
                        new FdTextCss.fd_display(),
                        new FdTextCss.fd_num(),
                        new FdTextCss.fd_caption(),
                        new FdTextCss.fd_muted(),
                        new FdTextCss.fd_strong(),
                        new FdTextCss.fd_title()),
                        FdTextCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdControlCss.fd_btn(),
                        new FdControlCss.fd_input()),
                        FdControlCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdStatusCss.fd_error_text(),
                        new FdStatusCss.fd_status_warn()),
                        FdStatusCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdSurfaceCss.fd_rule()),
                        FdSurfaceCss.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
            "    var root = branch.createElement('root', 'div');",
            "    css.setClass(root, fd_widget_root);",
            "",
            "    var head = fdk.el(branch, 'head-1', 'div', fd_header_row);",
            "    head.appendChild(fdk.el(branch, 'title-1', 'span', fd_title, 'PRICER'));",
            "    var stampSlot = fdk.el(branch, 'slot-1', 'span', null);",
            "    head.appendChild(stampSlot);",
            "    root.appendChild(head);",
            "",
            "    var input = fdk.el(branch, 'ticket', 'input', [fd_input, fd_mono]);",
            "    input.type = 'text';",
            "    input.value = 'eurusd 3m 1.0850 ko 1.1200 10';",
            "    root.appendChild(input);",
            "    root.appendChild(fdk.el(branch, 'cap-1', 'div', [fd_caption, fd_muted], ",
            "        'desk shorthand · Enter prices · \\u23ce books  \\u00b7  premium ccy / delta convention inherited from PairDef (Ring 2, read-only)'));",
            "",
            "    var out = fdk.el(branch, 'out', 'div', fd_section);",
            "    root.appendChild(out);",
            "    var status = fdk.el(branch, 'status', 'div', [fd_caption, fd_muted, fd_section]);",
            "    root.appendChild(status);",
            "",
            "    // render() re-runs on every Enter, so everything it builds lives in a",
            "    // sub-branch that is dissolved first — a branch element name may only",
            "    // be claimed once, and dissolve() detaches the old DOM as well.",
            "    var outBranch = null;",
            "",
            "    function greekRow(b, grid, key, label, value) {",
            "        grid.appendChild(fdk.el(b, 'gk-' + key, 'span', fd_muted, label));",
            "        grid.appendChild(fdk.el(b, 'gv-' + key, 'span', fd_num, value));",
            "    }",
            "",
            "    function render(d) {",
            "        if (outBranch) outBranch.dissolve();",
            "        outBranch = branch.createBranch('out');",
            "        outBranch.activate(root);",
            "        var b = outBranch;",
            "        if (d.error) {",
            "            out.appendChild(fdk.chip(b, 'chip', 'warn', 'cannot price'));",
            "            out.appendChild(fdk.el(b, 'err', 'div', fd_error_text, d.error));",
            "            return;",
            "        }",
            "        stampSlot.appendChild(fdk.stamp(b, 'stamp', { slice: d.slice, surface: d.epoch, model: d.model }));",
            "",
            "        var t = d.ticket;",
            "        var parsed = fdk.el(b, 'parsed', 'div', fd_section);",
            "        parsed.appendChild(fdk.kv(b, 'kv-type', 'Type', t.type));",
            "        parsed.appendChild(fdk.kv(b, 'kv-strike', 'Strike / Barrier', t.strike + (t.barrier ? ' / ' + t.barrier : ' / \\u2014')));",
            "        parsed.appendChild(fdk.kv(b, 'kv-tenor', 'Tenor', t.tenor + ' \\u00b7 NY 10am cut'));",
            "        parsed.appendChild(fdk.kv(b, 'kv-notional', 'Notional', 'EUR ' + t.notionalM + ',000,000'));",
            "        out.appendChild(parsed);",
            "",
            "        out.appendChild(fdk.sectionTitle(b, 'sect-price', 'Price'));",
            "        out.appendChild(fdk.el(b, 'pv', 'div', fd_display, 'EUR ' + fdk.fmt.amount(d.pvAmount)));",
            "        out.appendChild(fdk.el(b, 'pv-pct', 'div', fd_dense, ",
            "            fdk.fmt.num(d.pvPct, 4) + ' % EUR \\u00b7 vol used ' + d.vol));",
            "        var bo = fdk.el(b, 'bidoffer', 'div', [fd_cluster, fd_num]);",
            "        bo.appendChild(fdk.el(b, 'bid', 'span', fd_strong, 'bid ' + fdk.fmt.num(d.bid, 3)));",
            "        bo.appendChild(fdk.el(b, 'offer', 'span', fd_strong, 'offer ' + fdk.fmt.num(d.offer, 3)));",
            "        out.appendChild(bo);",
            "        out.appendChild(fdk.el(b, 'spread', 'div', [fd_caption, fd_muted], ",
            "            'mid ' + fdk.fmt.num(d.mid, 4) + ' \\u00b7 spread \\u00b1' + fdk.fmt.num(d.spread, 3)",
            "            + ' \\u00b7 inventory skew +' + fdk.fmt.num(d.skew, 3) + '  (decomposed \\u2014 P1)'));",
            "",
            "        out.appendChild(fdk.sectionTitle(b, 'sect-greeks', 'Greeks'));",
            "        var grid = fdk.el(b, 'greeks', 'div', [fd_grid_pairs, fd_dense]);",
            "        greekRow(b, grid, 'delta', '\\u0394', d.greeks.delta);",
            "        greekRow(b, grid, 'gamma', '\\u0393', d.greeks.gamma);",
            "        greekRow(b, grid, 'vega', 'Vega', d.greeks.vega);",
            "        greekRow(b, grid, 'vanna', 'Vanna', d.greeks.vanna);",
            "        greekRow(b, grid, 'volga', 'Volga', d.greeks.volga);",
            "        greekRow(b, grid, 'theta', '\\u0398', d.greeks.theta);",
            "        out.appendChild(grid);",
            "        out.appendChild(fdk.el(b, 'provenance', 'div', [fd_caption, fd_muted], ",
            "            'provenance: analytic (' + d.model + ') \\u00b7 ticks with market'));",
            "        if (d.barrierNote) {",
            "            out.appendChild(fdk.el(b, 'barrier-note', 'div', fd_caption, d.barrierNote));",
            "        }",
            "",
            "        out.appendChild(fdk.sectionTitle(b, 'sect-whatif', 'What-if (same contracts as risk \\u2014 P5)'));",
            "        var ladder = fdk.el(b, 'ladder', 'div', [fd_num, fd_dense]);",
            "        for (var i = 0; i < d.spotLadder.length; i++) {",
            "            var rung = d.spotLadder[i];",
            "            var row = fdk.el(b, 'rung-' + i, 'div', [fd_split_row, fd_rule]);",
            "            var s = rung.shift;",
            "            row.appendChild(fdk.el(b, 'rung-shift-' + i, 'span', fd_muted, ",
            "                'spot ' + (s === 0 ? '0' : (s > 0 ? '+' : '\\u2212') + Math.abs(s) + '%')));",
            "            var koFlag = d.ticket.barrier && i === d.spotLadder.length - 1;",
            "            var pv = fdk.el(b, 'rung-pv-' + i, 'span', null,",
            "                'PV ' + fdk.fmt.compact(rung.pv) + (koFlag ? ' \\u25b2KO' : ''));",
            "            // A knock-out rung is a state, not emphasis (P2).",
            "            if (koFlag) css.setClass(pv, fd_strong, fd_status_warn);",
            "            row.appendChild(pv);",
            "            ladder.appendChild(row);",
            "        }",
            "        out.appendChild(ladder);",
            "        out.appendChild(fdk.el(b, 'vol-shift', 'div', fd_caption, ",
            "            'vol +1.0 \\u2192 ' + fdk.fmt.compact(d.volUp) + '  \\u00b7  vol \\u22121.0 \\u2192 ' + fdk.fmt.compact(d.volDown)));",
            "",
            "        var book = fdk.el(b, 'book', 'button', fd_btn, 'Book trade \\u23ce');",
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
            "                    // Prefill only. This used to take DOM focus as well, so that",
            "                    // picking an instrument left you ready to type — but the",
            "                    // selection usually comes from ANOTHER pane, and pulling focus",
            "                    // out of it broke the pane you were driving: one arrow key in",
            "                    // the blotter moved the selection, the pricer took the caret,",
            "                    // and every arrow after that went into this text box. It also",
            "                    // left the entered-pane ring pointing at a pane that no longer",
            "                    // held the keyboard (RFC 0048). Focus follows the user's own",
            "                    // gesture now: entering this pane focuses the input.",
            "                    input.value = String(ins.pair).toLowerCase() + ' ' + (ins.tenor || '3m').toLowerCase() + ' ';",
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
