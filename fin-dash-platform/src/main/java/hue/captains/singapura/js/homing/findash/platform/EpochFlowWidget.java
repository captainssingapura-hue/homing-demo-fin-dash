package hue.captains.singapura.js.homing.findash.platform;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashGridModule;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;
import hue.captains.singapura.js.homing.findash.core.css.FdFrameCss;
import hue.captains.singapura.js.homing.findash.core.css.FdStatusCss;
import hue.captains.singapura.js.homing.findash.core.css.FdTextCss;

import java.util.List;

/**
 * The epoch-flow pane (W5): per-pair surface epoch age vs expected cadence,
 * with click-through to the root cause and its affected consumers — the
 * study's "click-through: waiting on 1M BF quarantine → affected: USDJPY
 * surface, EURJPY cross". Clicking a row also publishes the pair on the desk
 * party.
 */
public final class EpochFlowWidget
        extends WorkspaceWidget<WorkspaceWidget._None, EpochFlowWidget> {

    public static final EpochFlowWidget INSTANCE = new EpochFlowWidget();

    private EpochFlowWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, EpochFlowWidget> {}

    @Override protected _Construct<_None, EpochFlowWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Epoch Flow"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(
                new ModuleImports<>(List.of(new FinDashKitModule.fdk()), FinDashKitModule.INSTANCE),
                new ModuleImports<>(List.of(new FinDashGridModule.fdGrid()), FinDashGridModule.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdFrameCss.fd_section(),
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
            "        actorId = 'platform/epochs-' + Math.random().toString(36).slice(2, 8);",
            "        party.joinActor({ id: actorId, parentSecretary: 'desk', reactors: {} });",
            "    }",
            "",
            "    root.appendChild(fdk.el(branch, 'title-1', 'div', fd_title, ",
            "        'EPOCH FLOW'));",
            "    root.appendChild(fdk.el(branch, 'cap-1', 'div', [fd_caption, fd_section], ",
            "        'surface epoch age vs expected cadence \\u00b7 click a row for root cause'));",
            "",
            "    var gridSlot = fdk.el(branch, 'slot-1', 'div', null);",
            "    root.appendChild(gridSlot);",
            "    var curve = fdk.el('div', 'color:var(--color-text-primary);font-size:12px;margin-top:6px;');",
            "    root.appendChild(curve);",
            "    var cause = fdk.el('div', 'margin-top:6px;font-size:12px;color:var(--color-text-muted);min-height:18px;', ",
            "        'root cause appears here');",
            "    root.appendChild(cause);",
            "",
            "    fetch('/fx/platform')",
            "        .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
            "        .then(function (d) {",
            "            var grid = fdGrid(branch, {",
            "                compact: true,",
            "                columns: [",
            "                    { key: 'pair', label: 'Pair', render: function (v) {",
            "                        return fdk.el(branch, 'strong-1', 'span', fd_strong, v); } },",
            "                    { key: 'epoch', label: 'Surface', align: 'right' },",
            "                    { key: 'age', label: 'Age', align: 'right' },",
            "                    { key: 'expected', label: 'Expected', align: 'right' },",
            "                    { key: 'state', label: 'State', render: function (v, row) {",
            "                        return v === 'ok' ? fdk.chip('good', row.note || 'on cadence')",
            "                            : fdk.chip('warn', 'behind cadence'); } }",
            "                ],",
            "                rows: d.epochs,",
            "                onRowClick: function (row) {",
            "                    cause.style.color = row.note ? 'var(--color-text-primary)' : 'var(--color-text-muted)';",
            "                    cause.textContent = row.note",
            "                        ? row.pair + ': ' + row.note",
            "                        : row.pair + ': on cadence \\u2014 nothing waiting';",
            "                    if (party && actorId) party.tellFrom(actorId,",
            "                        { kind: 'InstrumentSelected', instrument: { pair: row.pair } });",
            "                }",
            "            });",
            "            gridSlot.appendChild(grid.root);",
            "            curve.textContent = d.curveSet;",
            "        })",
            "        .catch(function (e) {",
            "            gridSlot.appendChild(fdk.el(branch, 'err-1', 'div', fd_error_text, ",
            "                'epoch flow load failed: ' + (e && e.message ? e.message : e)));",
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
