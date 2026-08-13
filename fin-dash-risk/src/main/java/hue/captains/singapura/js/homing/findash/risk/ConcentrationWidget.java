package hue.captains.singapura.js.homing.findash.risk;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

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
                List.of(new FinDashKitModule.fdk()), FinDashKitModule.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
            "    var root = branch.createElement('root', 'div');",
            "    root.style.cssText = 'height:100%;overflow:auto;box-sizing:border-box;padding:14px;'",
            "        + 'font-family:system-ui,sans-serif;font-size:13px;color:var(--color-text-primary);';",
            "",
            "    var party = (workspaceCtx && workspaceCtx.deskParty) ? workspaceCtx.deskParty : null;",
            "    var actorId = null;",
            "    if (party) {",
            "        actorId = 'risk/concentration-' + Math.random().toString(36).slice(2, 8);",
            "        party.joinActor({ id: actorId, parentSecretary: 'desk', reactors: {} });",
            "    }",
            "",
            "    root.appendChild(fdk.el('div', 'font-weight:700;font-size:14px;letter-spacing:0.3px;',",
            "        'CONCENTRATIONS'));",
            "",
            "    root.appendChild(fdk.sectionTitle('Barrier density \\u2014 notional within bands of spot'));",
            "    var density = fdk.el('div', '');",
            "    root.appendChild(density);",
            "",
            "    root.appendChild(fdk.sectionTitle('Event-date vega'));",
            "    var events = fdk.el('div', '');",
            "    root.appendChild(events);",
            "",
            "    function row(container, chipState, chipText, boldText, subText, pair) {",
            "        var line = fdk.el('div', 'display:flex;gap:10px;align-items:center;padding:6px 4px;'",
            "            + 'border-bottom:1px solid var(--color-border);font-size:12.5px;cursor:pointer;');",
            "        line.appendChild(fdk.chip(chipState, chipText));",
            "        var main = fdk.el('div', '');",
            "        main.appendChild(fdk.el('div', 'font-weight:600;', boldText));",
            "        main.appendChild(fdk.el('div', 'color:var(--color-text-primary);font-size:11.5px;', subText));",
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
            "                row(density, b.severity, b.notional, b.pair + ' \\u00b7 ' + b.band + ' of spot',",
            "                    b.note, b.pair);",
            "            }",
            "            for (i = 0; i < d.eventVega.length; i++) {",
            "                var e = d.eventVega[i];",
            "                row(events, e.severity, e.vega, e.event + ' \\u00b7 ' + e.date,",
            "                    'vega concentrated on the event date', null);",
            "            }",
            "        })",
            "        .catch(function (e) {",
            "            density.appendChild(fdk.el('div', 'color:#a8502a;',",
            "                'concentration load failed: ' + (e && e.message ? e.message : e)));",
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
