// FinDashKitModule — the fin-dash UI kit: format helpers and the two
// cross-cutting patterns every widget carries (UI study P1 + P2): the status
// chip (state is visible, never by color alone — icon + label) and the lineage
// stamp (every number can explain itself).
//
// Consumer discipline: elements come from the caller's branch, styling is typed
// CSS classes. Every builder takes (branch, name, …) — the branch owns the
// element and the name makes it addressable and releasable on dissolve.
//
// No colour lives here any more. State maps to a chip class in FdStatusCss;
// the severity ramp maps to the status background classes. The kit knows which
// STATE something is in, and the theme decides what that looks like.
//
// The framework appends the `export { fdk }` from exports().
var fdk = {
    /**
     * A party actor id for a widget instance: `<kind>/<branch name>`.
     *
     * The shell names every widget's branch after its tab, so the branch
     * name is unique per instance and stable across a reconstruction of
     * the workspace — which is what an identity is for. Twenty widgets used
     * to mint one with Math.random(): unique, but a different actor on
     * every mount, so nothing could be addressed or persisted by it and no
     * bus interaction could be replayed. (The desk's own conformance rule
     * bans Math.random() in risk models for the same reason; this closes
     * the gap for identities.)
     *
     * The counter is the fallback for a branch without a name — a test
     * harness, say — and is deterministic within a page lifetime.
     */
    actorId: function (kind, branch) {
        var name = branch && branch.name;
        if (!name) name = 'n' + (fdk._actorSeq = (fdk._actorSeq || 0) + 1);
        return kind + '/' + name;
    },

    // P2 — state is carried by icon + label + class, never by colour alone.
    status: {
        good:     { icon: '●', label: 'healthy',  cls: fd_chip_good     },
        live:     { icon: '●', label: 'live',     cls: fd_chip_good     },
        warn:     { icon: '▲', label: 'degraded', cls: fd_chip_warn     },
        serious:  { icon: '◆', label: 'serious',  cls: fd_chip_serious  },
        critical: { icon: '✕', label: 'critical', cls: fd_chip_critical },
        neutral:  { icon: '○', label: 'idle',     cls: fd_chip_neutral  }
    },

    // The general builder. `klass` is a CssClass constant the caller imported,
    // or an array of them when an element carries more than one role (a caption
    // that is also muted). Passing null leaves the element unstyled — a slot or
    // a bare wrapper that inherits from its parent.
    el: function (branch, name, tag, klass, text) {
        var d = branch.createElement(name, tag);
        if (klass) {
            // Array.isArray, not `.length !== undefined` — a string has a length
            // too, and would be spread one character per class.
            if (Array.isArray(klass)) css.setClass.apply(null, [d].concat(klass));
            else css.setClass(d, klass);
        }
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

    // P2 — the shared status chip: icon + label, never colour alone.
    chip: function (branch, name, state, text) {
        var s = fdk.status[state] || fdk.status.neutral;
        var d = branch.createElement(name, 'span');
        css.setClass(d, fd_chip);
        css.addClass(d, s.cls);
        d.textContent = s.icon + ' ' + (text != null ? text : s.label);
        return d;
    },

    // P1 — the lineage stamp: slice / surface epoch / model, always visible.
    // info: { slice, surface, model, note? } — title attr is the hover explain.
    stamp: function (branch, name, info) {
        var parts = [];
        if (info.slice)   parts.push('slice ' + info.slice);
        if (info.surface) parts.push('surface ' + info.surface);
        if (info.model)   parts.push('model ' + info.model);
        var d = branch.createElement(name, 'span');
        css.setClass(d, fd_stamp);
        d.textContent = parts.join(' · ');
        d.title = 'Lineage (P1 — every number explains itself): '
            + parts.join(', ') + (info.note ? ' — ' + info.note : '');
        return d;
    },

    // Meter: the track carries severity as a status tint, the fill width comes
    // from live data via the --fd-meter-frac custom property (the sanctioned
    // dynamic-value hatch — a class cannot encode a runtime number).
    // frac in [0,1]; thresholds default warn 0.75 / critical 0.9.
    meter: function (branch, name, frac, opts) {
        opts = opts || {};
        var warnAt = opts.warnAt == null ? 0.75 : opts.warnAt;
        var critAt = opts.critAt == null ? 0.90 : opts.critAt;
        var tint = fd_status_good_bg;
        if      (frac >= critAt) tint = fd_status_critical_bg;
        else if (frac >= warnAt) tint = fd_status_warn_bg;
        var wrap = branch.createElement(name, 'span');
        css.setClass(wrap, fd_meter);
        css.addClass(wrap, tint);
        var f = Math.max(0, Math.min(1, frac));
        var fill = branch.createElement(name + '-fill', 'span');
        css.setClass(fill, fd_meter_fill);
        fill.style.setProperty('--fd-meter-frac', String(f));
        wrap.appendChild(fill);
        wrap.title = Math.round(f * 100) + '%';
        return wrap;
    },

    // label: value row for fact panels
    kv: function (branch, name, label, value) {
        var row = branch.createElement(name, 'div');
        css.setClass(row, fd_kv_row);
        var lab = branch.createElement(name + '-label', 'span');
        css.setClass(lab, fd_kv_label);
        lab.textContent = label;
        row.appendChild(lab);
        var v = branch.createElement(name + '-value', 'span');
        css.setClass(v, fd_kv_value);
        if (value && value.nodeType) v.appendChild(value); else v.textContent = value;
        row.appendChild(v);
        return row;
    },

    sectionTitle: function (branch, name, text) {
        var d = branch.createElement(name, 'div');
        css.setClass(d, fd_section_title);
        d.textContent = text;
        return d;
    }
};
