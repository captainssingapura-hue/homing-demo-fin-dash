package hue.captains.singapura.js.homing.findash.data.meta;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/**
 * One widget's dependency on a data type: which widget, in which workspace,
 * and in what role.
 *
 * <p>The widget is named by <b>stable string ids</b>, never by class: this
 * module must stay free of any UI dependency (D1). The map is documentation
 * that happens to be machine-readable — which is what lets a screen answer
 * "who needs this type?" without anyone maintaining a second list.</p>
 */
public record WidgetUsage(String widgetId, String widgetLabel, String workspaceKind,
                          UsageRole role, String note) implements ValueObject {

    public WidgetUsage {
        Objects.requireNonNull(widgetId, "WidgetUsage.widgetId");
        Objects.requireNonNull(widgetLabel, "WidgetUsage.widgetLabel");
        Objects.requireNonNull(workspaceKind, "WidgetUsage.workspaceKind");
        Objects.requireNonNull(role, "WidgetUsage.role");
        Objects.requireNonNull(note, "WidgetUsage.note");
    }
}
