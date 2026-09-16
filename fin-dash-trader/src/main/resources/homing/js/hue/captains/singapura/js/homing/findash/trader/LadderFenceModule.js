// LadderFenceModule — what the domain puts between the ladder's tables.
//
// RelGridFenceContract: fenceElement() once, onFolded(folded) optional,
// dispose() ours. A fence is a noun as a cell is — it owns its element and
// the group never reads what it drew. Which is the point of this module: THE
// FENCE'S LOOK IS THE DOMAIN'S. The group offers a slot; what a desk puts in
// it is the desk's design. Here that is the Minesweeper bevel the Episode 1
// section row settled on — the caption reads as an unopened cell, the tables
// under it as opened ones — carried over as the same typed class, unchanged.
//
// Three rules the group's walk imposes, from the KT (§6):
//   - the toggle is the fence's FIRST control: Enter on the fence stop presses it;
//   - paint from onFolded, never from the press — a fold arrives by any road;
//   - a fence has no handle: `tell` is the host's closure onto the group.
//
// The framework appends `export { createPairFence, createStampFence }`.

/** The slot above a pair's detail table: the toggle and the pair's name. */
var createPairFence = function (pair, opts) {
    opts = opts || {};
    if (!opts.branch) throw new Error('[PairFence] opts.branch is required: the fence\'s own');
    var b = opts.branch, tell = (typeof opts.tell === 'function') ? opts.tell : null;
    var state = opts.folded === true, root = null, toggle = null, onDbl = null;
    b.activate({ toString: function () { return 'PairFence ' + pair; } });

    function paint() {
        if (!toggle) return;
        toggle.textContent = state ? '▸' : '▾';           // ▸ folded · ▾ shown
        toggle.setAttribute('aria-expanded', state ? 'false' : 'true');
        toggle.setAttribute('aria-label', (state ? 'Unfold ' : 'Fold ') + pair);
    }

    return {
        fenceElement: function () {
            if (root) return root;
            root = b.createElement('fence', 'div');
            css.setClass(root, fd_dense);
            css.addClass(root, fd_section_row);       // the bevel: the domain's look, not the grid's
            css.addClass(root, fd_fence);
            toggle = b.createElement('fold', 'button');
            css.setClass(toggle, fd_fence_toggle);
            toggle.type = 'button';
            toggle.addEventListener('click', function () {
                if (tell) tell(new RelGridGroupFold(pair, !state));
            });
            paint();
            var n = b.createElement('name', 'span');
            css.setClass(n, fd_fence_name);
            n.textContent = pair;
            root.appendChild(toggle);
            root.appendChild(n);
            // The Episode 1 gesture kept: a double-click anywhere on the caption
            // folds. Nothing of the grid's competes for it on a fence.
            onDbl = function (ev) { ev.preventDefault(); if (tell) tell(new RelGridGroupFold(pair, !state)); };
            root.addEventListener('dblclick', onDbl);
            return root;
        },
        onFolded: function (folded) { state = folded === true; paint(); },
        folded:   function () { return state; },
        dispose:  function () {
            if (root && onDbl) root.removeEventListener('dblclick', onDbl);
            root = null; toggle = null; onDbl = null; b.dissolve();
        }
    };
};

/** The trailing slot: the as-of stamp, kept current by the feed. */
var createStampFence = function (feed, opts) {
    opts = opts || {};
    if (!opts.branch) throw new Error('[StampFence] opts.branch is required: the fence\'s own');
    var b = opts.branch, root = null, text = null, unsubscribe = null;
    b.activate({ toString: function () { return 'StampFence'; } });
    function line() {
        var s = feed.slice(), a = feed.asOf();
        return 'as-of slice ' + (s || a || '—') + ' · totals come from the desk, not from summing what you see (P5)';
    }
    return {
        fenceElement: function () {
            if (root) return root;
            root = b.createElement('fence', 'div');
            css.setClass(root, fd_caption);
            css.addClass(root, fd_muted);
            css.addClass(root, fd_fence);
            text = b.createElement('stamp', 'span');
            text.textContent = line();
            root.appendChild(text);
            unsubscribe = feed.subscribe(function (row) { if (row === null && text) text.textContent = line(); });
            return root;
        },
        dispose: function () { if (unsubscribe) unsubscribe(); unsubscribe = null; root = null; text = null; b.dissolve(); }
    };
};
