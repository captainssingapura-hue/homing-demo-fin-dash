package hue.captains.singapura.js.homing.findash.ontology;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.js.TreeRendererModule;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;
import hue.captains.singapura.js.homing.findash.core.css.FdFrameCss;
import hue.captains.singapura.js.homing.findash.core.css.FdStatusCss;
import hue.captains.singapura.js.homing.findash.core.css.FdTextCss;

import java.util.List;

/**
 * The data-type catalogue as a keyboard-navigable tree: stratum → type.
 *
 * <p>Same {@code TreeRenderer} substrate as the portfolio tree and the
 * conformance studio's crate tree — a third proof that a conforming tree
 * renders with no bespoke JS. Selecting a type publishes {@code
 * DataTypeSelected} on the desk party; the usage pane answers it.</p>
 */
public final class DataTypeTreeWidget
        extends WorkspaceWidget<WorkspaceWidget._None, DataTypeTreeWidget> {

    public static final DataTypeTreeWidget INSTANCE = new DataTypeTreeWidget();

    private DataTypeTreeWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, DataTypeTreeWidget> {}

    @Override protected _Construct<_None, DataTypeTreeWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Data Types"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(
                new ModuleImports<>(List.of(new FinDashKitModule.fdk()), FinDashKitModule.INSTANCE),
                new ModuleImports<>(List.of(new TreeRendererModule.TreeRenderer()),
                        TreeRendererModule.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdFrameCss.fd_header_row(),
                        new FdFrameCss.fd_widget_root()),
                        FdFrameCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdStatusCss.fd_error_text()),
                        FdStatusCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdTextCss.fd_caption(),
                        new FdTextCss.fd_dense(),
                        new FdTextCss.fd_muted(),
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
            "    head.appendChild(fdk.el(branch, 'title-1', 'span', fd_title, ",
            "        'DATA TYPES'));",
            "    var selSlot = fdk.el(branch, 'slot-1', 'span', null);",
            "    head.appendChild(selSlot);",
            "    root.appendChild(head);",
            "    root.appendChild(fdk.el(branch, 'cap-1', 'div', [fd_muted, fd_caption], ",
            "        'the ontology, by stratum \\u00b7 a type may reference only strata below its own'));",
            "",
            "    var container = branch.createElement('treeContainer', 'div');",
            "    root.appendChild(container);",
            "    var status = fdk.el(branch, 'txt-1', 'div', [fd_muted, fd_dense], 'Loading ontology\\u2026');",
            "    container.appendChild(status);",
            "    root.appendChild(fdk.el(branch, 'cap-2', 'div', [fd_muted, fd_caption], ",
            "        '\\u2191\\u2193 move \\u00b7 \\u2192\\u2190 expand/fold \\u00b7 selection drives the usage pane'));",
            "",
            "    var party = (workspaceCtx && workspaceCtx.deskParty) ? workspaceCtx.deskParty : null;",
            "    var actorId = null;",
            "    if (party) {",
            "        actorId = fdk.actorId('ontology/types', branch);",
            "        party.joinActor({ id: actorId, parentSecretary: 'desk', reactors: {} });",
            "    }",
            "",
            "    var index = {};",
            "    function clear(n) { while (n.firstChild) n.removeChild(n.firstChild); }",
            "",
            "    // RFC 0053: address a node by its namePath, not by a machine field",
            "    // smuggled through the display slot. Segments are slugged (lower-cased)",
            "    // from the type id — 'PairId' becomes 'pairid' — so the last segment is",
            "    // folded back to the real id through a map built from the index.",
            "    var bySegment = {};",
            "",
            "    function broadcast(sel) {",
            "        var np = sel.namePath || '';",
            "        if (!np) { return; }                                   // the root, not a type",
            "        var parts = np.split('/');",
            "        var id = bySegment[parts[parts.length - 1]];",
            "        if (!id) { return; }                                   // a stratum node, not a type",
            "        var meta = index[id];",
            "        if (!meta) { return; }",
            "        clear(selSlot);",
            "        selSlot.appendChild(fdk.chip(branch, 'chip-1', 'neutral', id + ' \\u00b7 ' + meta.usages.length + ' widgets'));",
            "        if (party && actorId) {",
            "            party.tellFrom(actorId, { kind: 'DataTypeSelected', dataType: { id: id } });",
            "        }",
            "    }",
            "",
            "    var renderer = null;",
            "    var keyHandler = function (ev) {",
            "        if (renderer && renderer.handleKeydown(ev)) ev.preventDefault();",
            "    };",
            "",
            "    fetch('/fx/data-types')",
            "        .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
            "        .then(function (d) {",
            "            index = d.index;",
            "            for (var k in index) {",
            "                if (Object.prototype.hasOwnProperty.call(index, k)) bySegment[k.toLowerCase()] = k;",
            "            }",
            "            container.removeChild(status);",
            "            renderer = new TreeRenderer({",
            "                branch: branch, container: container, data: d.tree,",
            "                expandDepth: 1, onSelect: broadcast, onActivate: broadcast",
            "            });",
            "        })",
            "        .catch(function (err) {",
            "            css.addClass(status, fd_error_text);",
            "            status.textContent = 'ontology load failed: '",
            "                + (err && err.message ? err.message : String(err));",
            "        });",
            "",
            "    return {",
            "        root: root,",
            "        setActive: function (active) {",
            "            if (active) document.addEventListener('keydown', keyHandler);",
            "            else        document.removeEventListener('keydown', keyHandler);",
            "        },",
            "        partyDeregister: function () {",
            "            document.removeEventListener('keydown', keyHandler);",
            "            if (actorId && party) { party.leave(actorId); }",
            "        }",
            "    };");
    }
}
