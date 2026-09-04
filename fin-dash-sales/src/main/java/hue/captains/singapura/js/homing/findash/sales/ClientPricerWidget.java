package hue.captains.singapura.js.homing.findash.sales;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;
import hue.captains.singapura.js.homing.findash.core.css.FdFrameCss;
import hue.captains.singapura.js.homing.findash.core.css.FdStatusCss;
import hue.captains.singapura.js.homing.findash.core.css.FdTextCss;
import hue.captains.singapura.js.homing.findash.core.css.FdControlCss;

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
                        new FdFrameCss.fd_cluster(),
                        new FdFrameCss.fd_section(),
                        new FdFrameCss.fd_widget_root()),
                        FdFrameCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdStatusCss.fd_error_text()),
                        FdStatusCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdTextCss.fd_dense(),
                        new FdTextCss.fd_display(),
                        new FdTextCss.fd_mono(),
                        new FdTextCss.fd_caption(),
                        new FdTextCss.fd_muted(),
                        new FdTextCss.fd_title()),
                        FdTextCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdControlCss.fd_btn(),
                        new FdControlCss.fd_input()),
                        FdControlCss.INSTANCE));
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
            "    var input = fdk.el(branch, 'input', 'input', [fd_input, fd_mono]);",
            "    input.type = 'text';",
            "    input.value = 'eurusd 3m 1.0850 ko 1.1200 10';",
            "    root.appendChild(input);",
            "",
            "    var out = fdk.el(branch, 'slot-1', 'div', null);",
            "    root.appendChild(out);",
            "",
            "    var state = { firmLeft: 0, timer: null, data: null, margin: 8 };",
            "",
            "    // Three things re-render on their own cadence: the whole quote (each",
            "    // price), the client all-in (each margin keystroke) and the firm chip",
            "    // (each second of the countdown). Each owns a dissolvable sub-branch —",
            "    // dissolve releases both the elements and their names, so the next",
            "    // render can reuse them. Tearing children out by hand would leave the",
            "    // names taken and throw on the second pass.",
            "    var outBranch = null, clientBranch = null, firmBranch = null;",
            "",
            "    function render() {",
            "        if (outBranch) { outBranch.dissolve(); clientBranch = null; firmBranch = null; }",
            "        outBranch = branch.createBranch('quote');",
            "        outBranch.activate(out);",
            "        var b = outBranch;",
            "        var d = state.data;",
            "        if (!d) return;",
            "        if (d.error) { out.appendChild(fdk.chip(b, 'chip-1', 'warn', d.error)); return; }",
            "        out.appendChild(fdk.kv(b, 'kv-1', 'Instrument', d.ticket.pair + ' ' + d.ticket.tenor + ' ' + d.ticket.type));",
            "        out.appendChild(fdk.kv(b, 'kv-2', 'Desk price', 'EUR ' + fdk.fmt.amount(d.pvAmount)",
            "            + '  (' + fdk.fmt.num(d.pvPct, 4) + ' %)'));",
            "",
            "        var mRow = fdk.el(b, 'cluster-1', 'div', [fd_dense, fd_cluster, fd_section]);",
            "        mRow.appendChild(fdk.el(b, 'el-1', 'span', fd_muted, 'Sales margin'));",
            "        var m = fdk.el(b, 'm', 'input', [fd_input, fd_mono]);",
            "        m.type = 'text'; m.value = String(state.margin);",
            "        m.addEventListener('input', function () {",
            "            var v = parseFloat(m.value); if (!isNaN(v)) { state.margin = v; renderClient(); }",
            "        });",
            "        mRow.appendChild(m);",
            "        mRow.appendChild(fdk.el(b, 'cap-2', 'span', [fd_caption, fd_muted], ",
            "            'bp \\u00b7 visible to desk + product control \\u2014 never blended into \"the price\"'));",
            "        out.appendChild(mRow);",
            "",
            "        var clientSlot = fdk.el(b, 'slot-2', 'div', null);",
            "        out.appendChild(clientSlot);",
            "        state.clientSlot = clientSlot;",
            "        renderClient();",
            "",
            "        var btns = fdk.el(b, 'cluster-2', 'div', [fd_cluster, fd_section]);",
            "        var firm = fdk.el(b, 'firm', 'button', fd_btn);",
            "        firm.textContent = 'Quote FIRM (30s)';",
            "        firm.onclick = function () { state.firmLeft = 30; tick(); };",
            "        btns.appendChild(firm);",
            "        state.firmSlot = fdk.el(b, 'slot-3', 'span', null);",
            "        btns.appendChild(state.firmSlot);",
            "        out.appendChild(btns);",
            "        out.appendChild(fdk.el(b, 'cap-3', 'div', [fd_caption, fd_muted, fd_section], ",
            "            'RFQ chain journaled: request \\u2192 desk response \\u2192 client presentation (demo)'));",
            "        renderFirm();",
            "    }",
            "",
            "    function renderClient() {",
            "        if (!state.clientSlot || !outBranch) return;",
            "        if (clientBranch) clientBranch.dissolve();",
            "        clientBranch = outBranch.createBranch('client');",
            "        clientBranch.activate(state.clientSlot);",
            "        var b = clientBranch;",
            "        var d = state.data;",
            "        var clientPv = d.pvAmount * (1 + state.margin / 10000);",
            "        state.clientSlot.appendChild(fdk.el(b, 'display-1', 'div', [fd_display, fd_section], ",
            "            'EUR ' + fdk.fmt.amount(clientPv)));",
            "        state.clientSlot.appendChild(fdk.el(b, 'cap-4', 'div', fd_caption, ",
            "            'client all-in = desk ' + fdk.fmt.amount(d.pvAmount) + ' + margin '",
            "            + fdk.fmt.amount(clientPv - d.pvAmount) + '  (decomposed \\u2014 P1)'));",
            "    }",
            "",
            "    function renderFirm() {",
            "        if (!state.firmSlot || !outBranch) return;",
            "        if (firmBranch) firmBranch.dissolve();",
            "        firmBranch = outBranch.createBranch('firm-state');",
            "        firmBranch.activate(state.firmSlot);",
            "        state.firmSlot.appendChild(state.firmLeft > 0",
            "            ? fdk.chip(firmBranch, 'chip-2', 'good', 'FIRM \\u00b7 expires in ' + state.firmLeft + 's')",
            "            : fdk.chip(firmBranch, 'chip-3', 'neutral', 'INDICATIVE'));",
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
            "        // On failure the quote is gone, not stale: state is dropped and the",
            "        // FIRM countdown with it, before the kit draws the failure line.",
            "        fdk.load('/fx/price?ticket=' + encodeURIComponent(input.value),",
            "            { branch: branch, host: out, what: 'price' },",
            "            function (d) { state.data = d; state.firmLeft = 0; render(); },",
            "            function ()  { state.data = null; render(); });",
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
