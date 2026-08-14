package hue.captains.singapura.js.homing.findash.governance;

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
                new ModuleImports<>(List.of(new FinDashGridModule.fdGrid()), FinDashGridModule.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdFrameCss.fd_section(),
                        new FdFrameCss.fd_widget_root()),
                        FdFrameCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdStatusCss.fd_error_text()),
                        FdStatusCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdTextCss.fd_caption(),
                        new FdTextCss.fd_strong(),
                        new FdTextCss.fd_title()),
                        FdTextCss.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
            "    var root = branch.createElement('root', 'div');",
            "    css.setClass(root, fd_widget_root);",
            "",
            "    root.appendChild(fdk.el(branch, 'title-1', 'div', fd_title, ",
            "        'MODEL INVENTORY'));",
            "    root.appendChild(fdk.el(branch, 'cap-1', 'div', [fd_caption, fd_section], ",
            "        'named strategies \\u00b7 Ring 1: read-only here \\u2014 change goes through a W6 package'));",
            "",
            "    var gridSlot = fdk.el(branch, 'slot-1', 'div', null);",
            "    root.appendChild(gridSlot);",
            "",
            "    var rqTitle = fdk.sectionTitle(branch, 'sect-1', 'Reverse query \\u2014 \"everything priced by model X vY\"');",
            "    root.appendChild(rqTitle);",
            "    var rq = fdk.el('div', 'font-size:12px;color:var(--color-text-muted);min-height:18px;', ",
            "        'click a model row to run it');",
            "    root.appendChild(rq);",
            "",
            "    var revTitle = fdk.sectionTitle(branch, 'sect-2', 'Revalidation worklist');",
            "    root.appendChild(revTitle);",
            "    var rev = fdk.el('div', 'color:var(--color-text-primary);font-size:12px;line-height:1.7;');",
            "    root.appendChild(rev);",
            "",
            "    function statusChip(s) {",
            "        if (s === 'approved')  return fdk.chip(branch, 'chip-1', 'good', 'approved');",
            "        if (s === 'candidate') return fdk.chip(branch, 'chip-2', 'warn', 'candidate');",
            "        return fdk.chip(branch, 'chip-3', 'neutral', s);",
            "    }",
            "",
            "    fetch('/fx/changes')",
            "        .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
            "        .then(function (d) {",
            "            var grid = fdGrid(branch, {",
            "                columns: [",
            "                    { key: 'name', label: 'Strategy', render: function (v, row) {",
            "                        return fdk.el(branch, 'strong-1', 'span', fd_strong, v); } },",
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
            "            gridSlot.appendChild(fdk.el(branch, 'err-1', 'div', fd_error_text, ",
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
