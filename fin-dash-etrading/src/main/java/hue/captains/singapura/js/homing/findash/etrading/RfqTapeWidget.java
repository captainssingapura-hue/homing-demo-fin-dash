package hue.captains.singapura.js.homing.findash.etrading;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * The RFQ tape (W3 companion): every quote with its source, decomposition,
 * win/loss against the eventual print where known — and its slice stamp (P1:
 * a lost quote during a degraded window shows exactly which surface priced
 * it). Clicking an entry publishes {@code InstrumentSelected} for its pair.
 */
public final class RfqTapeWidget extends WorkspaceWidget<WorkspaceWidget._None, RfqTapeWidget> {

    public static final RfqTapeWidget INSTANCE = new RfqTapeWidget();

    private RfqTapeWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, RfqTapeWidget> {}

    @Override protected _Construct<_None, RfqTapeWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "RFQ Tape"; }
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
            "        actorId = 'etrading/rfq-' + Math.random().toString(36).slice(2, 8);",
            "        party.joinActor({ id: actorId, parentSecretary: 'desk', reactors: {} });",
            "    }",
            "",
            "    var head = fdk.el('div', 'display:flex;align-items:baseline;gap:10px;flex-wrap:wrap;');",
            "    head.appendChild(fdk.el('span', 'font-weight:700;font-size:14px;letter-spacing:0.3px;', 'RFQ TAPE'));",
            "    head.appendChild(fdk.el('span', 'color:var(--color-text-primary);font-size:11.5px;',",
            "        'every quote stamped with its slice (P1)'));",
            "    root.appendChild(head);",
            "",
            "    var list = fdk.el('div', 'margin-top:8px;');",
            "    root.appendChild(list);",
            "",
            "    function outcomeChip(o) {",
            "        if (o === 'WON')  return fdk.chip('good', 'WON');",
            "        if (o === 'LOST') return fdk.chip('serious', 'LOST');",
            "        return fdk.chip('neutral', o);",
            "    }",
            "",
            "    function render(d) {",
            "        for (var i = 0; i < d.rfqs.length; i++) {",
            "            (function (q) {",
            "                var line = fdk.el('div', 'display:flex;gap:10px;align-items:center;padding:7px 4px;'",
            "                    + 'border-bottom:1px solid var(--color-border);font-size:12.5px;cursor:pointer;');",
            "                line.appendChild(outcomeChip(q.outcome));",
            "                var main = fdk.el('div', '');",
            "                var top = fdk.el('div', '');",
            "                top.appendChild(fdk.el('span', 'color:var(--color-text-muted);font-variant-numeric:tabular-nums;'",
            "                    + 'font-size:11px;', q.time + ' \\u00b7 ' + q.source + '  '));",
            "                top.appendChild(fdk.el('span', 'font-weight:600;', q.desc));",
            "                main.appendChild(top);",
            "                var sub = fdk.el('div', 'display:flex;gap:8px;align-items:baseline;');",
            "                sub.appendChild(fdk.el('span', 'color:var(--color-text-primary);font-size:11.5px;', q.quote));",
            "                sub.appendChild(fdk.stamp({ surface: q.stamp }));",
            "                main.appendChild(sub);",
            "                line.appendChild(main);",
            "                line.onclick = function () {",
            "                    var pair = q.desc.split(' ')[0];",
            "                    if (party && actorId) party.tellFrom(actorId,",
            "                        { kind: 'InstrumentSelected', instrument: { pair: pair } });",
            "                };",
            "                list.appendChild(line);",
            "            })(d.rfqs[i]);",
            "        }",
            "    }",
            "",
            "    fetch('/fx/quoting')",
            "        .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
            "        .then(render)",
            "        .catch(function (e) {",
            "            list.appendChild(fdk.el('div', 'color:#a8502a;',",
            "                'tape load failed: ' + (e && e.message ? e.message : e)));",
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
