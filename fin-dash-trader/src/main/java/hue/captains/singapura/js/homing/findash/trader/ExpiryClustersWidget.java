package hue.captains.singapura.js.homing.findash.trader;

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
 * Expiry / pin clusters — same-cut expiry clusters (the bulk-processing unit
 * at cuts like NY 10am / Tokyo 3pm, cut times zone-explicit per the NFRs) and
 * pin-risk candidates (expiry-day open interest near spot), each with P2
 * status chips. Clicking a row publishes {@code InstrumentSelected} on the
 * desk party.
 */
public final class ExpiryClustersWidget
        extends WorkspaceWidget<WorkspaceWidget._None, ExpiryClustersWidget> {

    public static final ExpiryClustersWidget INSTANCE = new ExpiryClustersWidget();

    private ExpiryClustersWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, ExpiryClustersWidget> {}

    @Override protected _Construct<_None, ExpiryClustersWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Expiry / Pins"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(new ModuleImports<>(
                List.of(new FinDashKitModule.fdk()), FinDashKitModule.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdFrameCss.fd_header_row(),
                        new FdFrameCss.fd_row(),
                        new FdFrameCss.fd_widget_root()),
                        FdFrameCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdStatusCss.fd_error_text()),
                        FdStatusCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdTextCss.fd_caption(),
                        new FdTextCss.fd_strong(),
                        new FdTextCss.fd_title()),
                        FdTextCss.INSTANCE));
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
            "        actorId = 'trader/expiries-' + Math.random().toString(36).slice(2, 8);",
            "        party.joinActor({ id: actorId, parentSecretary: 'desk', reactors: {} });",
            "    }",
            "",
            "    var head = fdk.el(branch, 'head-1', 'div', fd_header_row);",
            "    head.appendChild(fdk.el(branch, 'title-1', 'span', fd_title, ",
            "        'EXPIRY / PINS'));",
            "    head.appendChild(fdk.el(branch, 'cap-1', 'span', fd_caption, ",
            "        'cut times desk-local \\u00b7 UTC on hover'));",
            "    var stampSlot = fdk.el(branch, 'slot-1', 'span', null);",
            "    head.appendChild(stampSlot);",
            "    root.appendChild(head);",
            "",
            "    var cutsTitle = fdk.sectionTitle(branch, 'sect-1', 'Cut clusters (bulk processing unit)');",
            "    root.appendChild(cutsTitle);",
            "    var cuts = fdk.el(branch, 'slot-2', 'div', null);",
            "    root.appendChild(cuts);",
            "    var pinsTitle = fdk.sectionTitle(branch, 'sect-2', 'Pin candidates (open interest near spot)');",
            "    root.appendChild(pinsTitle);",
            "    var pins = fdk.el(branch, 'slot-3', 'div', null);",
            "    root.appendChild(pins);",
            "    root.appendChild(fdk.el('div', 'color:var(--color-text-muted);font-size:11px;margin-top:8px;', ",
            "        'portfolio expiry processing is preview-then-commit (middle office \\u00a710) \\u00b7 '",
            "        + 'click a row to broadcast the selection'));",
            "",
            "    function row(container, chipState, chipText, boldText, subText, pair) {",
            "        var line = fdk.el(branch, 'row-1', 'div', fd_row",
            "            + 'border-bottom:1px solid var(--color-border);font-size:12.5px;cursor:pointer;');",
            "        line.appendChild(fdk.chip(branch, 'chip-1', chipState, chipText));",
            "        var main = fdk.el(branch, 'slot-4', 'div', null);",
            "        main.appendChild(fdk.el(branch, 'strong-1', 'div', fd_strong, boldText));",
            "        main.appendChild(fdk.el(branch, 'cap-2', 'div', fd_caption, subText));",
            "        line.appendChild(main);",
            "        line.onclick = function () {",
            "            if (party && actorId) {",
            "                party.tellFrom(actorId, { kind: 'InstrumentSelected',",
            "                    instrument: { pair: pair } });",
            "            }",
            "        };",
            "        container.appendChild(line);",
            "    }",
            "",
            "    function render(d) {",
            "        stampSlot.appendChild(fdk.stamp(branch, 'stamp-1', { slice: d.slice, model: d.model }));",
            "        for (var i = 0; i < d.clusters.length; i++) {",
            "            var c = d.clusters[i];",
            "            row(cuts, c.severity === 'good' ? 'good' : c.severity, c.notional,",
            "                c.pair + ' \\u00b7 ' + c.cut, c.note, c.pair);",
            "        }",
            "        for (i = 0; i < d.pins.length; i++) {",
            "            var p = d.pins[i];",
            "            row(pins, p.severity === 'good' ? 'good' : p.severity, p.notional,",
            "                p.pair + ' ' + p.strike, p.note, p.pair);",
            "        }",
            "    }",
            "",
            "    fetch('/fx/expiries')",
            "        .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
            "        .then(render)",
            "        .catch(function (e) {",
            "            cuts.appendChild(fdk.el(branch, 'err-1', 'div', fd_error_text, ",
            "                'expiries load failed: ' + (e && e.message ? e.message : e)));",
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
