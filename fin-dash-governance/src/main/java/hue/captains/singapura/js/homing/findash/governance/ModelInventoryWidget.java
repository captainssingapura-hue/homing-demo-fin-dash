package hue.captains.singapura.js.homing.findash.governance;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashGridModule;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * The model inventory (study §9): every named strategy — version, approval
 * status, where the selection matrix uses it, validation doc — plus the
 * scheduled-revalidation worklist with aging, and the <b>reverse query</b>
 * auditors always ask and systems rarely answer: "show me everything priced
 * by model X version Y" — answerable here because every PricingResult carries
 * its model stamp (P1). Click a row to run it.
 */
public final class ModelInventoryWidget
        extends WorkspaceWidget<WorkspaceWidget._None, ModelInventoryWidget> {

    public static final ModelInventoryWidget INSTANCE = new ModelInventoryWidget();

    private ModelInventoryWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, ModelInventoryWidget> {}

    @Override protected _Construct<_None, ModelInventoryWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Model Inventory"; }
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
            "        + 'font-family:system-ui,sans-serif;font-size:13px;color:var(--color-text-primary);';",
            "",
            "    root.appendChild(fdk.el('div', 'font-weight:700;font-size:14px;letter-spacing:0.3px;',",
            "        'MODEL INVENTORY'));",
            "    root.appendChild(fdk.el('div', 'color:var(--color-text-primary);font-size:11.5px;margin:2px 0 8px;',",
            "        'named strategies \\u00b7 Ring 1: read-only here \\u2014 change goes through a W6 package'));",
            "",
            "    var gridSlot = fdk.el('div', '');",
            "    root.appendChild(gridSlot);",
            "",
            "    var rqTitle = fdk.sectionTitle('Reverse query \\u2014 \"everything priced by model X vY\"');",
            "    root.appendChild(rqTitle);",
            "    var rq = fdk.el('div', 'font-size:12px;color:var(--color-text-muted);min-height:18px;',",
            "        'click a model row to run it');",
            "    root.appendChild(rq);",
            "",
            "    var revTitle = fdk.sectionTitle('Revalidation worklist');",
            "    root.appendChild(revTitle);",
            "    var rev = fdk.el('div', 'color:var(--color-text-primary);font-size:12px;line-height:1.7;');",
            "    root.appendChild(rev);",
            "",
            "    function statusChip(s) {",
            "        if (s === 'approved')  return fdk.chip('good', 'approved');",
            "        if (s === 'candidate') return fdk.chip('warn', 'candidate');",
            "        return fdk.chip('neutral', s);",
            "    }",
            "",
            "    fetch('/fx/changes')",
            "        .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
            "        .then(function (d) {",
            "            var grid = fdGrid({",
            "                columns: [",
            "                    { key: 'name', label: 'Strategy', render: function (v, row) {",
            "                        return fdk.el('span', 'font-weight:600;', v); } },",
            "                    { key: 'version', label: 'Version', align: 'right' },",
            "                    { key: 'status', label: 'Status', render: function (v) { return statusChip(v); } },",
            "                    { key: 'usedBy', label: 'Used by (selection matrix)' },",
            "                    { key: 'doc', label: 'Validation doc' }",
            "                ],",
            "                rows: d.models,",
            "                onRowClick: function (row) {",
            "                    rq.style.color = 'var(--color-text-primary)';",
            "                    rq.textContent = row.name + ' v' + row.version + ': ' + row.reverseQuery;",
            "                }",
            "            });",
            "            gridSlot.appendChild(grid.root);",
            "            for (var i = 0; i < d.revalidation.length; i++) {",
            "                rev.appendChild(fdk.el('div', null, '\\u2022 ' + d.revalidation[i]));",
            "            }",
            "        })",
            "        .catch(function (e) {",
            "            gridSlot.appendChild(fdk.el('div', 'color:#a8502a;',",
            "                'inventory load failed: ' + (e && e.message ? e.message : e)));",
            "        });",
            "",
            "    return {",
            "        root: root,",
            "        setActive: function (active) {},",
            "        partyDeregister: function () {}",
            "    };");
    }
}
