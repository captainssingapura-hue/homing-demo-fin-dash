package hue.captains.singapura.js.homing.findash.ontology;

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
 * <b>Who needs this type?</b> — the connection map, made interactive.
 *
 * <p>Follows a {@code DataTypeSelected} from the type tree and answers, for
 * that type: what it is, what mode of being it declares, which era introduced
 * it, what it points at, what points back at it, and — the point of the
 * widget — <b>every fin-dash widget that requires it</b>, grouped by workspace
 * and labelled with its role (drives / follows / renders / derives from).</p>
 *
 * <p>This validates the join contract before a single fact exists: if a type
 * has no consumers, or a screen's dependency is missing, it is visible here
 * rather than discovered halfway through authoring data.</p>
 */
public final class DataTypeUsageWidget
        extends WorkspaceWidget<WorkspaceWidget._None, DataTypeUsageWidget> {

    public static final DataTypeUsageWidget INSTANCE = new DataTypeUsageWidget();

    private DataTypeUsageWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, DataTypeUsageWidget> {}

    @Override protected _Construct<_None, DataTypeUsageWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Type Usage"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(new ModuleImports<>(
                List.of(new FinDashKitModule.fdk()), FinDashKitModule.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdFrameCss.fd_cluster(),
                        new FdFrameCss.fd_header_row(),
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
            "    var head = fdk.el(branch, 'head-1', 'div', fd_header_row);",
            "    var title = fdk.el(branch, 'title-1', 'span', fd_title, ",
            "        'TYPE USAGE');",
            "    head.appendChild(title);",
            "    var chips = fdk.el(branch, 'cluster-1', 'span', fd_cluster);",
            "    head.appendChild(chips);",
            "    root.appendChild(head);",
            "",
            "    var body = fdk.el(branch, 'sect-1', 'div', fd_section);",
            "    root.appendChild(body);",
            "    body.appendChild(fdk.el('div', 'color:var(--color-text-muted);font-size:12px;', ",
            "        'select a data type in the tree \\u2014 this pane answers \"which widgets require it?\"'));",
            "",
            "    var index = {};",
            "    function clear(n) { while (n.firstChild) n.removeChild(n.firstChild); }",
            "",
            "    var ROLE = {",
            "        DRIVES:       { state: 'live',    text: 'drives' },",
            "        FOLLOWS:      { state: 'good',    text: 'follows' },",
            "        RENDERS:      { state: 'neutral', text: 'renders' },",
            "        DERIVES_FROM: { state: 'warn',    text: 'derives from' }",
            "    };",
            "",
            "    function line(container, label, value) {",
            "        container.appendChild(fdk.kv(branch, 'kv-1', label, value));",
            "    }",
            "",
            "    function render(id) {",
            "        var t = index[id];",
            "        clear(body); clear(chips);",
            "        if (!t) {",
            "            body.appendChild(fdk.el(branch, 'err-1', 'div', fd_error_text, 'unknown type: ' + id));",
            "            return;",
            "        }",
            "        title.textContent = 'TYPE USAGE \\u2014 ' + id;",
            "        chips.appendChild(fdk.chip(branch, 'chip-1', 'neutral', t.marker.toLowerCase().replace(/_/g, ' ')));",
            "        chips.appendChild(fdk.chip(branch, 'chip-2', 'neutral', 'stratum ' + t.stratum.toLowerCase()));",
            "        chips.appendChild(fdk.chip(branch, 'chip-3', 'good', 'era ' + t.era));",
            "",
            "        body.appendChild(fdk.el('div', 'color:var(--color-text-primary);font-size:12.5px;line-height:1.6;'",
            "            + 'margin:6px 0 10px;', t.summary));",
            "        line(body, 'java type', t.javaType);",
            "",
            "        body.appendChild(fdk.sectionTitle(branch, 'sect-1', 'Required by (' + t.usages.length + ')'));",
            "        if (!t.usages.length) {",
            "            body.appendChild(fdk.el('div', 'color:#9a6b1f;font-size:12px;', ",
            "                'no widget declares a dependency on this type \\u2014 either it is purely "
                + "internal, or a consumer is missing from the map.'));",
            "        }",
            "        var byWorkspace = {};",
            "        for (var i = 0; i < t.usages.length; i++) {",
            "            var u = t.usages[i];",
            "            (byWorkspace[u.workspace] = byWorkspace[u.workspace] || []).push(u);",
            "        }",
            "        Object.keys(byWorkspace).sort().forEach(function (ws) {",
            "            var group = fdk.el('div', 'margin-bottom:6px;');",
            "            group.appendChild(fdk.el('div', 'font-weight:600;font-size:11.5px;color:#1c5cab;'",
            "                + 'border-bottom:1px solid var(--color-border);padding-bottom:2px;',",
            "                '\\u25b8 ' + ws + ' workspace'));",
            "            byWorkspace[ws].forEach(function (u) {",
            "                var row = fdk.el('div', 'display:flex;gap:8px;align-items:baseline;'",
            "                    + 'padding:4px 2px;font-size:12.5px;');",
            "                var r = ROLE[u.role] || { state: 'neutral', text: u.role };",
            "                row.appendChild(fdk.chip(r.state, r.text));",
            "                var main = fdk.el(branch, 'slot-1', 'div', null);",
            "                main.appendChild(fdk.el(branch, 'strong-1', 'div', fd_strong, u.widget));",
            "                main.appendChild(fdk.el(branch, 'cap-1', 'div', fd_caption, u.note));",
            "                row.appendChild(main);",
            "                group.appendChild(row);",
            "            });",
            "            body.appendChild(group);",
            "        });",
            "",
            "        if (t.relations.length) {",
            "            body.appendChild(fdk.sectionTitle(branch, 'sect-2', 'Points at'));",
            "            t.relations.forEach(function (r) {",
            "                body.appendChild(fdk.el('div', 'font-size:12px;color:var(--color-text-primary);line-height:1.6;', ",
            "                    '\\u2192 ' + r.to + '  (' + r.edge + ', '",
            "                    + r.cardinality.toLowerCase().replace(/_/g, ' ') + ') \\u00b7 ' + r.purpose));",
            "            });",
            "        }",
            "        if (t.incoming.length) {",
            "            body.appendChild(fdk.sectionTitle('Pointed at by'));",
            "            t.incoming.forEach(function (r) {",
            "                body.appendChild(fdk.el('div', 'font-size:12px;color:var(--color-text-primary);line-height:1.6;', ",
            "                    '\\u2190 ' + r.from + '  (' + r.edge + ') \\u00b7 ' + r.purpose));",
            "            });",
            "        }",
            "    }",
            "",
            "    var party = (workspaceCtx && workspaceCtx.deskParty) ? workspaceCtx.deskParty : null;",
            "    var actorId = null;",
            "    if (party) {",
            "        actorId = 'ontology/usage-' + Math.random().toString(36).slice(2, 8);",
            "        party.joinActor({",
            "            id: actorId,",
            "            parentSecretary: 'desk',",
            "            reactors: {",
            "                DataTypeChanged: function (msg) {",
            "                    var dt = msg.dataType || {};",
            "                    if (dt.id) render(dt.id);",
            "                }",
            "            }",
            "        });",
            "    }",
            "",
            "    fetch('/fx/data-types')",
            "        .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
            "        .then(function (d) { index = d.index; })",
            "        .catch(function (e) {",
            "            body.appendChild(fdk.el(branch, 'err-2', 'div', fd_error_text, ",
            "                'ontology load failed: ' + (e && e.message ? e.message : e)));",
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
