package hue.captains.singapura.js.homing.findash.trader;

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
 * The barrier watch — W4's first-class barrier display as its own widget:
 * positions within the configured band of their trigger sorted by proximity,
 * with distance-to-level, notional, and the tightened budget state (P2 chips).
 * Clicking a row publishes {@code InstrumentSelected} on the desk party — the
 * pricer and surface manager jump to that pair.
 */
public final class BarrierWatchWidget
        extends WorkspaceWidget<WorkspaceWidget._None, BarrierWatchWidget> {

    public static final BarrierWatchWidget INSTANCE = new BarrierWatchWidget();

    private BarrierWatchWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, BarrierWatchWidget> {}

    @Override protected _Construct<_None, BarrierWatchWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Barrier Watch"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(new ModuleImports<>(
                List.of(new FinDashKitModule.fdk()), FinDashKitModule.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdFrameCss.fd_row(),
                        new FdFrameCss.fd_header_row(),
                        new FdFrameCss.fd_section(),
                        new FdFrameCss.fd_widget_root()),
                        FdFrameCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdStatusCss.fd_error_text()),
                        FdStatusCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdTextCss.fd_muted(),
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
            "        actorId = fdk.actorId('trader/barriers', branch);",
            "        party.joinActor({ id: actorId, parentSecretary: 'desk', reactors: {} });",
            "    }",
            "",
            "    var head = fdk.el(branch, 'head-1', 'div', fd_header_row);",
            "    head.appendChild(fdk.el(branch, 'title-1', 'span', fd_title, ",
            "        'BARRIER WATCH'));",
            "    head.appendChild(fdk.el(branch, 'cap-1', 'span', fd_caption, 'sorted by proximity'));",
            "    var stampSlot = fdk.el(branch, 'slot-1', 'span', null);",
            "    head.appendChild(stampSlot);",
            "    root.appendChild(head);",
            "",
            "    var list = fdk.el(branch, 'sect-1', 'div', fd_section);",
            "    root.appendChild(list);",
            "    root.appendChild(fdk.el(branch, 'footer', 'div', [fd_caption, fd_muted, fd_section],",
            "        'positions inside their band tighten the reval error budget (two-speed risk) \\u00b7 '",
            "        + 'click a row to broadcast the selection'));",
            "",
            "    function render(d) {",
            "        stampSlot.appendChild(fdk.stamp(branch, 'stamp-1', { slice: d.slice, model: d.model }));",
            "        var bs = d.barriers.slice().sort(function (a, b) { return a.pips - b.pips; });",
            "        for (var i = 0; i < bs.length; i++) {",
            "            // Every element is keyed by the row index — one name per barrier,",
            "            // since a branch name may only be claimed once.",
            "            (function (b, i) {",
            "                var line = fdk.el(branch, 'b-' + i, 'div', [fd_row, fd_rule, fd_clickable]);",
            "                line.appendChild(fdk.chip(branch, 'b-chip-' + i,",
            "                    b.severity === 'good' ? 'good' : b.severity, b.pips + ' pips'));",
            "                var main = fdk.el(branch, 'b-main-' + i, 'div', null);",
            "                main.appendChild(fdk.el(branch, 'b-level-' + i, 'div', fd_strong,",
            "                    b.pair + ' ' + b.type + ' ' + b.level));",
            "                main.appendChild(fdk.el(branch, 'b-note-' + i, 'div', fd_caption,",
            "                    b.notional + ' notional \\u00b7 ' + b.note));",
            "                line.appendChild(main);",
            "                line.onclick = function () {",
            "                    if (party && actorId) {",
            "                        party.tellFrom(actorId, { kind: 'InstrumentSelected',",
            "                            instrument: { pair: b.pair } });",
            "                    }",
            "                };",
            "                list.appendChild(line);",
            "            })(bs[i], i);",
            "        }",
            "    }",
            "",
            "    fetch('/fx/barriers')",
            "        .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
            "        .then(render)",
            "        .catch(function (e) {",
            "            list.appendChild(fdk.el(branch, 'err-1', 'div', fd_error_text, ",
            "                'barriers load failed: ' + (e && e.message ? e.message : e)));",
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
