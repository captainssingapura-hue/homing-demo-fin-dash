// RiskLadderRowsModule — the ladder's ROWS, and the value of every cell in them.
//
// Split out of RiskLadderWidget when that module crossed the 250 effective-line
// gate. The seam is not arbitrary: everything here is the shape of the table —
// which rows exist, in what order, and what each cell of each row means — and
// nothing here touches the DOM, the grid, the bus or the widget's view state.
// What stays behind is the widget: loading, the adapter, focus and folding,
// the chips, the party.
//
// Worth naming for anyone copying this: ALL of it exists because a grid
// displays exactly one relation and the ladder needs several — a detail block
// per pair, a subtotal closing each, the book closing the table. See
// docs/homologous-relations.md; if that facility lands, most of this file
// stops existing rather than moving somewhere else.
//
// The framework appends `export { ladderRows, ladderCellValue }` from exports().

// ── the four row kinds ───────────────────────────────────────────────────────
//   section   a pair's header                     scope: that pair
//   leaf      one tenor of one pair               scope: that pair
//   subtotal  one pair, all tenors                scope: that pair
//   grand     the book                            scope: null (everything)
//
// The kind rides on the row record — never parsed back out of the pk string.
// A pk is an identity; asking it what sort of thing it is would be the same
// mistake as reading a machine field out of a display slot. The section row's
// identity is THE PAIR — a real domain entity, not a presentation row invented
// for the eye. That is what earns it a pk.
function section(pair)     { return { pk: pair, kind: 'section',
                                      scope: pair, label: pair, data: {} }; }
function leaf(pair, r)     { return { pk: pair + '/' + r.tenor, kind: 'leaf',
                                      scope: pair, label: r.tenor, data: r }; }
// The subtotal does not repeat the pair: the header above already names it, and
// repeating it made the name the loudest thing in the block — read twice,
// needed once. A bare Σ was tried and read as a stray glyph in a column of
// words. The grand total names its scope because nothing above it declares one.
function subtotal(pair, r) { return { pk: pair + '/Σ', kind: 'subtotal',
                                      scope: pair, label: 'Subtotal', data: r }; }
function grand(r)          { return { pk: 'ΣΣ', kind: 'grand',
                                      scope: null, label: 'FXO book total', data: r }; }

function isAggregate(row) { return row.kind === 'subtotal' || row.kind === 'grand'; }

// A column the desk does not publish is still a CELL of its row: it must wear
// the row's band or the total reads as broken rather than partial. 'not
// published' is a value, not the absence of one — and the ladder will not sum
// the rows to fill the gap (P5).
function absent(rk) { return { kind: 'none', text: '—', rowKind: rk, agg: true }; }

/**
 * ORDER. Aggregates sort last WITHIN THEIR SCOPE — a pair's subtotal closes its
 * own block, the grand total closes the table. Last-overall would tear each
 * subtotal away from the rows it totals, which reads as a mistake rather than
 * a convention.
 */
var ladderRows = function (d) {
    var pairs = [], seen = {}, all = d.rows || [], i;
    for (i = 0; i < all.length; i++) {
        if (!seen[all[i].pair]) { seen[all[i].pair] = 1; pairs.push(all[i].pair); }
    }
    var out = [];
    for (var p = 0; p < pairs.length; p++) {
        var pair = pairs[p], sub = null;
        out.push(section(pair));                 // opens its scope
        for (i = 0; i < all.length; i++) {
            if (all[i].pair !== pair) continue;
            if (all[i].group) sub = all[i]; else out.push(leaf(pair, all[i]));
        }
        if (sub) out.push(subtotal(pair, sub));   // last within its scope
    }
    if (d.totals) out.push(grand(d.totals));      // last overall
    return out;
};

/**
 * Every per-row judgement is made HERE, because a cell is never told which row
 * it is in. The value carries the verdict — and the row kind, so the cell can
 * render weight and refuse to be copied as data.
 *
 * `folded` is passed rather than read: this module holds no view state, so the
 * one thing it needs from the widget's (which sections are shut, for the caret)
 * arrives as an argument.
 */
var ladderCellValue = function (row, col, folded) {
    var r = row.data, agg = isAggregate(row);
    // A section row has a name and nothing else. Every other column is VOID —
    // not a missing measurement but no measurement at all — and the factory
    // turns that into a different cell class entirely. The fold shows in the
    // CARET and nowhere else; the band keeps its face either way, so only the
    // label needs to know.
    if (row.kind === 'section') {
        var open = !(folded || {})[row.scope];
        return col === 'tenor'
            ? { kind: 'label', text: (open ? '▾ ' : '▸ ') + row.label,
                agg: false, leaf: false, rowKind: 'section', scope: row.scope }
            : { kind: 'void', rowKind: 'section', scope: row.scope };
    }
    // rowKind travels with EVERY value so each cell can wear the row's
    // treatment — the grid owns the <tr>, so a band is drawn by its cells
    // agreeing rather than by styling a row that is not ours.
    var rk = row.kind;
    if (col === 'tenor') {
        // Everything under a header is indented under it — the tenors AND the
        // subtotal that closes them, on the same edge, so the closing row reads
        // as part of the block. Only the grand total sits flush: nothing above
        // it to belong to.
        return { kind: 'label', text: row.label, agg: agg,
                 indent: rk !== 'grand', rowKind: rk };
    }
    if (col === 'fresh') {
        var secs = r.freshSecs;
        if (secs == null) return absent(rk);
        // The VERDICT comes from the desk (freshState, beside the number), not
        // from a cut-off this widget invented. One function upstream judges
        // every reading, so this column and the Risk Blotter's can never
        // disagree about the same second (P5).
        return { kind: 'fresh', n: secs, text: secs + 's', agg: agg, rowKind: rk,
                 state: r.freshState };
    }
    var n = r[col];
    if (n == null) return absent(rk);
    // Reval budget is the desk's own trust metric: past it, the number on
    // screen is Taylor-tracked rather than freshly revalued. The desk publishes
    // the band (budgetState); the ladder relays it.
    var st = r.budgetState || 'ok';
    return { kind: 'num', n: n, text: fdk.fmt.compact(n), state: st, agg: agg,
             rowKind: rk };
};
