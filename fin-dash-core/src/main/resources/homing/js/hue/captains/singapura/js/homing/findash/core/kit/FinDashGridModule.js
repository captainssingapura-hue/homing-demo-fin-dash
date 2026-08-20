// FinDashGridModule — the dense-blotter grid the trader/MO screens are built
// on (UI study NFR: 30+ rows without scrolling, tabular numerals, keyboardable
// later). Column-spec driven; numeric columns right-aligned with tabular-nums;
// selection highlight; per-row click callback for party-bus publication.
//
// fdGrid(branch, opts):
//   columns : [{ key, label, align?('right'), render?(value,row,rowBranch,i)->Node }]
//   rows    : [ { ...cells, _indent?, _group? } ]
//   onRowClick?(row, tr)
//   compact?: tighter cell padding
// returns { root, setRows(rows) }
//
// Consumer discipline: elements come from the caller's branch, styling is
// typed CSS classes, selection is a class rather than a style write.
//
// Rows live in a SUB-BRANCH. branch.createElement throws on a duplicate name,
// so a grid that re-renders cannot simply rebuild rows under the same names —
// setRows dissolves the row branch (which detaches every owned element) and
// creates a fresh one. That is also why the render callback is handed the row
// branch: cells a widget builds must be owned by the branch that gets dissolved,
// or the second setRows would collide.
//
// The framework appends the `export { fdGrid }` from exports().
function fdGrid(branch, opts) {
    var root = branch.createElement('grid-root', 'div');
    css.setClass(root, fd_scroll);

    var table = branch.createElement('grid-table', 'table');
    css.setClass(table, fd_table);
    root.appendChild(table);

    var thead = branch.createElement('grid-thead', 'thead');
    var hr = branch.createElement('grid-head-row', 'tr');
    for (var c = 0; c < opts.columns.length; c++) {
        var col = opts.columns[c];
        var th = branch.createElement('grid-th-' + c, 'th');
        css.setClass(th, fd_th);
        if (col.align === 'right') css.addClass(th, fd_td_num);
        if (opts.compact) css.addClass(th, fd_cell_compact);
        th.textContent = col.label;
        hr.appendChild(th);
    }
    thead.appendChild(hr);
    table.appendChild(thead);

    var tbody = branch.createElement('grid-tbody', 'tbody');
    table.appendChild(tbody);

    var rowBranch = null;
    var selectedTr = null;

    function buildRow(b, row, i) {
        var tr = b.createElement('r' + i, 'tr');
        css.setClass(tr, fd_rule);
        if (row._group) css.addClass(tr, fd_row_group);
        for (var c = 0; c < opts.columns.length; c++) {
            var col = opts.columns[c];
            var td = b.createElement('r' + i + '-c' + c, 'td');
            css.setClass(td, fd_td);
            if (col.align === 'right') css.addClass(td, fd_td_num);
            if (opts.compact) css.addClass(td, fd_cell_compact);
            if (c === 0 && row._indent) css.addClass(td, fd_row_indent);
            var v = row[col.key];
            if (col.render) {
                var node = col.render(v, row, b, i);
                if (node != null) td.appendChild(node);
            } else if (v != null) {
                td.textContent = v;
            }
            tr.appendChild(td);
        }
        if (opts.onRowClick) {
            css.addClass(tr, fd_clickable);
            css.addClass(tr, fd_clickable_hover);
            tr.onclick = function () {
                if (selectedTr) css.removeClass(selectedTr, fd_selected);
                selectedTr = tr;
                css.addClass(tr, fd_selected);
                opts.onRowClick(row, tr);
            };
        }
        return tr;
    }

    function setRows(rows) {
        // dissolve() detaches every element the row branch owns — the
        // sanctioned teardown, and why no removeChild loop is needed.
        if (rowBranch) rowBranch.dissolve();
        rowBranch = branch.createBranch('grid-rows');
        rowBranch.activate(root);
        selectedTr = null;
        for (var i = 0; i < rows.length; i++) {
            tbody.appendChild(buildRow(rowBranch, rows[i], i));
        }
    }

    setRows(opts.rows || []);

    return { root: root, setRows: setRows };
}
