package hue.captains.singapura.js.homing.findash.book;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.js.TreeRendererModule;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * The portfolio navigator — the book hierarchy (desk → book → portfolio) on
 * the framework's {@code TreeRenderer}, <b>keyboard-first</b>: arrows move
 * the selection (live-follow), ArrowRight/Left expand/fold, and every move
 * broadcasts {@code PortfolioSelected} on the desk party with the selected
 * node resolved to its <b>leaf-portfolio ids</b> — so the Portfolio view,
 * Trade Blotter, and any other consumer filter to the subtree, whatever
 * level was picked. The same renderer that draws the conformance studio's
 * crate tree; zero bespoke tree JS.
 */
public final class PortfolioTreeWidget
        extends WorkspaceWidget<WorkspaceWidget._None, PortfolioTreeWidget> {

    public static final PortfolioTreeWidget INSTANCE = new PortfolioTreeWidget();

    private PortfolioTreeWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, PortfolioTreeWidget> {}

    @Override protected _Construct<_None, PortfolioTreeWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Portfolios"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(
                new ModuleImports<>(List.of(new FinDashKitModule.fdk()), FinDashKitModule.INSTANCE),
                new ModuleImports<>(List.of(new TreeRendererModule.TreeRenderer()),
                        TreeRendererModule.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
            "    var root = branch.createElement('root', 'div');",
            "    root.style.cssText = 'height:100%;overflow:auto;box-sizing:border-box;padding:10px 6px;'",
            "        + 'font-family:system-ui,sans-serif;font-size:13px;color:#0b0b0b;';",
            "",
            "    var head = fdk.el('div', 'display:flex;align-items:baseline;gap:8px;padding:0 8px;flex-wrap:wrap;');",
            "    head.appendChild(fdk.el('span', 'font-weight:700;font-size:13px;letter-spacing:0.3px;', 'PORTFOLIOS'));",
            "    var selSlot = fdk.el('span', '');",
            "    head.appendChild(selSlot);",
            "    root.appendChild(head);",
            "",
            "    var container = branch.createElement('treeContainer', 'div');",
            "    root.appendChild(container);",
            "    var status = fdk.el('div', 'padding:6px 8px;color:#898781;font-size:12px;', 'Loading portfolios\\u2026');",
            "    container.appendChild(status);",
            "    root.appendChild(fdk.el('div', 'color:#898781;font-size:10.5px;padding:6px 8px;',",
            "        '\\u2191\\u2193 move \\u00b7 \\u2192\\u2190 expand/fold \\u00b7 selection broadcasts at any level'));",
            "",
            "    var party = (workspaceCtx && workspaceCtx.deskParty) ? workspaceCtx.deskParty : null;",
            "    var actorId = null;",
            "    if (party) {",
            "        actorId = 'book/portfolios-' + Math.random().toString(36).slice(2, 8);",
            "        party.joinActor({ id: actorId, parentSecretary: 'desk', reactors: {} });",
            "    }",
            "",
            "    var index = {};",
            "    function clear(n) { while (n.firstChild) n.removeChild(n.firstChild); }",
            "",
            "    function broadcast(sel) {",
            "        var id = sel.summary;             // the node's machine field",
            "        var meta = index[id];",
            "        if (!meta) return;",
            "        clear(selSlot);",
            "        selSlot.appendChild(fdk.chip('neutral', meta.label + ' \\u00b7 ' + meta.positions + ' pos'));",
            "        if (party && actorId) {",
            "            party.tellFrom(actorId, { kind: 'PortfolioSelected',",
            "                portfolio: { id: id, label: meta.label, leafIds: meta.leafIds } });",
            "        }",
            "    }",
            "",
            "    var renderer = null;",
            "    var keyHandler = function (ev) {",
            "        if (renderer && renderer.handleKeydown(ev)) ev.preventDefault();",
            "    };",
            "",
            "    fetch('/fx/portfolios')",
            "        .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
            "        .then(function (d) {",
            "            index = d.index;",
            "            container.removeChild(status);",
            "            renderer = new TreeRenderer({",
            "                branch:      branch,",
            "                container:   container,",
            "                data:        d.tree,",
            "                expandDepth: 2,",
            "                onSelect:    broadcast,",
            "                onActivate:  broadcast",
            "            });",
            "        })",
            "        .catch(function (err) {",
            "            status.style.color = '#a8502a';",
            "            status.textContent = 'portfolios load failed: '",
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
