package hue.captains.singapura.js.homing.findash.ontology;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;
import hue.captains.singapura.js.homing.findash.core.css.FdFrameCss;
import hue.captains.singapura.js.homing.findash.core.css.FdStatusCss;
import hue.captains.singapura.js.homing.findash.core.css.FdTextCss;
import hue.captains.singapura.js.homing.findash.core.css.FdSurfaceCss;

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
                        new FdStatusCss.fd_status_warn(),
                        new FdStatusCss.fd_error_text()),
                        FdStatusCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdTextCss.fd_dense(),
                        new FdTextCss.fd_muted(),
                        new FdTextCss.fd_caption(),
                        new FdTextCss.fd_strong(),
                        new FdTextCss.fd_title()),
                        FdTextCss.INSTANCE),
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
            "    body.appendChild(fdk.el(branch, 'cap-1', 'div', [fd_dense, fd_muted], ",
            "        'select a data type in the tree \\u2014 this pane answers \"which widgets require it?\"'));",
            "",
            "    var index = {};",
                        "",
            "    var ROLE = {",
            "        DRIVES:       { state: 'live',    text: 'drives' },",
            "        FOLLOWS:      { state: 'good',    text: 'follows' },",
            "        RENDERS:      { state: 'neutral', text: 'renders' },",
            "        DERIVES_FROM: { state: 'warn',    text: 'derives from' }",
            "    };",
            "",
            "    function line(b, key, container, label, value) {",
            "        container.appendChild(fdk.kv(b, key, label, value));",
            "    }",
            "",
            "    var viewBranch = null;",
            "",
            "    function render(id) {",
            "        var t = index[id];",
            "        // Rebuilt on every type selection, so the whole pane lives in a",
            "        // sub-branch that is dissolved first and every element is keyed.",
            "        if (viewBranch) viewBranch.dissolve();",
            "        viewBranch = branch.createBranch('view');",
            "        viewBranch.activate(root);",
            "        var b = viewBranch;",
            "        if (!t) {",
            "            body.appendChild(fdk.el(b, 'err', 'div', fd_error_text, 'unknown type: ' + id));",
            "            return;",
            "        }",
            "        title.textContent = 'TYPE USAGE \\u2014 ' + id;",
            "        chips.appendChild(fdk.chip(b, 'chip-marker', 'neutral', t.marker.toLowerCase().replace(/_/g, ' ')));",
            "        chips.appendChild(fdk.chip(b, 'chip-stratum', 'neutral', 'stratum ' + t.stratum.toLowerCase()));",
            "        chips.appendChild(fdk.chip(b, 'chip-era', 'good', 'era ' + t.era));",
            "",
            "        body.appendChild(fdk.el(b, 'summary', 'div', [fd_dense, fd_section], t.summary));",
            "        line(b, 'kv-java', body, 'java type', t.javaType);",
            "",
            "        body.appendChild(fdk.sectionTitle(b, 'sect-required', 'Required by (' + t.usages.length + ')'));",
            "        if (!t.usages.length) {",
            "            body.appendChild(fdk.el(b, 'no-usage', 'div', [fd_status_warn, fd_dense], ",
            "                'no widget declares a dependency on this type \\u2014 either it is purely "
                + "internal, or a consumer is missing from the map.'));",
            "        }",
            "        var byWorkspace = {};",
            "        for (var i = 0; i < t.usages.length; i++) {",
            "            var u = t.usages[i];",
            "            (byWorkspace[u.workspace] = byWorkspace[u.workspace] || []).push(u);",
            "        }",
            "        Object.keys(byWorkspace).sort().forEach(function (ws) {",
            "            var group = fdk.el(b, 'ws-' + ws, 'div', fd_section);",
            "            group.appendChild(fdk.el(b, 'ws-head-' + ws, 'div', [fd_strong, fd_caption, fd_rule],",
            "                '\\u25b8 ' + ws + ' workspace'));",
            "            byWorkspace[ws].forEach(function (u) {",
            "                var row = fdk.el(b, 'u-' + u.widget, 'div', [fd_cluster, fd_dense]);",
            "                var r = ROLE[u.role] || { state: 'neutral', text: u.role };",
            "                row.appendChild(fdk.chip(b, 'u-chip-' + u.widget, r.state, r.text));",
            "                var main = fdk.el(b, 'u-main-' + u.widget, 'div', null);",
            "                main.appendChild(fdk.el(b, 'u-name-' + u.widget, 'div', fd_strong, u.widget));",
            "                main.appendChild(fdk.el(b, 'u-note-' + u.widget, 'div', fd_caption, u.note));",
            "                row.appendChild(main);",
            "                group.appendChild(row);",
            "            });",
            "            body.appendChild(group);",
            "        });",
            "",
            "        if (t.relations.length) {",
            "            body.appendChild(fdk.sectionTitle(b, 'sect-points', 'Points at'));",
            "            t.relations.forEach(function (r, ri) {",
            "                body.appendChild(fdk.el(b, 'rel-' + ri, 'div', fd_dense,",
            "                    '\\u2192 ' + r.to + '  (' + r.edge + ', '",
            "                    + r.cardinality.toLowerCase().replace(/_/g, ' ') + ') \\u00b7 ' + r.purpose));",
            "            });",
            "        }",
            "        if (t.incoming.length) {",
            "            body.appendChild(fdk.sectionTitle(b, 'sect-pointed', 'Pointed at by'));",
            "            t.incoming.forEach(function (r, ri) {",
            "                body.appendChild(fdk.el(b, 'inc-' + ri, 'div', fd_dense,",
            "                    '\\u2190 ' + r.from + '  (' + r.edge + ') \\u00b7 ' + r.purpose));",
            "            });",
            "        }",
            "    }",
            "",
            "    var party = (workspaceCtx && workspaceCtx.deskParty) ? workspaceCtx.deskParty : null;",
            "    var actorId = null;",
            "    if (party) {",
            "        actorId = fdk.actorId('ontology/usage', branch);",
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
            "            if (actorId && party) { party.leave(actorId); }",
            "        }",
            "    };");
    }
}
