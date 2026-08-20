// PersonaCardModule — the shared persona home card (scaffold stage).
// Consumer discipline: elements come from the caller's branch, styling is
// typed CSS classes. personaCard(branch, info) returns the card element; the
// calling widget appends it into its own DOM.
//
// info: { title, mission, ring, cadence, screens: [..] }
// The `accent` field is gone — a per-persona colour override was a literal by
// another name; the heading uses the theme's emphasis instead.
//
// The framework appends the `export { personaCard }` from exports().
function personaCard(branch, info) {
    var card = branch.createElement('persona-card', 'div');
    css.setClass(card, fd_stack);

    var title = branch.createElement('persona-title', 'div');
    css.setClass(title, fd_display);
    title.textContent = info.title;
    card.appendChild(title);

    var mission = branch.createElement('persona-mission', 'div');
    css.setClass(mission, fd_body);
    mission.textContent = info.mission;
    card.appendChild(mission);

    // P2 — status chip in the shared visual language ("scaffold" state: work
    // planned, not degraded, not healthy-live). Icon + label, never colour alone.
    var chip = branch.createElement('persona-chip', 'span');
    css.setClass(chip, fd_chip);
    css.addClass(chip, fd_chip_warn);
    chip.textContent = '● scaffold — screens not built yet';
    card.appendChild(chip);

    var facts = branch.createElement('persona-facts', 'div');
    css.setClass(facts, fd_body);
    css.addClass(facts, fd_section);
    var ring = branch.createElement('persona-ring', 'div');
    ring.textContent = 'Ring writes:  ' + info.ring;
    facts.appendChild(ring);
    var cadence = branch.createElement('persona-cadence', 'div');
    cadence.textContent = 'Cadence:  ' + info.cadence;
    facts.appendChild(cadence);
    card.appendChild(facts);

    var list = branch.createElement('persona-screens', 'div');
    css.setClass(list, fd_body);
    css.addClass(list, fd_section);
    var listHead = branch.createElement('persona-screens-head', 'div');
    css.setClass(listHead, fd_strong);
    listHead.textContent = 'Planned screens (UI study):';
    list.appendChild(listHead);
    for (var i = 0; i < info.screens.length; i++) {
        var item = branch.createElement('persona-screen-' + i, 'div');
        item.textContent = '• ' + info.screens[i];
        list.appendChild(item);
    }
    card.appendChild(list);

    // P1 — lineage stamp, always visible (static example until live data lands).
    var stamp = branch.createElement('persona-stamp', 'div');
    css.setClass(stamp, fd_stamp);
    stamp.textContent = 'slice C204 · surface S513/87 · model VV-2.3  (explain on hover)';
    card.appendChild(stamp);

    return card;
}
