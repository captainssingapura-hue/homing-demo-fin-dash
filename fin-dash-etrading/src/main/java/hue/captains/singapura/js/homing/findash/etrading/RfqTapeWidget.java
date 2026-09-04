package hue.captains.singapura.js.homing.findash.etrading;

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
                        new FdTextCss.fd_num(),
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
            "        actorId = fdk.actorId('etrading/rfq', branch);",
            "        party.joinActor({ id: actorId, parentSecretary: 'desk', reactors: {} });",
            "    }",
            "",
            "    var head = fdk.el(branch, 'head-1', 'div', fd_header_row);",
            "    head.appendChild(fdk.el(branch, 'title-1', 'span', fd_title, 'RFQ TAPE'));",
            "    head.appendChild(fdk.el(branch, 'cap-1', 'span', fd_caption, ",
            "        'every quote stamped with its slice (P1)'));",
            "    root.appendChild(head);",
            "",
            "    var list = fdk.el(branch, 'sect-1', 'div', fd_section);",
            "    root.appendChild(list);",
            "",
            "    function outcomeChip(i, o) {",
            "        if (o === 'WON')  return fdk.chip(branch, 'q-chip-' + i, 'good', 'WON');",
            "        if (o === 'LOST') return fdk.chip(branch, 'q-lost-' + i, 'serious', 'LOST');",
            "        return fdk.chip(branch, 'q-other-' + i, 'neutral', o);",
            "    }",
            "",
            "    function render(d) {",
            "        for (var i = 0; i < d.rfqs.length; i++) {",
            "            // Keyed by tape position — every element in the row carries the",
            "            // index, since a branch name may only be claimed once.",
            "            (function (q, i) {",
            "                var line = fdk.el(branch, 'q-' + i, 'div', [fd_row, fd_rule, fd_clickable]);",
            "                line.appendChild(outcomeChip(i, q.outcome));",
            "                var main = fdk.el(branch, 'q-main-' + i, 'div', null);",
            "                var top = fdk.el(branch, 'q-top-' + i, 'div', null);",
            "                top.appendChild(fdk.el(branch, 'q-meta-' + i, 'span', [fd_caption, fd_muted, fd_num],",
            "                    q.time + ' \\u00b7 ' + q.source + '  '));",
            "                top.appendChild(fdk.el(branch, 'q-desc-' + i, 'span', fd_strong, q.desc));",
            "                main.appendChild(top);",
            "                var sub = fdk.el(branch, 'q-sub-' + i, 'div', fd_header_row);",
            "                sub.appendChild(fdk.el(branch, 'q-quote-' + i, 'span', fd_caption, q.quote));",
            "                sub.appendChild(fdk.stamp(branch, 'q-stamp-' + i, { surface: q.stamp }));",
            "                main.appendChild(sub);",
            "                line.appendChild(main);",
            "                line.onclick = function () {",
            "                    var pair = q.desc.split(' ')[0];",
            "                    if (party && actorId) party.tellFrom(actorId,",
            "                        { kind: 'InstrumentSelected', instrument: { pair: pair } });",
            "                };",
            "                list.appendChild(line);",
            "            })(d.rfqs[i], i);",
            "        }",
            "    }",
            "",
            "    fetch('/fx/quoting')",
            "        .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
            "        .then(render)",
            "        .catch(function (e) {",
            "            list.appendChild(fdk.el(branch, 'err-1', 'div', fd_error_text, ",
            "                'tape load failed: ' + (e && e.message ? e.message : e)));",
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
