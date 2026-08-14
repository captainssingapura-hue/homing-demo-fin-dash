package hue.captains.singapura.js.homing.findash.sales;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;
import hue.captains.singapura.js.homing.findash.core.css.FdFrameCss;
import hue.captains.singapura.js.homing.findash.core.css.FdStatusCss;
import hue.captains.singapura.js.homing.findash.core.css.FdTextCss;

import java.util.List;

/**
 * The sales client pricer (study §5): read-only against the pricing core —
 * sales sees prices, never mark inputs. Sales margin sits <b>on top of</b> the
 * desk price, visible to the desk and product control, never blended
 * invisibly into "the price". Indicative vs firm is explicit on every
 * displayed price; a firm price carries an expiry countdown. The RFQ chain
 * (request → desk response → client presentation) is journaled (demo).
 */
public final class ClientPricerWidget
        extends WorkspaceWidget<WorkspaceWidget._None, ClientPricerWidget> {

    public static final ClientPricerWidget INSTANCE = new ClientPricerWidget();

    private ClientPricerWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, ClientPricerWidget> {}

    @Override protected _Construct<_None, ClientPricerWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Client Pricer"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(new ModuleImports<>(
                List.of(new FinDashKitModule.fdk()), FinDashKitModule.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdFrameCss.fd_section(),
                        new FdFrameCss.fd_widget_root()),
                        FdFrameCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdStatusCss.fd_error_text()),
                        FdStatusCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdTextCss.fd_caption(),
                        new FdTextCss.fd_muted(),
                        new FdTextCss.fd_title()),
                        FdTextCss.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
            "    var root = branch.createElement('root', 'div');",
            "    css.setClass(root, fd_widget_root);",
            "",
            "    root.appendChild(fdk.el(branch, 'title-1', 'div', fd_title, ",
            "        'CLIENT PRICER'));",
            "    root.appendChild(fdk.el(branch, 'cap-1', 'div', [fd_caption, fd_section], ",
            "        'read-only projection of the pricing core \\u2014 sales sees prices, never mark inputs'));",
            "",
            "    var input = document.createElement('input');",
            "    input.type = 'text';",
            "    input.value = 'eurusd 3m 1.0850 ko 1.1200 10';",
            "    input.style.cssText = 'width:100%;box-sizing:border-box;margin:2px 0 8px;padding:7px 10px;'",
            "        + 'font-family:' + fdk.tokens.mono + ';font-size:13px;border:1px solid var(--color-border);'",
            "        + 'border-radius:6px;background:var(--color-surface);';",
            "    root.appendChild(input);",
            "",
            "    var out = fdk.el(branch, 'slot-1', 'div', null);",
            "    root.appendChild(out);",
            "",
            "    var state = { firmLeft: 0, timer: null, data: null, margin: 8 };",
            "    function clear(n) { while (n.firstChild) n.removeChild(n.firstChild); }",
            "",
            "    function render() {",
            "        clear(out);",
            "        var d = state.data;",
            "        if (!d) return;",
            "        if (d.error) { out.appendChild(fdk.chip(branch, 'chip-1', 'warn', d.error)); return; }",
            "        out.appendChild(fdk.kv(branch, 'kv-1', 'Instrument', d.ticket.pair + ' ' + d.ticket.tenor + ' ' + d.ticket.type));",
            "        out.appendChild(fdk.kv(branch, 'kv-2', 'Desk price', 'EUR ' + fdk.fmt.amount(d.pvAmount)",
            "            + '  (' + fdk.fmt.num(d.pvPct, 4) + ' %)'));",
            "",
            "        var mRow = fdk.el('div', 'display:flex;gap:8px;align-items:center;margin:6px 0;font-size:12.5px;');",
            "        mRow.appendChild(fdk.el('span', 'color:var(--color-text-muted);min-width:110px;', 'Sales margin'));",
            "        var m = document.createElement('input');",
            "        m.type = 'text'; m.value = String(state.margin);",
            "        m.style.cssText = 'width:52px;padding:3px 6px;font-family:' + fdk.tokens.mono",
            "            + ';font-size:12px;border:1px solid var(--color-border);border-radius:4px;';",
            "        m.addEventListener('input', function () {",
            "            var v = parseFloat(m.value); if (!isNaN(v)) { state.margin = v; renderClient(); }",
            "        });",
            "        mRow.appendChild(m);",
            "        mRow.appendChild(fdk.el(branch, 'cap-2', 'span', [fd_caption, fd_muted], ",
            "            'bp \\u00b7 visible to desk + product control \\u2014 never blended into \"the price\"'));",
            "        out.appendChild(mRow);",
            "",
            "        var clientSlot = fdk.el(branch, 'slot-2', 'div', null);",
            "        out.appendChild(clientSlot);",
            "        state.clientSlot = clientSlot;",
            "        renderClient();",
            "",
            "        var btns = fdk.el('div', 'margin-top:10px;display:flex;gap:8px;align-items:center;');",
            "        var firm = document.createElement('button');",
            "        firm.textContent = 'Quote FIRM (30s)';",
            "        firm.style.cssText = 'padding:5px 12px;font-size:12px;font-weight:600;color:var(--color-surface);'",
            "            + 'background:#2a78d6;border:none;border-radius:6px;cursor:pointer;';",
            "        firm.onclick = function () { state.firmLeft = 30; tick(); };",
            "        btns.appendChild(firm);",
            "        state.firmSlot = fdk.el(branch, 'slot-3', 'span', null);",
            "        btns.appendChild(state.firmSlot);",
            "        out.appendChild(btns);",
            "        out.appendChild(fdk.el(branch, 'cap-3', 'div', [fd_caption, fd_muted, fd_section], ",
            "            'RFQ chain journaled: request \\u2192 desk response \\u2192 client presentation (demo)'));",
            "        renderFirm();",
            "    }",
            "",
            "    function renderClient() {",
            "        if (!state.clientSlot) return;",
            "        clear(state.clientSlot);",
            "        var d = state.data;",
            "        var clientPv = d.pvAmount * (1 + state.margin / 10000);",
            "        state.clientSlot.appendChild(fdk.el('div', 'font-size:20px;font-weight:700;margin-top:4px;', ",
            "            'EUR ' + fdk.fmt.amount(clientPv)));",
            "        state.clientSlot.appendChild(fdk.el(branch, 'cap-4', 'div', fd_caption, ",
            "            'client all-in = desk ' + fdk.fmt.amount(d.pvAmount) + ' + margin '",
            "            + fdk.fmt.amount(clientPv - d.pvAmount) + '  (decomposed \\u2014 P1)'));",
            "    }",
            "",
            "    function renderFirm() {",
            "        if (!state.firmSlot) return;",
            "        clear(state.firmSlot);",
            "        state.firmSlot.appendChild(state.firmLeft > 0",
            "            ? fdk.chip(branch, 'chip-2', 'good', 'FIRM \\u00b7 expires in ' + state.firmLeft + 's')",
            "            : fdk.chip(branch, 'chip-3', 'neutral', 'INDICATIVE'));",
            "    }",
            "",
            "    function tick() {",
            "        if (state.timer) clearTimeout(state.timer);",
            "        renderFirm();",
            "        if (state.firmLeft > 0) {",
            "            state.timer = setTimeout(function () { state.firmLeft--; tick(); }, 1000);",
            "        }",
            "    }",
            "",
            "    function priceNow() {",
            "        fetch('/fx/price?ticket=' + encodeURIComponent(input.value))",
            "            .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
            "            .then(function (d) { state.data = d; state.firmLeft = 0; render(); })",
            "            .catch(function (e) {",
            "                clear(out);",
            "                out.appendChild(fdk.el(branch, 'err-1', 'div', fd_error_text, 'price failed: ' + (e && e.message ? e.message : e)));",
            "            });",
            "    }",
            "    input.addEventListener('keydown', function (ev) { if (ev.key === 'Enter') priceNow(); });",
            "    priceNow();",
            "",
            "    return {",
            "        root: root,",
            "        setActive: function (active) { if (active) input.focus(); },",
            "        partyDeregister: function () { if (state.timer) clearTimeout(state.timer); }",
            "    };");
    }
}
