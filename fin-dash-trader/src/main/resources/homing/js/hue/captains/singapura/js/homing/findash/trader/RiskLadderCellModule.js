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
// The framework appends the `export { ladderCell }` from exports().
var ladderCell = function (column, value) {
    return new LadderCell(column);
};

function LadderCell(column) {
    this._column = column;
    this._el = null;
    this._value = null;
}

LadderCell.prototype.render = function (host, value) {
    this._el = host;
    // The grid mints a bare div; the fin-dash type scale is applied here.
    css.setClass(host, fd_dense);
    if (this._column !== 'tenor') css.addClass(host, fd_num);
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
    css.removeClass(el, fd_total_row);
    css.removeClass(el, fd_grand_row);

    // The ROW treatment goes on EVERY cell of an aggregate row. The grid owns
    // the <tr>, so a band across the row is drawn by its cells agreeing — and
    // because .hgr-td carries padding:0, this element fills the cell exactly.
    // Weight alone was not enough: with only the label emboldened, a subtotal
    // read as one more tenor once the eye was in the numbers.
    if (value != null && value.rowKind === 'subtotal') css.addClass(el, fd_total_row);
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
        if (value.agg) css.addClass(el, fd_strongest);
        if (value.leaf) css.addClass(el, fd_row_indent);
        text = value.text;
    } else if (value.kind === 'fresh') {
        // P2 — past its budget, staleness is a state with a mark, never a tint
        // alone. The number stays visible so the desk can see how stale.
        if (value.state === 'over') css.addClass(el, fd_status_serious);
        else if (value.state === 'warn') css.addClass(el, fd_status_warn);
        else css.addClass(el, fd_muted);
        text = (value.state === 'ok' ? '' : '▲ ') + value.text;
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
