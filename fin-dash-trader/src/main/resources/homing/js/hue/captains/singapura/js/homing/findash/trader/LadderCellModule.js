// LadderCellModule — a verdict that draws itself. RFC 0050 · Episode 2.
//
// RelGridCellContract, the read-only half only: cellElement() minted once on
// the branch this cell was handed, onSelect(mode) as pure lifecycle, and
// dispose() which is the owner's. Every column of the ladder is declared
// read-only by its relations, so the grid never asks mayTakeControl,
// editorElement or takeControl — they are left off, as the KT says (§9.4).
//
// `set(verdict)` is ours, not the contract's: the relation calls it when the
// feed ticks, and the grid is never told. The verdict is the same value the
// Episode 1 ladder shipped — { kind, n, text, state, agg, rowKind } — judged
// upstream by the desk (freshState, budgetState) and relayed, never decided
// here (P5).
//
// What is GONE from the Episode 1 cell, and why: VoidCell (a section row no
// longer exists — the caption is a fence), the row-band-by-agreement (a
// one-row member's only row IS the row, so the band is this cell's class),
// getValueToCopy's blanking (a selection lives in one member, so a subtotal
// cannot be copied beside the rows it totals), and the eight-class reset
// per paint stays only because a cell outlives its value.
//
// The framework appends `export { LadderCell }` from exports().

class LadderCell {

    constructor(opts) {
        opts = opts || {};
        if (!opts.branch) throw new Error('[LadderCell] opts.branch is required: the cell\'s own');
        this._branch = opts.branch;
        this._branch.activate(this);
        this._el = null;
        this._column = opts.column || null;
        this._value = opts.value || null;
        this._mode = 'none';
    }

    toString() { return 'LadderCell ' + (this._column || '?'); }

    /** The cell's element, minted once on its branch; the grid places it. */
    cellElement() {
        if (!this._el) {
            this._el = this._branch.createElement('cell', 'div');
            css.setClass(this._el, fd_dense);
            css.addClass(this._el, fd_cell_compact);
            if (this._column !== 'tenor') css.addClass(this._el, fd_num);
            this._paint();
        }
        return this._el;
    }

    set(value) { this._value = value || null; this._paint(); return this; }
    value()    { return this._value; }

    _paint() {
        var el = this._el, value = this._value;
        if (!el) return;

        // A cell outlives its value, and a row that recovers must stop reading
        // as a breach.
        css.removeClass(el, fd_status_warn);
        css.removeClass(el, fd_status_serious);
        css.removeClass(el, fd_strongest);
        css.removeClass(el, fd_muted);
        css.removeClass(el, fd_row_indent);
        css.removeClass(el, fd_total_row);
        css.removeClass(el, fd_grand_row);

        // The row treatment is this cell's own class: in a one-row member every
        // cell is that row, so nothing has to agree with anything.
        if (value && value.rowKind === 'subtotal') css.addClass(el, fd_total_row);
        else if (value && value.rowKind === 'grand') css.addClass(el, fd_grand_row);

        var text;
        if (value == null || value.kind === 'none') {
            // A column the desk does not publish: the honest dash, never a sum
            // to fill it (P5) — and the row band stays, so a partial total does
            // not read as a broken one.
            css.addClass(el, fd_muted);
            text = value == null ? '—' : value.text;
        } else if (value.kind === 'label') {
            if (value.agg)    css.addClass(el, fd_strongest);
            if (value.indent) css.addClass(el, fd_row_indent);
            text = value.text;
        } else if (value.kind === 'fresh') {
            // Only the degraded state takes colour (P2); the healthy mark is
            // still drawn, because a blank reads as "no reading" as easily as
            // "fine". The marks are the kit's vocabulary, not this widget's.
            if (value.state === 'stale') { css.addClass(el, fd_status_warn); text = '▲ ' + value.text; }
            else                         { css.addClass(el, fd_muted);       text = '● ' + value.text; }
        } else {
            if (value.state === 'over')      css.addClass(el, fd_status_serious);
            else if (value.state === 'warn') css.addClass(el, fd_status_warn);
            text = (value.state === 'over' ? '◆ ' : '') + value.text;
        }
        el.textContent = text;
    }

    /** Pure lifecycle: 'none' | 'shallow' | 'deep'. The look does not change. */
    onSelect(mode) { this._mode = mode; }
    mode()         { return this._mode; }

    /** The owner's, never the grid's. */
    dispose() { this._el = null; this._branch.dissolve(); }
}
