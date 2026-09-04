package hue.captains.singapura.js.homing.findash.risk;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;
import hue.captains.singapura.js.homing.findash.core.css.FdFrameCss;
import hue.captains.singapura.js.homing.findash.core.css.FdStatusCss;
import hue.captains.singapura.js.homing.findash.core.css.FdTextCss;
import hue.captains.singapura.js.homing.findash.core.css.FdControlCss;
import hue.captains.singapura.js.homing.findash.core.css.FdSurfaceCss;

import java.util.List;

/**
 * Concentration views (study §8) — the shapes FX risk actually needs:
 * barrier-density bands (notional of triggers within bands of current spot —
 * where the book's gamma flips), and event-date vega concentration. Rows
 * publish their pair on the desk party.
 */
public final class ConcentrationWidget
        extends WorkspaceWidget<WorkspaceWidget._None, ConcentrationWidget> {

    public static final ConcentrationWidget INSTANCE = new ConcentrationWidget();

    private ConcentrationWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, ConcentrationWidget> {}

    @Override protected _Construct<_None, ConcentrationWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Concentrations"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(new ModuleImports<>(
                List.of(new FinDashKitModule.fdk()), FinDashKitModule.INSTANCE),
                new ModuleImports<>(List.of(
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
                        FdTextCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdControlCss.fd_clickable()),
                        FdControlCss.INSTANCE),
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
            "    var party = (workspaceCtx && workspaceCtx.deskParty) ? workspaceCtx.deskParty : null;",
            "    var actorId = null;",
            "    if (party) {",
            "        actorId = fdk.actorId('risk/concentration', branch);",
            "        party.joinActor({ id: actorId, parentSecretary: 'desk', reactors: {} });",
            "    }",
            "",
            "    root.appendChild(fdk.el(branch, 'title-1', 'div', fd_title, ",
            "        'CONCENTRATIONS'));",
            "",
            "    root.appendChild(fdk.sectionTitle(branch, 'sect-1', 'Barrier density \\u2014 notional within bands of spot'));",
            "    var density = fdk.el(branch, 'slot-1', 'div', null);",
            "    root.appendChild(density);",
            "",
            "    root.appendChild(fdk.sectionTitle(branch, 'sect-2', 'Event-date vega'));",
            "    var events = fdk.el(branch, 'slot-2', 'div', null);",
            "    root.appendChild(events);",
            "",
            "    // k keys every element: this is called once per row, and a branch",
            "    // element name may only be claimed once.",
            "    function row(k, container, chipState, chipText, boldText, subText, pair) {",
            "        var line = fdk.el(branch, k, 'div', [fd_row, fd_rule, fd_clickable]);",
            "        line.appendChild(fdk.chip(branch, k + '-chip', chipState, chipText));",
            "        var main = fdk.el(branch, k + '-main', 'div', null);",
            "        main.appendChild(fdk.el(branch, k + '-label', 'div', fd_strong, boldText));",
            "        main.appendChild(fdk.el(branch, k + '-note', 'div', fd_caption, subText));",
            "        line.appendChild(main);",
            "        line.onclick = function () {",
            "            if (party && actorId && pair) party.tellFrom(actorId,",
            "                { kind: 'InstrumentSelected', instrument: { pair: pair } });",
            "        };",
            "        container.appendChild(line);",
            "    }",
            "",
            "    fetch('/fx/risk')",
            "        .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
            "        .then(function (d) {",
            "            for (var i = 0; i < d.density.length; i++) {",
            "                var b = d.density[i];",
            "                row('d-' + i, density, b.severity, b.notional, b.pair + ' \\u00b7 ' + b.band + ' of spot',",
            "                    b.note, b.pair);",
            "            }",
            "            for (i = 0; i < d.eventVega.length; i++) {",
            "                var e = d.eventVega[i];",
            "                row('e-' + i, events, e.severity, e.vega, e.event + ' \\u00b7 ' + e.date,",
            "                    'vega concentrated on the event date', null);",
            "            }",
            "        })",
            "        .catch(function (e) {",
            "            density.appendChild(fdk.el(branch, 'err-1', 'div', fd_error_text, ",
            "                'concentration load failed: ' + (e && e.message ? e.message : e)));",
            "        });",
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
