package hue.captains.singapura.js.homing.findash.ontology;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.js.TreeRendererModule;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;
import hue.captains.singapura.js.homing.findash.core.css.FdFrameCss;

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
                        new FdFrameCss.fd_widget_root()),
                        FdFrameCss.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
            "    var root = branch.createElement('root', 'div');",
            "    css.setClass(root, fd_widget_root);",
            "",
            "    var head = fdk.el('div', 'display:flex;align-items:baseline;gap:8px;padding:0 8px;flex-wrap:wrap;');",
            "    head.appendChild(fdk.el('span', 'font-weight:700;font-size:13px;letter-spacing:0.3px;', ",
            "        'DATA TYPES'));",
            "    var selSlot = fdk.el(branch, 'slot-1', 'span', null);",
            "    head.appendChild(selSlot);",
            "    root.appendChild(head);",
            "    root.appendChild(fdk.el('div', 'color:var(--color-text-muted);font-size:10.5px;padding:2px 8px 6px;', ",
            "        'the ontology, by stratum \\u00b7 a type may reference only strata below its own'));",
            "",
            "    var container = branch.createElement('treeContainer', 'div');",
            "    root.appendChild(container);",
            "    var status = fdk.el('div', 'padding:6px 8px;color:var(--color-text-muted);font-size:12px;', 'Loading ontology\\u2026');",
            "    container.appendChild(status);",
            "    root.appendChild(fdk.el('div', 'color:var(--color-text-muted);font-size:10.5px;padding:6px 8px;', ",
            "        '\\u2191\\u2193 move \\u00b7 \\u2192\\u2190 expand/fold \\u00b7 selection drives the usage pane'));",
            "",
            "    var party = (workspaceCtx && workspaceCtx.deskParty) ? workspaceCtx.deskParty : null;",
            "    var actorId = null;",
            "    if (party) {",
            "        actorId = 'ontology/types-' + Math.random().toString(36).slice(2, 8);",
            "        party.joinActor({ id: actorId, parentSecretary: 'desk', reactors: {} });",
            "    }",
            "",
            "    var index = {};",
            "    function clear(n) { while (n.firstChild) n.removeChild(n.firstChild); }",
            "",
            "    function broadcast(sel) {",
            "        var id = sel.summary;",
            "        if (!id || id.indexOf('stratum:') === 0) { return; }   // a stratum node, not a type",
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
            "            container.removeChild(status);",
            "            renderer = new TreeRenderer({",
            "                branch: branch, container: container, data: d.tree,",
            "                expandDepth: 1, onSelect: broadcast, onActivate: broadcast",
            "            });",
            "        })",
            "        .catch(function (err) {",
            "            status.style.color = '#a8502a';",
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
            "            if (actorId && party) { try { party.leave(actorId); } catch (e) {} }",
            "        }",
            "    };");
    }
}
