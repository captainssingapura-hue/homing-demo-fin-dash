// RiskLadderCellModule — the cells of the risk ladder.
//
// RelationGrid hands cell ownership to the application: `cellFactory(column,
// value, meta)` mints one, and `render(el, value)` gets a real element on the
// grid's cells branch. So everything a desk cell needs — tabular numerals, a
// subtotal that reads as a subtotal, a freshness state that is a state and not
// a colour — is ours to write, not the grid's to configure.
//
// One design constraint shapes what follows: the grid calls `ensure(pk, col,
// factory, value)` with no `meta`, so a cell never learns which ROW it is in.
// It cannot ask "is this tenor's residual over ITS tolerance?". The adapter
// answers that instead and passes the verdict down inside the value:
//
//     { kind: 'num', n: 1100000, text: '1.10M', state: 'ok' | 'warn' | 'over' }
//
// The cell renders a decision someone else made. That is the right split: the
// adapter is the seam onto the domain, the cell is presentation.
//
// The factory picks the CELL CLASS per cell, which is where a void cell comes
// from — the grid needs no notion of one. A section-header row has a name and
// nothing else: "EURUSD ▸ vATM" is not a missing measurement, it is not a
// measurement at all, and a dash there would misreport not-applicable as
// not-available.
//
// Note the choice is permanent: GridCells.ensure mints a cell once per (pk,
// column) and caches it, so voidness must be a property of the ROW KIND — which
// never changes for a given row — and not of whatever the value happens to be
// at first render.
//
// `opts.onToggleSection(scope)` — folding. The grid listens for `click` on its
// own <td> and for nothing else, so a dblclick handler on OUR element competes
// with nobody: the fold gesture is app-owned, like the cell it hangs on.
//
// The framework appends the `export { ladderCell }` from exports().
var ladderCell = function (column, value, opts) {
    if (value && value.kind === 'void') return new VoidCell(opts);
    return new LadderCell(column, opts);
};

/** Fold on double-click, anywhere along the header row — not just its label. */
function foldable(host, value, opts) {
    if (!value || value.rowKind !== 'section' || !opts || !opts.onToggleSection) return;
    host.addEventListener('dblclick', function (ev) {
        ev.preventDefault();
        opts.onToggleSection(value.scope);
    });
}

/**
 * A cell that is structurally present and semantically absent. It renders the
 * section band so the header reads as one strip, answers the contract, and
 * contributes nothing: no text, no value, no copy.
 */
function VoidCell(opts) { this._el = null; this._opts = opts || null; }

VoidCell.prototype.render = function (host, value) {
    this._el = host;
    css.setClass(host, fd_dense);
    css.addClass(host, fd_section_row);
    foldable(host, value, this._opts);
    return this;
};
VoidCell.prototype.update         = function () { return this; };
VoidCell.prototype.onSelect       = function (mode) {};
VoidCell.prototype.effectiveType  = function () { return null; };
VoidCell.prototype.beginEdit      = function () {};
VoidCell.prototype.commitEdit     = function () {};
VoidCell.prototype.cancelEdit     = function () {};
VoidCell.prototype.preview        = function (text) {};
VoidCell.prototype.getValue       = function () { return null; };
VoidCell.prototype.getEditValue   = function () { return null; };
VoidCell.prototype.getValueToCopy = function () { return ''; };
VoidCell.prototype.dispose        = function () { this._el = null; };

function LadderCell(column, opts) {
    this._column = column;
    this._opts = opts || null;
    this._el = null;
    this._value = null;
}

LadderCell.prototype.render = function (host, value) {
    this._el = host;
    // The grid mints a bare div; the fin-dash type scale is applied here.
    css.setClass(host, fd_dense);
    if (this._column !== 'tenor') css.addClass(host, fd_num);
    foldable(host, value, this._opts);
    this._paint(value);
    return this;
};

LadderCell.prototype.update = function (value) { this._paint(value); return this; };

LadderCell.prototype._paint = function (value) {
    var el = this._el;
    if (!el) return;
    this._value = value;

    // Reset the state classes every paint — a cell outlives its value, and a
    // row that recovers must stop reading as a breach.
    css.removeClass(el, fd_status_warn);
    css.removeClass(el, fd_status_serious);
    css.removeClass(el, fd_strongest);
    css.removeClass(el, fd_muted);
    css.removeClass(el, fd_row_indent);
    css.removeClass(el, fd_row_mark);
    css.removeClass(el, fd_total_row);
    css.removeClass(el, fd_grand_row);
    css.removeClass(el, fd_section_row);

    // The ROW treatment goes on EVERY cell of an aggregate row. The grid owns
    // the <tr>, so a band across the row is drawn by its cells agreeing — and
    // because .hgr-td carries padding:0, this element fills the cell exactly.
    // Weight alone was not enough: with only the label emboldened, a subtotal
    // read as one more tenor once the eye was in the numbers.
    if (value != null && value.rowKind === 'section') css.addClass(el, fd_section_row);
    else if (value != null && value.rowKind === 'subtotal') css.addClass(el, fd_total_row);
    else if (value != null && value.rowKind === 'grand') css.addClass(el, fd_grand_row);

    // The text is computed, then assigned once. A cell holds no child
    // elements, so there is nothing to clear first — and clearing by
    // assigning '' is a wholesale DOM wipe the rules (rightly) refuse.
    var text;

    if (value == null || value.kind === 'none') {
        // A column the desk does not publish. The dash is the honest answer —
        // the ladder will not sum the rows to fill it (P5) — and the cell still
        // wears its row band, so a partial total does not read as a broken one.
        css.addClass(el, fd_muted);
        text = value == null ? '—' : value.text;
    } else if (value.kind === 'label') {
        // An aggregate announces itself by weight; a leaf sits indented under
        // the block it belongs to. Neither relies on position alone, because a
        // filtered view can put any row first.
        //
        // A MARK is the third case: Σ is not an answer to "which tenor", so it
        // leaves the column's reading edge. Flush left, under 1W / 1M / 3M, it
        // read as a fourth tenor rather than as the line that closes them.
        if (value.agg) css.addClass(el, fd_strongest);
        if (value.leaf) css.addClass(el, fd_row_indent);
        if (value.mark) css.addClass(el, fd_row_mark);
        text = value.text;
    } else if (value.kind === 'fresh') {
        // Freshness is the column that says whether the rest of the row can be
        // trusted, so all three states are marked — including the good one. A
        // blank cell is ambiguous: it reads as "no reading" as easily as "fine".
        //
        // The marks are the kit's own status vocabulary (● healthy, ▲ degraded,
        // ◆ serious), not a set this widget invented — a ▲ means the same thing
        // here as on every chip in the desk.
        //
        // Only the degraded states take colour. Eleven of fifteen rows are
        // healthy, and tinting them all would spend the eye's attention on the
        // rows that do not need it — and compete with the ◆ breach marks in the
        // number columns, which are what a trader is actually scanning for
        // (P2: degraded is loud, not everything is loud).
        if (value.state === 'stale') {
            css.addClass(el, fd_status_warn);
            text = '▲ ' + value.text;
        } else {
            css.addClass(el, fd_muted);
            text = '● ' + value.text;
        }
    } else {
        // Numbers. A breach carries a mark as well as a class, same reason.
        if (value.state === 'over')      css.addClass(el, fd_status_serious);
        else if (value.state === 'warn') css.addClass(el, fd_status_warn);
        // (the row treatment above already carries the aggregate weight)
        text = (value.state === 'over' ? '◆ ' : '') + value.text;
    }

    el.textContent = text;
};

// ── the rest of the cell contract ────────────────────────────────────────────
// The ladder is read-only: it reports risk the desk already owns (P5), so the
// edit half of the contract is inert. getValueToCopy still answers honestly,
// because copying a ladder into a mail is a real desk gesture.

LadderCell.prototype.onSelect        = function (mode) {};
LadderCell.prototype.effectiveType   = function () { return null; };
LadderCell.prototype.beginEdit       = function () {};
LadderCell.prototype.commitEdit      = function () {};
LadderCell.prototype.cancelEdit      = function () {};
LadderCell.prototype.preview         = function (text) {};
LadderCell.prototype.getValue        = function () {
    return this._value && this._value.n != null ? this._value.n : null;
};
LadderCell.prototype.getEditValue    = function () { return this.getValue(); };
LadderCell.prototype.getValueToCopy  = function () {
    if (!this._value) return '';
    // An aggregate's NUMBER never leaves as data. A ladder copied into a mail
    // or a sheet is a real desk gesture, and a subtotal pasted alongside the
    // rows it totals is a column that sums to twice the book. The label still
    // copies, so the block stays readable — it just cannot be added up wrong.
    if (this._value.agg && this._value.kind !== 'label') return '';
    return String(this._value.text);
};
LadderCell.prototype.dispose         = function () { this._el = null; };
