// SmileChartModule — the W2 smile chart: fitted curve vs raw market quotes per
// pillar, in delta space (10ΔP · 25ΔP · ATM · 25ΔC · 10ΔC).
//
// Dataviz specs: 2px round-joined fit line (series blue), ≥8px market dots
// with a 2px surface ring, hairline solid gridlines, muted axis text, text in
// text tokens (never the series color). Overridden pillars get the warning
// status color + a label — icon + label, never color alone.
//
// smileChart(opts): { width?, height?, pillars: [{ label, mkt, fit, ovr? }] }
// returns an <svg> element. Pure DOM builder (createElementNS), no lookups.
// The framework appends the `export { smileChart }` from exports().
function smileChart(opts) {
    var NS = 'http://www.w3.org/2000/svg';
    var W = opts.width || 360, H = opts.height || 180;
    var PADL = 34, PADR = 12, PADT = 12, PADB = 24;
    var BLUE = '#2a78d6', MUTED = '#898781', GRID = '#e1e0d9',
        SURF = '#fcfcfb', INK = '#52514e', WARN = '#9a6b1f';

    function sel(tag, attrs) {
        var e = document.createElementNS(NS, tag);
        for (var k in attrs) e.setAttribute(k, attrs[k]);
        return e;
    }

    var pillars = opts.pillars;
    var lo = Infinity, hi = -Infinity;
    for (var i = 0; i < pillars.length; i++) {
        var p = pillars[i];
        var vals = [p.mkt, p.fit, p.ovr != null ? p.ovr : p.fit];
        for (var j = 0; j < vals.length; j++) {
            if (vals[j] < lo) lo = vals[j];
            if (vals[j] > hi) hi = vals[j];
        }
    }
    var span = Math.max(0.4, hi - lo);
    lo -= span * 0.15; hi += span * 0.15;

    var innerW = W - PADL - PADR, innerH = H - PADT - PADB;
    function x(i) { return PADL + (pillars.length === 1 ? innerW / 2 : i * innerW / (pillars.length - 1)); }
    function y(v) { return PADT + (hi - v) / (hi - lo) * innerH; }

    var svg = sel('svg', { width: W, height: H, viewBox: '0 0 ' + W + ' ' + H });
    svg.style.cssText = 'display:block;background:' + SURF + ';border:1px solid ' + GRID
        + ';border-radius:8px;font-family:system-ui,sans-serif;';

    // hairline gridlines + y ticks (clean numbers)
    var step = span > 2 ? 1 : 0.5;
    var first = Math.ceil(lo / step) * step;
    for (var g = first; g <= hi; g += step) {
        svg.appendChild(sel('line', { x1: PADL, x2: W - PADR, y1: y(g), y2: y(g),
            stroke: GRID, 'stroke-width': 1 }));
        var tick = sel('text', { x: PADL - 5, y: y(g) + 3.5, 'text-anchor': 'end',
            'font-size': 9.5, fill: MUTED });
        tick.textContent = g.toFixed(step < 1 ? 1 : 0);
        svg.appendChild(tick);
    }

    // fit line — 2px, round join/cap, series blue
    var d = '';
    for (i = 0; i < pillars.length; i++) {
        d += (i === 0 ? 'M' : 'L') + x(i).toFixed(1) + ' ' + y(pillars[i].fit).toFixed(1) + ' ';
    }
    svg.appendChild(sel('path', { d: d, fill: 'none', stroke: BLUE, 'stroke-width': 2,
        'stroke-linejoin': 'round', 'stroke-linecap': 'round' }));

    // market dots — r 4.5 with 2px surface ring; overridden pillar in warning
    for (i = 0; i < pillars.length; i++) {
        var pt = pillars[i];
        var overridden = pt.ovr != null;
        svg.appendChild(sel('circle', { cx: x(i), cy: y(pt.mkt), r: 4.5,
            fill: overridden ? '#fab219' : INK, stroke: SURF, 'stroke-width': 2 }));
        // x label (delta space); overridden gets the ▲ icon + warning ink
        var lbl = sel('text', { x: x(i), y: H - 8, 'text-anchor': 'middle',
            'font-size': 9.5, fill: overridden ? WARN : MUTED,
            'font-weight': overridden ? 600 : 400 });
        lbl.textContent = (overridden ? '▲ ' : '') + pt.label;
        svg.appendChild(lbl);
    }

    // legend (two series: market dots, fit line) — always present for ≥2 series
    var legend = sel('g', {});
    var ly = PADT + 2;
    legend.appendChild(sel('circle', { cx: W - PADR - 96, cy: ly + 3, r: 4,
        fill: INK, stroke: SURF, 'stroke-width': 2 }));
    var t1 = sel('text', { x: W - PADR - 88, y: ly + 6.5, 'font-size': 9.5, fill: MUTED });
    t1.textContent = 'market';
    legend.appendChild(t1);
    legend.appendChild(sel('line', { x1: W - PADR - 46, x2: W - PADR - 30,
        y1: ly + 3, y2: ly + 3, stroke: BLUE, 'stroke-width': 2, 'stroke-linecap': 'round' }));
    var t2 = sel('text', { x: W - PADR - 26, y: ly + 6.5, 'font-size': 9.5, fill: MUTED });
    t2.textContent = 'fit';
    legend.appendChild(t2);
    svg.appendChild(legend);

    return svg;
}
