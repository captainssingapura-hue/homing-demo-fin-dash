// LadderRelationModule — the ladder's relations. RFC 0050 · Episode 2.
//
// The ladder is not one relation with four kinds of row; it is a STACK of
// relations over one schema — a detail relation per pair, a one-row subtotal
// relation per pair, a one-row relation for the book — and this module makes
// all three from one factory. Which relation a row is in is now its kind;
// nothing is parsed out of a pk, and there is no `kind` field anywhere.
//
// A relation, to the grid, is view() / columns() / cellFor() and a few
// optional readers. The grid holds no value: a cell is ours, we update it
// from the feed with cell.set(), and the grid is never told. Membership is
// ours too — cellFor throws for a stranger, and the grid asks for every
// presented identity BEFORE a slot moves, so a stale tenor refuses the whole
// remap and the previous rows stay (KT §9.3).
//
// verdictOf(row, col) is what Episode 1's ladderCellValue was, minus the
// `folded` argument: the caret it carried is the fence's now.
//
// The framework appends `export { LADDER_COLUMNS, createLadderRelation }`
// from exports().

var LADDER_COLUMNS = Object.freeze(['tenor', 'delta', 'vAtm', 'vRr', 'vBf', 'theta', 'fresh']);
var LADDER_LABELS  = Object.freeze({ tenor: 'Book ▸ tenor', delta: 'Δ', vAtm: 'vATM',
                                     vRr: 'vRR', vBf: 'vBF', theta: 'Θ', fresh: 'freshness' });

function absent(rk) { return { kind: 'none', text: '—', rowKind: rk, agg: true }; }

/**
 * Every per-cell judgement, in one place. `rk` names the relation the row is
 * in ('leaf' | 'subtotal' | 'grand'); `label` is what the tenor column shows.
 */
function verdictOf(row, col, rk, label) {
    var agg = rk !== 'leaf';
    if (col === 'tenor') {
        // Under a caption, the tenors and the subtotal that closes them sit on
        // one edge; only the book total sits flush, with nothing above it.
        return { kind: 'label', text: label, agg: agg, indent: rk !== 'grand', rowKind: rk };
    }
    if (!row) return absent(rk);
    if (col === 'fresh') {
        var secs = row.freshSecs;
        if (secs == null) return absent(rk);
        // The verdict is the desk's (freshState), not a cut-off invented here.
        return { kind: 'fresh', n: secs, text: secs + 's', agg: agg, rowKind: rk, state: row.freshState };
    }
    var n = row[col];
    if (n == null) return absent(rk);
    return { kind: 'num', n: n, text: fdk.fmt.compact(n), state: row.budgetState || 'ok',
             agg: agg, rowKind: rk };
}

/**
 * One factory, three shapes:
 *   createLadderRelation(feed, { pair, kind: 'detail',   branch })  view() = the pair's tenors
 *   createLadderRelation(feed, { pair, kind: 'subtotal', branch })  view() = ['Σ']
 *   createLadderRelation(feed, {       kind: 'book',     branch })  view() = ['book']
 *
 * The branch is the relation's own, handed unactivated; it activates it and
 * mints each cell on a sub-branch of it, so dispose() releases everything at
 * once.
 */
var createLadderRelation = function (feed, opts) {
    opts = opts || {};
    if (!opts.branch) throw new Error('[LadderRelation] opts.branch is required: the relation\'s own');
    var kind = opts.kind || 'detail', pair = opts.pair || null, branch = opts.branch;
    if (kind !== 'book' && !pair) throw new Error('[LadderRelation] a ' + kind + ' relation needs a pair');
    var name = kind === 'book' ? 'book' : pair + (kind === 'subtotal' ? '/Σ' : '');
    branch.activate({ toString: function () { return 'LadderRelation ' + name; } });

    var cells = new Map(), seq = 0;

    // What the rows are, and where a row's data comes from.
    function pks() {
        if (kind === 'detail')   return feed.tenors(pair);
        if (kind === 'subtotal') return ['Σ'];
        return ['book'];
    }
    function rowOf(pk) {
        if (kind === 'detail')   return feed.row(pair, pk);
        if (kind === 'subtotal') return feed.subtotal(pair);
        return feed.totals();
    }
    function labelOf(pk) {
        if (kind === 'detail')   return pk;
        if (kind === 'subtotal') return 'Subtotal';
        return 'FXO book total';
    }
    var rk = kind === 'detail' ? 'leaf' : (kind === 'subtotal' ? 'subtotal' : 'grand');

    // The feed ticks; the cells that belong to a changed row repaint. The
    // book relation listens for the totals, which the feed signals with null.
    var unsubscribe = feed.subscribe(function (row) {
        var touched;
        if (row === null)                  touched = kind === 'book' ? ['book'] : [];
        else if (row.pair !== pair)        touched = [];
        else if (row.group)                touched = kind === 'subtotal' ? ['Σ'] : [];
        else                               touched = kind === 'detail'   ? [row.tenor] : [];
        touched.forEach(function (pk) {
            var data = rowOf(pk);
            LADDER_COLUMNS.forEach(function (col) {
                var c = cells.get(pk + ' ' + col);
                if (c) c.set(verdictOf(data, col, rk, labelOf(pk)));
            });
        });
    });

    return {
        id:      function () { return name; },
        view:    function (intent) { return intent ? null : pks(); },   // one root; a movement goes nowhere
        columns: function () { return LADDER_COLUMNS; },
        labels:  function () { return LADDER_LABELS; },
        readOnlyColumns: function () { return LADDER_COLUMNS; },         // a book is read
        cellFor: function (pk, col) {
            if (pks().indexOf(pk) < 0) throw new Error('[LadderRelation] ' + name + ' has no row ' + pk);
            var k = pk + ' ' + col, c = cells.get(k);
            if (!c) {
                c = new LadderCell({ branch: branch.createBranch('c' + (++seq)), column: col,
                                     value: verdictOf(rowOf(pk), col, rk, labelOf(pk)) });
                cells.set(k, c);
            }
            return c;
        },
        cellCount: function () { return cells.size; },
        dispose: function () {
            unsubscribe();
            cells.forEach(function (c) { c.dispose(); });
            cells.clear();
            branch.dissolve();
        }
    };
};
