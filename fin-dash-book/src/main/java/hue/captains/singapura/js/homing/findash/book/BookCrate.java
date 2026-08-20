package hue.captains.singapura.js.homing.findash.book;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.js.CoreJsCrate;
import hue.captains.singapura.js.homing.findash.core.FinDashCoreCrate;

import java.util.List;

/**
 * The {@link Crate} for {@code fin-dash-book}: the two shared anchor widgets
 * — the Portfolio view and the Trade Blotter — declared once here and listed
 * in <b>multiple</b> persona workspace specs (trader, risk, middle office,
 * IPV, audit). One crate, many rosters: the crate governs serving +
 * conformance; the specs govern which pickers offer the widget.
 */
public final class BookCrate implements Crate {

    public static final BookCrate INSTANCE = new BookCrate();

    private BookCrate() {}

    @Override public String name() { return "fin-dash-book"; }

    @Override
    public List<Crate> requires() {
        // FinDashCoreCrate: the kit. CoreJsCrate: PortfolioTreeWidget imports
        // the framework's TreeRendererModule — a genuine cross-crate edge.
        return List.of(FinDashCoreCrate.INSTANCE, CoreJsCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(PortfolioWidget.INSTANCE),
                CrateEntry.of(TradeBlotterWidget.INSTANCE),
                CrateEntry.of(TradeRowModule.INSTANCE),
                CrateEntry.of(PortfolioTreeWidget.INSTANCE));
    }
}
