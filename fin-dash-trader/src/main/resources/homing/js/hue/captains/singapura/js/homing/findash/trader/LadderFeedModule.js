// LadderFeedModule — the book, as the ladder's relations read it.
//
// One poll of /fx/book, one re-judged snapshot, many subscribers. The feed
// is the seam onto the desk: it holds the rows the desk published (with the
// desk's verdicts already beside the numbers — freshState, budgetState) and
// answers the questions a relation asks — which pairs, which tenors of a
// pair, the row for one of them, the totals — in the DESK's order, never
// re-sorted here. It knows nothing of grids, cells or fences.
//
// A tick is `subscribe(fn)`; fn(row) is called for each row whose data
// changed since the last poll, then fn(null) once if any did, so a relation
// can update its cells one by one and a fence can repaint its stamp once.
//
// The framework appends `export { createLadderFeed }` from exports().

var createLadderFeed = function (opts) {
    opts = opts || {};
    var branch = opts.branch, host = opts.host;
    if (!branch || !host) throw new Error('[LadderFeed] opts.branch and opts.host are required');
    var everyMs = (typeof opts.everyMs === 'number') ? opts.everyMs : 5000;

    var byKey = {}, order = [], totals = null, asOf = '', slice = '';
    var subs = [], timer = null, gen = 0;

    function key(r) { return r.pair + '/' + (r.group ? 'Σ' : r.tenor); }

    // The desk's order is the book's order. Pairs in first-seen order; within
    // a pair, tenors in the order the desk listed them; the group row apart.
    function absorb(d) {
        var next = {}, nextOrder = [], changed = [];
        (d.rows || []).forEach(function (r) {
            var k = key(r);
            next[k] = r; nextOrder.push(k);
            var was = byKey[k];
            if (!was || JSON.stringify(was) !== JSON.stringify(r)) changed.push(r);
        });
        var totalsChanged = JSON.stringify(totals) !== JSON.stringify(d.totals || null);
        byKey = next; order = nextOrder; totals = d.totals || null;
        asOf = d.asOf || d.slice || ''; slice = d.slice || '';
        return { rows: changed, totals: totalsChanged };
    }

    function tell(delta) {
        if (!delta.rows.length && !delta.totals) return;
        subs.slice().forEach(function (fn) {
            delta.rows.forEach(function (r) { fn(r); });
            fn(null);
        });
    }

    function poll() {
        var g = ++gen;
        fdk.load('/fx/book', {
            branch: branch, host: host, what: 'risk ladder',
            stillWanted: function () { return g === gen && timer !== null; }
        }, function (d) { tell(absorb(d)); });
    }

    return {
        pairs:  function () {
            var seen = {}, out = [];
            order.forEach(function (k) { var p = byKey[k].pair; if (!seen[p]) { seen[p] = 1; out.push(p); } });
            return out;
        },
        tenors: function (pair) {
            return order.filter(function (k) { var r = byKey[k]; return r.pair === pair && !r.group; })
                        .map(function (k) { return byKey[k].tenor; });
        },
        row:      function (pair, tenor) { return byKey[pair + '/' + tenor] || null; },
        subtotal: function (pair)        { return byKey[pair + '/Σ'] || null; },
        totals:   function ()            { return totals; },
        asOf:     function ()            { return asOf; },
        slice:    function ()            { return slice; },
        /** First load: resolves once rows exist, then keeps polling. */
        start: function (onReady) {
            if (timer !== null) return;
            var g = ++gen;
            fdk.load('/fx/book', { branch: branch, host: host, what: 'risk ladder' }, function (d) {
                if (g !== gen) return;
                absorb(d);
                timer = setInterval(poll, everyMs);
                if (onReady) onReady();
            });
        },
        subscribe: function (fn) {
            subs.push(fn);
            return function () { var i = subs.indexOf(fn); if (i >= 0) subs.splice(i, 1); };
        },
        stop: function () { if (timer !== null) { clearInterval(timer); timer = null; } gen++; subs = []; }
    };
};
