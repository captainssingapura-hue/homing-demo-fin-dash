// PersonaCardModule — the shared persona home card (scaffold stage).
// Pure DOM builder: personaCard(info) returns a detached element; the calling
// widget appends it into branch-owned DOM. No branch, no lookups, no HTML
// literals — createElement + textContent only (view doctrine).
//
// info: { title, mission, ring, cadence, screens: [..], accent? }
// The framework appends the `export { personaCard }` from exports().
function personaCard(info) {
    function el(tag, css, text) {
        var d = document.createElement(tag);
        if (css) d.style.cssText = css;
        if (text != null) d.textContent = text;
        return d;
    }
    var accent = info.accent || '#1f5f8b';
    var card = el('div', 'max-width:560px;');

    card.appendChild(el('div', 'font-size:18px;font-weight:700;color:var(--color-text-primary);', info.title));
    card.appendChild(el('div', 'color:var(--color-text-primary);margin:4px 0 12px;', info.mission));

    // P2 — status chip in the shared visual language ("scaffold" state: work
    // planned, not degraded, not healthy-live).
    var chip = el('span', 'display:inline-block;font-size:11px;font-weight:600;'
        + 'color:#9a6b1f;background:#faf3e3;border-radius:999px;padding:2px 10px;',
        '● scaffold — screens not built yet');
    card.appendChild(chip);

    var facts = el('div', 'margin-top:12px;color:var(--color-text-primary);font-size:12.5px;line-height:1.7;');
    facts.appendChild(el('div', null, 'Ring writes:  ' + info.ring));
    facts.appendChild(el('div', null, 'Cadence:  ' + info.cadence));
    card.appendChild(facts);

    var list = el('div', 'margin-top:14px;color:var(--color-text-primary);line-height:1.7;');
    list.appendChild(el('div', 'font-weight:600;color:' + accent + ';', 'Planned screens (UI study):'));
    for (var i = 0; i < info.screens.length; i++) {
        list.appendChild(el('div', null, '• ' + info.screens[i]));
    }
    card.appendChild(list);

    // P1 — lineage stamp, always visible (static example until live data lands).
    card.appendChild(el('div', 'color:var(--color-text-muted);font-size:11px;margin-top:14px;',
        'slice C204 · surface S513/87 · model VV-2.3  (explain on hover)'));

    return card;
}
