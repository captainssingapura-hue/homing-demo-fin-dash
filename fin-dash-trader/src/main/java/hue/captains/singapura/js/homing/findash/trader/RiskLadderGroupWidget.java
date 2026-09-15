package hue.captains.singapura.js.homing.findash.trader;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.css.FdControlCss;
import hue.captains.singapura.js.homing.findash.core.css.FdFrameCss;
import hue.captains.singapura.js.homing.findash.core.css.FdStatusCss;
import hue.captains.singapura.js.homing.findash.core.css.FdTextCss;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;
import hue.captains.singapura.js.homing.relgrid.group.RelGridGroupModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * The risk ladder on the Relation Grid's <b>group</b> — RFC 0050 · Episode 2,
 * docked beside {@link RiskLadderWidget} (Episode 1) so the comparison is the
 * acceptance test.
 *
 * <p>The ladder re-read as {@code docs/homologous-relations.md} re-read it:
 * <b>not one relation with four kinds of row, but a stack of relations over
 * one schema</b> — a detail relation per pair, a one-row subtotal per pair, a
 * one-row book total — in a {@code RelGridGroup}, with a fence above each pair
 * that the domain fills (the pair's name and a fold toggle, wearing the desk's
 * own bevel) and one header that is the group's and sticks as a box.</p>
 *
 * <h2>What the widget owns, and what it does not</h2>
 *
 * <p>Two branches under its own — {@code grid}, handed whole to the group,
 * which activates it and gives every member's table a sub-branch; and
 * {@code domain}, divided here into a part per relation for its cells and a
 * part per fence. The group asks a cell for {@code cellElement()} once and
 * places it, a fence for {@code fenceElement()} once and places it; nothing
 * is rendered into anything and neither side sees the other's DOM.</p>
 *
 * <p>The group is a value: its member list is fixed for its life. A book
 * scope that changes the pairs on screen is a new group — teardown keeps the
 * widths and the folds, destroys the group (which disposes no fence), then
 * this widget disposes its fences and relations and dissolves both branches,
 * and builds again. The feed is untouched.</p>
 *
 * <h2>What stopped existing</h2>
 *
 * <p>The void cell, the section row, the band drawn by agreement, the
 * within-scope ordering, the fold predicate composed with focus, the copy
 * blanking, the write refusal, the {@code flushNow} after a fold. Fold to
 * subtotal is the group's fold. What remains is the member list, one fence,
 * one cell, and {@code verdictOf}.</p>
 *
 * <h2>The desk bus</h2>
 *
 * <p>{@code PortfolioChanged} with its pairs <i>is</i> the member list — the
 * book scope rebuilds the group over those pairs, the book total staying.
 * {@code InstrumentChanged} folds every other pair to its caption and makes
 * the named one active; both are one click back through the scope strip.</p>
 */
public final class RiskLadderGroupWidget
        extends WorkspaceWidget<WorkspaceWidget._None, RiskLadderGroupWidget> {

    public static final RiskLadderGroupWidget INSTANCE = new RiskLadderGroupWidget();

    private RiskLadderGroupWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, RiskLadderGroupWidget> {}

    @Override protected _Construct<_None, RiskLadderGroupWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Risk Ladder (group)"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(
                new ModuleImports<>(List.of(new FinDashKitModule.fdk()), FinDashKitModule.INSTANCE),
                new ModuleImports<>(List.of(new RelGridGroupModule.RelGridGroup()), RelGridGroupModule.INSTANCE),
                new ModuleImports<>(List.of(new LadderFeedModule.createLadderFeed()), LadderFeedModule.INSTANCE),
                new ModuleImports<>(List.of(
                        new LadderRelationModule.LADDER_COLUMNS(),
                        new LadderRelationModule.createLadderRelation()),
                        LadderRelationModule.INSTANCE),
                new ModuleImports<>(List.of(
                        new LadderFenceModule.createPairFence(),
                        new LadderFenceModule.createStampFence()),
                        LadderFenceModule.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdFrameCss.fd_cluster(),
                        new FdFrameCss.fd_header_row(),
                        new FdFrameCss.fd_scroll(),
                        new FdFrameCss.fd_section(),
                        new FdFrameCss.fd_widget_root()),
                        FdFrameCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdTextCss.fd_caption(),
                        new FdTextCss.fd_muted(),
                        new FdTextCss.fd_title()),
                        FdTextCss.INSTANCE),
                new ModuleImports<>(List.of(new FdStatusCss.fd_error_text()), FdStatusCss.INSTANCE),
                new ModuleImports<>(List.of(new FdControlCss.fd_btn_ghost()), FdControlCss.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
            "    var root = branch.createElement('root', 'div');",
            "    css.setClass(root, fd_widget_root);",
            "    var head = fdk.el(branch, 'head', 'div', fd_header_row);",
            "    head.appendChild(fdk.el(branch, 'title-1', 'span', fd_title, 'RISK LADDER'));",
            "    var scopeSlot = fdk.el(branch, 'scope', 'span', fd_cluster);",
            "    head.appendChild(scopeSlot);",
            "    root.appendChild(head);",
            "    root.appendChild(fdk.el(branch, 'cap-1', 'div', [fd_caption, fd_muted],",
            "        'on the grid GROUP (RFC 0050 \\u00b7 Episode 2) \\u00b7 a table per pair, a table per subtotal, one for the book"
                    + " \\u00b7 the caption above each is the desk\\u2019s own fence'));",
            "    var host = fdk.el(branch, 'host', 'div', [fd_section, fd_scroll]);",
            "    root.appendChild(host);",
            "",
            "    var owner = Object.freeze({ toString: function () { return 'risk ladder (group)'; } });",
            "    var feed = createLadderFeed({ branch: branch, host: host });",
            "",
            "    // VIEW STATE the widget keeps across rebuilds: the book scope (a set of",
            "    // pairs, or null for all), the instrument focus (a pair, or null), and",
            "    // what a group has of its own \\u2014 widths and folds.",
            "    //",
            "    // The widths are SEEDED, never left to the tables. Column agreement is",
            "    // the one thing separate tables cannot reach by themselves and the one",
            "    // thing the group exists to supply \\u2014 but it applies only what it holds,",
            "    // and with nothing held every member sizes its own columns by its own",
            "    // content, a subtotal table one way and a detail table another. A first",
            "    // snapshot is the host's to give (KT \\u00a79.7); after that it is whatever",
            "    // the members accepted, read back on every resize.",
            "    var state = { book: null, focus: null, folded: [],",
            "                  widths: { tenor: 118, delta: 74, vAtm: 68, vRr: 64, vBf: 64, theta: 64, fresh: 80 } };",
            "    var current = null;   // { group, relations, fences }",
            "",
            "    function pairsOnScreen() {",
            "        var all = feed.pairs();",
            "        if (!state.book) return all;",
            "        return all.filter(function (p) { return state.book.pairs.indexOf(p) >= 0; });",
            "    }",
            "",
            "    // THE GROUP IS A VALUE. Two branches, both dissolved at teardown: 'grid'",
            "    // handed whole to the group; 'domain' divided here \\u2014 a part per relation",
            "    // for its cells, a part per fence.",
            "    function build() {",
            "        var gridB = branch.createBranch('grid');",
            "        var domainB = branch.createBranch('domain');",
            "        domainB.activate(owner);",
            "        var relations = [], fences = [], members = [];",
            "        pairsOnScreen().forEach(function (pair) {",
            "            var fence = createPairFence(pair, {",
            "                branch: domainB.createBranch('fence-' + pair),",
            "                tell:   function (m) { return current ? current.group.tell(m) : false; },",
            "                folded: state.folded.indexOf(pair) >= 0",
            "            });",
            "            fences.push(fence);",
            "            var detail = createLadderRelation(feed, { pair: pair, kind: 'detail',   branch: domainB.createBranch('cells-' + pair) });",
            "            var sub    = createLadderRelation(feed, { pair: pair, kind: 'subtotal', branch: domainB.createBranch('cells-' + pair + '-sum') });",
            "            relations.push(detail, sub);",
            "            members.push({ id: pair,              fence: fence, grid: { relation: detail, label: pair } });",
            "            members.push({ id: pair + '/\\u03a3',               grid: { relation: sub,    label: pair + ' subtotal' } });",
            "        });",
            "        var book = createLadderRelation(feed, { kind: 'book', branch: domainB.createBranch('cells-book') });",
            "        relations.push(book);",
            "        members.push({ id: 'book', grid: { relation: book, label: 'FXO book total' } });",
            "        var stamp = createStampFence(feed, { branch: domainB.createBranch('fence-stamp') });",
            "        fences.push(stamp);",
            "        var group = new RelGridGroup({",
            "            container: host, branch: gridB, members: members, fence: stamp,",
            "            header: 'group', stickyHeader: true,",
            "            columnWidths: state.widths,",
            "            folded: state.folded,",
            "            onColumnResized: function () { if (current) state.widths = current.group.columnWidths(); },",
            "            onFolded: function () { if (current) state.folded = foldedIds(current.group); },",
            "            label: 'Risk ladder'",
            "        });",
            "        current = { group: group, relations: relations, fences: fences };",
            "    }",
            "    function foldedIds(group) { return group.members().filter(function (id) { return group.folded(id); }); }",
            "    function teardown() {",
            "        var c = current;",
            "        if (!c) return;",
            "        state.widths = c.group.columnWidths();",
            "        state.folded = foldedIds(c.group);",
            "        current = null;",
            "        c.group.destroy();",
            "        c.fences.forEach(function (f) { f.dispose(); });",
            "        c.relations.forEach(function (r) { r.dispose(); });",
            "        branch.dissolveBranch('grid');",
            "        branch.dissolveBranch('domain');",
            "    }",
            "    function rebuild() { teardown(); build(); renderScope(); }",
            "",
            "    // FOCUS is a fold, not a filter: every other pair collapses to its",
            "    // caption, the book total stays, every fence still answers a press.",
            "    function applyFocus() {",
            "        if (!current) return;",
            "        var g = current.group;",
            "        if (!state.focus) { g.foldAll(false); return; }",
            "        g.foldAll(true);",
            "        g.fold(state.focus, false);",
            "        g.fold(state.focus + '/\\u03a3', false);",
            "        g.fold('book', false);",
            "        g.activate(state.focus);",
            "    }",
            "",
            "    var scopeBranch = null;",
            "    function renderScope() {",
            "        if (scopeBranch) scopeBranch.dissolve();",
            "        scopeBranch = branch.createBranch('scope-strip');",
            "        scopeBranch.activate(scopeSlot);",
            "        if (state.book) {",
            "            scopeSlot.appendChild(fdk.chip(scopeBranch, 'book-chip', 'neutral', '\\ud83d\\udcc1 ' + state.book.label));",
            "            var dropBook = fdk.el(scopeBranch, 'clear-book', 'button', fd_btn_ghost, 'all books');",
            "            dropBook.onclick = function () { state.book = null; rebuild(); applyFocus(); };",
            "            scopeSlot.appendChild(dropBook);",
            "        }",
            "        if (state.focus) {",
            "            scopeSlot.appendChild(fdk.chip(scopeBranch, 'pair-chip', 'neutral', state.focus));",
            "            var dropPair = fdk.el(scopeBranch, 'clear-pair', 'button', fd_btn_ghost, 'all pairs');",
            "            dropPair.onclick = function () { state.focus = null; renderScope(); applyFocus(); };",
            "            scopeSlot.appendChild(dropPair);",
            "        }",
            "        if (!state.book && !state.focus) {",
            "            scopeSlot.appendChild(fdk.chip(scopeBranch, 'pair-chip', 'neutral', 'whole book'));",
            "        }",
            "    }",
            "",
            "    var party = (workspaceCtx && workspaceCtx.deskParty) ? workspaceCtx.deskParty : null;",
            "    var actorId = null;",
            "    if (party) {",
            "        actorId = fdk.actorId('trader/ladder-group', branch);",
            "        party.joinActor({",
            "            id: actorId, parentSecretary: 'desk',",
            "            reactors: {",
            "                InstrumentChanged: function (msg) {",
            "                    var ins = msg.instrument || {};",
            "                    if (!ins.pair || ins.pair === state.focus) return;",
            "                    state.focus = ins.pair;",
            "                    renderScope();",
            "                    applyFocus();",
            "                },",
            "                PortfolioChanged: function (msg) {",
            "                    var pf = msg.portfolio || {};",
            "                    if (!pf.pairs) return;",
            "                    // The member list IS the portfolio: a new group over its pairs.",
            "                    state.book = { label: pf.label, pairs: pf.pairs };",
            "                    if (current) { rebuild(); applyFocus(); }",
            "                }",
            "            }",
            "        });",
            "        party.tellFrom(actorId, { kind: 'CurrentInstrumentRequested' });",
            "        party.tellFrom(actorId, { kind: 'CurrentPortfolioRequested' });",
            "    }",
            "",
            "    feed.start(function () { build(); renderScope(); applyFocus(); });",
            "",
            "    return {",
            "        root: root,",
            "        setActive: function (active) {},",
            "        partyDeregister: function () {",
            "            feed.stop();",
            "            teardown();",
            "            if (party && actorId) { party.leave(actorId); actorId = null; }",
            "        }",
            "    };"
        );
    }
}
