// TradeRowModule — one line of the trade journal.
//
// The blotter decides which trades to show and what is selected; this decides
// what one of them looks like. Everything it needs arrives in `o`, so the row
// has no opinion about filters, groups, or the party bus:
//
//   o.branch    the branch that owns the row's elements (a per-render
//               sub-branch, so the caller can dissolve the whole list at once)
//   o.key       unique within the render pass — every element name is built
//               from it, so two trades never claim the same name
//   o.trade     the journal entry
//   o.open      true when this trade's amendment history is unfolded
//   o.selected  true when this is the current selection
//   o.onSelect  called with (trade, amended) on click
//
// The framework appends the `export { tradeRow }` from exports().
var tradeRow = function (o) {
    var b = o.branch, k = o.key, t = o.trade;
    var amended = t.amendments.length > 0;

    var wrap = fdk.el(b, 'tr-' + k, 'div', fd_rule);
    var line = fdk.el(b, 'tr-line-' + k, 'div', [fd_row, fd_clickable]);

    // P2 — an amended trade says so with an icon and a count, never colour alone.
    line.appendChild(amended
        ? fdk.chip(b, 'tr-chip-' + k, 'warn', 'amended (' + t.amendments.length + ')')
        : fdk.chip(b, 'tr-chip-' + k, 'good', 'booked'));

    var main = fdk.el(b, 'tr-main-' + k, 'div', fd_spacer);
    var top = fdk.el(b, 'tr-top-' + k, 'div', null);
    top.appendChild(fdk.el(b, 'tr-meta-' + k, 'span', [fd_caption, fd_muted, fd_num],
        t.time + ' · ' + t.id + ' · ' + t.trader + '  '));
    top.appendChild(fdk.el(b, 'tr-ticket-' + k, 'span', [fd_strong, fd_mono], t.ticket));
    main.appendChild(top);

    var sub = fdk.el(b, 'tr-sub-' + k, 'div', fd_header_row);
    sub.appendChild(fdk.el(b, 'tr-pv-' + k, 'span', fd_caption,
        t.notional + ' · PV at booking ' + t.pvAtBooking));
    // P1 — the slice it was priced on travels with the trade.
    sub.appendChild(fdk.stamp(b, 'tr-stamp-' + k, { slice: t.stamp }));
    main.appendChild(sub);
    line.appendChild(main);

    if (amended) {
        line.appendChild(fdk.el(b, 'tr-hist-' + k, 'span', [fd_caption, fd_muted],
            o.open ? '▾ history' : '▸ history'));
    }
    if (o.selected) css.addClass(line, fd_selected);
    line.onclick = function () { o.onSelect(t, amended); };
    wrap.appendChild(line);

    if (amended && o.open) {
        for (var a = 0; a < t.amendments.length; a++) {
            var am = t.amendments[a];
            var box = fdk.el(b, 'am-' + k + '-' + a, 'div',
                [fd_panel, fd_status_warn_bg, fd_caption]);
            box.appendChild(fdk.el(b, 'am-who-' + k + '-' + a, 'div',
                [fd_strong, fd_status_warn], am.when + ' · ' + am.who));
            box.appendChild(fdk.el(b, 'am-what-' + k + '-' + a, 'div', null, am.what));
            box.appendChild(fdk.el(b, 'am-pnl-' + k + '-' + a, 'div', null,
                am.pnlImpact + ' · ' + am.approval));
            wrap.appendChild(box);
        }
    }
    return wrap;
};
