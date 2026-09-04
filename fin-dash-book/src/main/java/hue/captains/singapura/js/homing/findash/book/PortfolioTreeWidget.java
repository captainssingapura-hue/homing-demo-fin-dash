package hue.captains.singapura.js.homing.findash.book;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.js.TreeRendererModule;
import hue.captains.singapura.js.homing.findash.core.css.FdFrameCss;
import hue.captains.singapura.js.homing.findash.core.css.FdStatusCss;
import hue.captains.singapura.js.homing.findash.core.css.FdTextCss;
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
                        TreeRendererModule.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdFrameCss.fd_widget_root(),
                        new FdFrameCss.fd_header_row()),
                        FdFrameCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdTextCss.fd_title(),
                        new FdTextCss.fd_caption(),
                        new FdTextCss.fd_muted()),
                        FdTextCss.INSTANCE),
                new ModuleImports<>(List.of(new FdStatusCss.fd_error_text()),
                        FdStatusCss.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
            "    var root = branch.createElement('root', 'div');",
            "    css.setClass(root, fd_widget_root);",
            "",
            "    var head = fdk.el(branch, 'head', 'div', fd_header_row);",
            "    head.appendChild(fdk.el(branch, 'head-title', 'span', fd_title, 'PORTFOLIOS'));",
            "    // The selection chip is built once and re-labelled, rather than rebuilt",
            "    // per selection: branch element names are unique for the branch's life,",
            "    // so a chip minted on every arrow-key move would collide on the second one.",
            "    var selChip = fdk.chip(branch, 'sel-chip', 'neutral', '\\u2014');",
            "    head.appendChild(selChip);",
            "    root.appendChild(head);",
            "",
            "    var container = branch.createElement('treeContainer', 'div');",
            "    root.appendChild(container);",
            "    var status = fdk.el(branch, 'status', 'div', fd_caption, 'Loading portfolios\\u2026');",
            "    css.addClass(status, fd_muted);",
            "    container.appendChild(status);",
            "    var hint = fdk.el(branch, 'hint', 'div', fd_caption,",
            "        '\\u2191\\u2193 move \\u00b7 \\u2192\\u2190 expand/fold \\u00b7 selection broadcasts at any level');",
            "    css.addClass(hint, fd_muted);",
            "    root.appendChild(hint);",
            "",
            "    var party = (workspaceCtx && workspaceCtx.deskParty) ? workspaceCtx.deskParty : null;",
            "    var actorId = null;",
            "    if (party) {",
            "        actorId = 'book/portfolios-' + Math.random().toString(36).slice(2, 8);",
            "        party.joinActor({ id: actorId, parentSecretary: 'desk', reactors: {} });",
            "    }",
            "",
            "    var index = {};",
            "",
            "    // RFC 0053: the machine field no longer rides in the display slot.",
            "    // A node's address is its namePath — the '/'-joined chain of segments,",
            "    // with the root's own segment excluded, so an empty path IS the root.",
            "    // Segments are slugged (lower-cased) from the portfolio id, hence the",
            "    // case-folded lookup rather than a direct index hit.",
            "    var rootId = null;",
            "    var bySegment = {};",
            "",
            "    function idOf(sel) {",
            "        var np = sel.namePath || '';",
            "        if (!np) return rootId;",
            "        var parts = np.split('/');",
            "        return bySegment[parts[parts.length - 1]];",
            "    }",
            "",
            "    function broadcast(sel) {",
            "        var id = idOf(sel);",
            "        var meta = id ? index[id] : null;",
            "        if (!meta) return;",
            "        selChip.textContent = '\\u25cb ' + meta.label + ' \\u00b7 ' + meta.positions + ' pos';",
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
            "            rootId = d.tree && d.tree.segment ? d.tree.segment : null;",
            "            for (var k in index) {",
            "                if (Object.prototype.hasOwnProperty.call(index, k)) bySegment[k.toLowerCase()] = k;",
            "            }",
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
            "            css.addClass(status, fd_error_text);",
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
            "            if (actorId && party) { party.leave(actorId); }",
            "        }",
            "    };");
    }
}
