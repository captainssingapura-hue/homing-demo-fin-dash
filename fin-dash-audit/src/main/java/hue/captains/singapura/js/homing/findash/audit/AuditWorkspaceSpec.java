package hue.captains.singapura.js.homing.findash.audit;

import hue.captains.singapura.js.homing.findash.book.PortfolioTreeWidget;
import hue.captains.singapura.js.homing.findash.book.TradeBlotterWidget;
import hue.captains.singapura.js.homing.findash.core.bus.DeskSecretaryModule;
import hue.captains.singapura.js.homing.findash.core.bus.TradeSecretaryModule;
import hue.captains.singapura.js.homing.studio.workspace.NavigatorSecretaryModule;
import hue.captains.singapura.js.homing.workspace.WidgetEntry;
import hue.captains.singapura.js.homing.workspace.WidgetGroup;
import hue.captains.singapura.js.homing.workspace.WidgetIcon;
import hue.captains.singapura.js.homing.workspace.WidgetLabel;
import hue.captains.singapura.js.homing.workspace.shell.PartyDecl;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceSpec;

import java.util.List;

/**
 * The Audit, compliance & mgmt workspace (kind {@code "audit"}) on the reused
 * {@code GenericWorkspace} shell. Scaffold: the home card; the persona's real
 * screens (UI study) land beside it, grouped as they arrive.
 *
 * <p>The generic {@code NavigatorSecretary} bus is reused (exposed as
 * {@code navParty}) until this persona's cross-widget events need a bespoke
 * secretary.</p>
 */
public final class AuditWorkspaceSpec implements WorkspaceSpec {

    public static final AuditWorkspaceSpec INSTANCE = new AuditWorkspaceSpec();

    private AuditWorkspaceSpec() {}

    @Override public String kind()  { return "audit"; }
    @Override public String title() { return "Audit Explorer"; }

    @Override
    public List<WidgetEntry> widgetEntries() {
        WidgetGroup overview = WidgetGroup.of("Overview");
        return List.of(
            WidgetEntry.of(PortfolioTreeWidget.class, WidgetLabel.of("Portfolios"))
                    .withIcon(new WidgetIcon.Emoji("🌳"))
                    .withGroup(WidgetGroup.of("Book")),
            WidgetEntry.of(TradeBlotterWidget.class, WidgetLabel.of("Trade Blotter"))
                    .withIcon(new WidgetIcon.Emoji("🧾"))
                    .withGroup(WidgetGroup.of("Console")),
            WidgetEntry.of(AuditExplorerWidget.class, WidgetLabel.of("Audit Explorer"))
                    .withIcon(new WidgetIcon.Emoji("🔍"))
                    .withGroup(WidgetGroup.of("Console")),
            WidgetEntry.of(AuditHomeWidget.class, WidgetLabel.of("Overview"))
                    .withIcon(new WidgetIcon.Emoji("🔍"))
                    .withGroup(overview)
        );
    }

    @Override
    public List<PartyDecl> parties() {
        return List.of(
            PartyDecl.of("navigation", NavigatorSecretaryModule.INSTANCE, "NavigatorSecretary")
                     .exposedAs("navParty")
                     .build(),
            PartyDecl.of("desk", DeskSecretaryModule.INSTANCE, "DeskSecretary")
                     .exposedAs("deskParty")
                     .build(),
            PartyDecl.of("trade", TradeSecretaryModule.INSTANCE, "TradeSecretary")
                     .exposedAs("tradeParty")
                     .build()
        );
    }
}
