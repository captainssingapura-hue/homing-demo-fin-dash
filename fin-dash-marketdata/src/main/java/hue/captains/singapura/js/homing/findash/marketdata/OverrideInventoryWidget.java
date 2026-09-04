package hue.captains.singapura.js.homing.findash.marketdata;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashGridModule;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;
import hue.captains.singapura.js.homing.findash.core.css.FdFrameCss;
import hue.captains.singapura.js.homing.findash.core.css.FdStatusCss;
import hue.captains.singapura.js.homing.findash.core.css.FdTextCss;

import java.util.List;

/**
 * The override inventory (study §6): every live Ring-3 mark and data action
 * across the whole stack — who, why, when it expires — in one queryable view,
 * oldest first, because "scattered overrides that nobody can enumerate are how
 * yesterday's judgment becomes next year's mystery". Aging entries carry P2
 * severity chips.
 */
public final class OverrideInventoryWidget
        extends WorkspaceWidget<WorkspaceWidget._None, OverrideInventoryWidget> {

    public static final OverrideInventoryWidget INSTANCE = new OverrideInventoryWidget();

    private OverrideInventoryWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, OverrideInventoryWidget> {}

    @Override protected _Construct<_None, OverrideInventoryWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Override Inventory"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(
                new ModuleImports<>(List.of(new FinDashKitModule.fdk()), FinDashKitModule.INSTANCE),
                new ModuleImports<>(List.of(new FinDashGridModule.fdGrid()), FinDashGridModule.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdFrameCss.fd_header_row(),
                        new FdFrameCss.fd_section(),
                        new FdFrameCss.fd_widget_root()),
                        FdFrameCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdStatusCss.fd_error_text()),
                        FdStatusCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdTextCss.fd_caption(),
                        new FdTextCss.fd_title()),
                        FdTextCss.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
            "    var root = branch.createElement('root', 'div');",
            "    css.setClass(root, fd_widget_root);",
            "",
            "    var head = fdk.el(branch, 'head-1', 'div', fd_header_row);",
            "    head.appendChild(fdk.el(branch, 'title-1', 'span', fd_title, ",
            "        'OVERRIDE INVENTORY'));",
            "    var countSlot = fdk.el(branch, 'slot-1', 'span', null);",
            "    head.appendChild(countSlot);",
            "    root.appendChild(head);",
            "    root.appendChild(fdk.el(branch, 'cap-1', 'div', [fd_caption, fd_section], ",
            "        'every live Ring-3 mark + data action, whole stack \\u00b7 who \\u00b7 why \\u00b7 expiry \\u00b7 oldest first'));",
            "",
            "    var gridSlot = fdk.el(branch, 'slot-2', 'div', null);",
            "    root.appendChild(gridSlot);",
            "",
            "    // The chip belongs to the row's own sub-branch and is keyed by row",
            "    // index — a fixed name on the widget branch throws on the second row.",
            "    function ageChip(b, i, row) {",
            "        return fdk.chip(b, 'age-chip-' + i, row.severity, row.age);",
            "    }",
            "",
            "    fdk.load('/fx/overrides', { branch: branch, host: gridSlot, what: 'overrides' }, function (d) {",
            "            var n = d.overrides.length;",
            "            var oldest = d.overrides[0];",
            "            countSlot.appendChild(fdk.chip(branch, 'chip-2', oldest.severity === 'serious' ? 'serious' : 'warn',",
            "                n + ' live \\u00b7 oldest ' + oldest.age));",
            "            var grid = fdGrid(branch, {",
            "                columns: [",
            "                    { key: 'age', label: 'Age', render: function (v, row, b, i) { return ageChip(b, i, row); } },",
            "                    { key: 'scope', label: 'Scope' },",
            "                    { key: 'what', label: 'Override' },",
            "                    { key: 'who', label: 'Who' },",
            "                    { key: 'why', label: 'Why' },",
            "                    { key: 'expires', label: 'Expires' }",
            "                ],",
            "                rows: d.overrides",
            "            });",
            "            gridSlot.appendChild(grid.root);",
"    });",
            "",
            "    return {",
            "        root: root,",
            "        setActive: function (active) {},",
            "        partyDeregister: function () {}",
            "    };");
    }
}
