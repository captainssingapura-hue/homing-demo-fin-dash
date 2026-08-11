package hue.captains.singapura.js.homing.findash.marketdata;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashGridModule;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

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
                new ModuleImports<>(List.of(new FinDashGridModule.fdGrid()), FinDashGridModule.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
            "    var root = branch.createElement('root', 'div');",
            "    root.style.cssText = 'height:100%;overflow:auto;box-sizing:border-box;padding:14px;'",
            "        + 'font-family:system-ui,sans-serif;font-size:13px;color:#0b0b0b;';",
            "",
            "    var head = fdk.el('div', 'display:flex;align-items:baseline;gap:10px;flex-wrap:wrap;');",
            "    head.appendChild(fdk.el('span', 'font-weight:700;font-size:14px;letter-spacing:0.3px;',",
            "        'OVERRIDE INVENTORY'));",
            "    var countSlot = fdk.el('span', '');",
            "    head.appendChild(countSlot);",
            "    root.appendChild(head);",
            "    root.appendChild(fdk.el('div', 'color:#52514e;font-size:11.5px;margin:2px 0 8px;',",
            "        'every live Ring-3 mark + data action, whole stack \\u00b7 who \\u00b7 why \\u00b7 expiry \\u00b7 oldest first'));",
            "",
            "    var gridSlot = fdk.el('div', '');",
            "    root.appendChild(gridSlot);",
            "",
            "    function ageChip(row) {",
            "        return fdk.chip(row.severity, row.age);",
            "    }",
            "",
            "    fetch('/fx/overrides')",
            "        .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
            "        .then(function (d) {",
            "            var n = d.overrides.length;",
            "            var oldest = d.overrides[0];",
            "            countSlot.appendChild(fdk.chip(oldest.severity === 'serious' ? 'serious' : 'warn',",
            "                n + ' live \\u00b7 oldest ' + oldest.age));",
            "            var grid = fdGrid({",
            "                columns: [",
            "                    { key: 'age', label: 'Age', render: function (v, row) { return ageChip(row); } },",
            "                    { key: 'scope', label: 'Scope' },",
            "                    { key: 'what', label: 'Override' },",
            "                    { key: 'who', label: 'Who' },",
            "                    { key: 'why', label: 'Why' },",
            "                    { key: 'expires', label: 'Expires' }",
            "                ],",
            "                rows: d.overrides",
            "            });",
            "            gridSlot.appendChild(grid.root);",
            "        })",
            "        .catch(function (e) {",
            "            gridSlot.appendChild(fdk.el('div', 'color:#a8502a;',",
            "                'overrides load failed: ' + (e && e.message ? e.message : e)));",
            "        });",
            "",
            "    return {",
            "        root: root,",
            "        setActive: function (active) {},",
            "        partyDeregister: function () {}",
            "    };");
    }
}
