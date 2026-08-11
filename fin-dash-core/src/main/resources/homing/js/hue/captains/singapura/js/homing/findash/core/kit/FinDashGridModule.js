// FinDashGridModule — the dense-blotter grid the trader/MO screens are built
// on (UI study NFR: 30+ rows without scrolling, tabular numerals, keyboardable
// later). Column-spec driven; numeric columns right-aligned with tabular-nums;
// selection highlight; per-row click callback for party-bus publication.
//
// fdGrid(opts):
//   columns : [{ key, label, align?('right'), width?, render?(value,row)->Node }]
//   rows    : [ { ...cells, _indent?, _group?, _chip? } ]
//   onRowClick?(row, tr)
//   compact?: tighter paddings
// returns { root, setRows(rows), selectRow(predicate) }
//
// Pure DOM builder — createElement + textContent only, no HTML literals.
// The framework appends the `export { fdGrid }` from exports().
function fdGrid(opts) {
    var INK = '#0b0b0b', SOFT = '#52514e', MUTED = '#898781',
        GRID = '#e1e0d9', WASH = '#eef4fc';
    var pad = opts.compact ? '2px 8px' : '4px 10px';

    var root = document.createElement('div');
    root.style.cssText = 'overflow:auto;';

    var table = document.createElement('table');
    table.style.cssText = 'border-collapse:collapse;width:100%;font-size:12px;';
    root.appendChild(table);

    var thead = document.createElement('thead');
    var hr = document.createElement('tr');
    for (var c = 0; c < opts.columns.length; c++) {
        var col = opts.columns[c];
        var th = document.createElement('th');
        th.style.cssText = 'text-align:' + (col.align === 'right' ? 'right' : 'left')
            + ';padding:' + pad + ';font-size:10.5px;font-weight:600;color:' + MUTED
            + ';border-bottom:1px solid ' + GRID + ';white-space:nowrap;'
            + (col.width ? 'width:' + col.width + ';' : '')
            + 'position:sticky;top:0;background:#fcfcfb;';
        th.textContent = col.label;
        hr.appendChild(th);
    }
    thead.appendChild(hr);
    table.appendChild(thead);

    var tbody = document.createElement('tbody');
    table.appendChild(tbody);

    var selectedTr = null;

    function buildRow(row) {
        var tr = document.createElement('tr');
        tr.style.cssText = 'border-bottom:1px solid ' + GRID + ';'
            + (row._group ? 'background:#f4f4f2;font-weight:600;' : '');
        for (var c = 0; c < opts.columns.length; c++) {
            var col = opts.columns[c];
            var td = document.createElement('td');
            var numeric = col.align === 'right';
            td.style.cssText = 'padding:' + pad + ';white-space:nowrap;color:'
                + (row._group ? INK : SOFT) + ';'
                + (numeric ? 'text-align:right;font-variant-numeric:tabular-nums;' : '')
                + (c === 0 && row._indent ? 'padding-left:' + (10 + row._indent * 16) + 'px;' : '');
            var v = row[col.key];
            if (col.render) {
                var node = col.render(v, row);
                if (node != null) td.appendChild(node);
            } else if (v != null) {
                td.textContent = v;
            }
            tr.appendChild(td);
        }
        if (opts.onRowClick) {
            tr.style.cursor = 'pointer';
            tr.onclick = function () {
                if (selectedTr) selectedTr.style.background = '';
                selectedTr = tr;
                tr.style.background = WASH;
                opts.onRowClick(row, tr);
            };
        }
        return tr;
    }

    function setRows(rows) {
        while (tbody.firstChild) tbody.removeChild(tbody.firstChild);
        selectedTr = null;
        for (var i = 0; i < rows.length; i++) tbody.appendChild(buildRow(rows[i]));
    }

    setRows(opts.rows || []);

    return { root: root, setRows: setRows };
}
