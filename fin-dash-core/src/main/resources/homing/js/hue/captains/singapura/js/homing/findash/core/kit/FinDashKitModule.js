// FinDashKitModule — the fin-dash UI kit: theme tokens, element/format helpers,
// and the two cross-cutting patterns every widget carries (UI study P1 + P2):
// the status chip (state is visible, never by color alone — icon + label) and
// the lineage stamp (every number can explain itself).
//
// Palette: the dataviz reference instance (validated). Status colors are
// reserved for state and never used as series colors.
//
// Pure DOM builders — no branch access, no lookups, no HTML literals.
// The framework appends the `export { fdk }` from exports().
var fdk = {

    tokens: {
        ink:      '#0b0b0b',
        soft:     '#52514e',
        muted:    '#898781',
        grid:     '#e1e0d9',
        baseline: '#c3c2b7',
        surface:  '#fcfcfb',
        panel:    '#f4f4f2',
        accent:   '#2a78d6',   // categorical slot 1 — the kit's single series hue
        accentSoft: '#cde2fb',
        mono: 'ui-monospace, Menlo, Consolas, monospace'
    },

    // Status palette (reserved; icon + label always — P2, never color alone).
    status: {
        good:     { icon: '●', color: '#006300', bg: '#e6f4e6', label: 'healthy'  },
        live:     { icon: '●', color: '#006300', bg: '#e6f4e6', label: 'live'     },
        warn:     { icon: '▲', color: '#9a6b1f', bg: '#fdf3dc', label: 'degraded' },
        serious:  { icon: '◆', color: '#a8502a', bg: '#fceee8', label: 'serious'  },
        critical: { icon: '✕', color: '#a32e2e', bg: '#f9e4e4', label: 'critical' },
        neutral:  { icon: '○', color: '#52514e', bg: '#efefec', label: 'idle'     }
    },

    el: function (tag, css, text) {
        var d = document.createElement(tag);
        if (css) d.style.cssText = css;
        if (text != null) d.textContent = text;
        return d;
    },

    fmt: {
        // 1234567 -> '1.23M', 486000 -> '486k' (blotter density)
        compact: function (x) {
            var neg = x < 0, a = Math.abs(x), s;
            if      (a >= 1e9) s = (a / 1e9).toFixed(2) + 'B';
            else if (a >= 1e6) s = (a / 1e6).toFixed(a >= 1e7 ? 1 : 2) + 'M';
            else if (a >= 1e3) s = Math.round(a / 1e3) + 'k';
            else               s = String(Math.round(a));
            return (neg ? '−' : '') + s;
        },
        num: function (x, dp) { return x.toFixed(dp == null ? 2 : dp); },
        signed: function (x, dp) {
            var s = fdk.fmt.num(Math.abs(x), dp);
            return (x < 0 ? '−' : '+') + s;
        },
        // thousands-comma'd integer (deal amounts)
        amount: function (x) {
            var neg = x < 0, s = String(Math.round(Math.abs(x))), out = '';
            while (s.length > 3) { out = ',' + s.slice(-3) + out; s = s.slice(0, -3); }
            return (neg ? '−' : '') + s + out;
        },
        pct: function (x, dp) { return fdk.fmt.num(x, dp == null ? 1 : dp) + '%'; }
    },

    // P2 — the shared status chip: icon + label, never color alone.
    chip: function (state, text) {
        var s = fdk.status[state] || fdk.status.neutral;
        return fdk.el('span',
            'display:inline-block;font-size:11px;font-weight:600;color:' + s.color
            + ';background:' + s.bg + ';border-radius:999px;padding:2px 10px;'
            + 'white-space:nowrap;',
            s.icon + ' ' + (text != null ? text : s.label));
    },

    // P1 — the lineage stamp: slice / surface epoch / model, always visible.
    // info: { slice, surface, model, note? } — title attr is the hover explain.
    stamp: function (info) {
        var parts = [];
        if (info.slice)   parts.push('slice ' + info.slice);
        if (info.surface) parts.push('surface ' + info.surface);
        if (info.model)   parts.push('model ' + info.model);
        var d = fdk.el('span', 'color:#898781;font-size:11px;white-space:nowrap;',
            parts.join(' · '));
        d.title = 'Lineage (P1 — every number explains itself): '
            + parts.join(', ') + (info.note ? ' — ' + info.note : '');
        return d;
    },

    // Meter: fill carries severity; the unfilled track is a lighter step of the
    // same ramp so state reads across the whole bar (dataviz spec).
    // frac in [0,1]; thresholds default warn 0.75 / critical 0.9.
    meter: function (frac, opts) {
        opts = opts || {};
        var warnAt = opts.warnAt == null ? 0.75 : opts.warnAt;
        var critAt = opts.critAt == null ? 0.90 : opts.critAt;
        var fill  = '#2a78d6', track = '#cde2fb';
        if (frac >= critAt)      { fill = '#d03b3b'; track = '#f9e4e4'; }
        else if (frac >= warnAt) { fill = '#fab219'; track = '#fdf3dc'; }
        var w = opts.width || 72;
        var wrap = fdk.el('span', 'display:inline-block;width:' + w + 'px;height:6px;'
            + 'background:' + track + ';border-radius:3px;vertical-align:middle;');
        var f = Math.max(0, Math.min(1, frac));
        wrap.appendChild(fdk.el('span', 'display:block;width:' + Math.round(f * 100)
            + '%;height:6px;background:' + fill + ';border-radius:3px;'));
        wrap.title = Math.round(f * 100) + '%';
        return wrap;
    },

    // label: value row for fact panels
    kv: function (label, value) {
        var row = fdk.el('div', 'display:flex;gap:8px;font-size:12.5px;line-height:1.6;');
        row.appendChild(fdk.el('span', 'color:#898781;min-width:110px;', label));
        var v = fdk.el('span', 'color:#0b0b0b;');
        if (value && value.nodeType) v.appendChild(value); else v.textContent = value;
        row.appendChild(v);
        return row;
    },

    sectionTitle: function (text) {
        return fdk.el('div', 'font-weight:600;font-size:12px;color:#0b0b0b;'
            + 'margin:14px 0 6px;letter-spacing:0.2px;', text);
    }
};
