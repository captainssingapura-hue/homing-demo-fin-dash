package hue.captains.singapura.js.homing.findash.widget;

import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * Scaffold placeholder — the one widget the dashboard ships until the real ones
 * land. It renders a "ready to build" card that also demonstrates the two
 * cross-cutting UI-study principles a downstream widget must honour, so the next
 * agent has a pattern to copy:
 * <ul>
 *   <li><b>P1 — every number explains itself:</b> a lineage stamp (slice / epoch /
 *       model+version) is always visible; here it is a static example.</li>
 *   <li><b>P2 — degraded state is loud:</b> a status chip in a shared visual
 *       language; here a healthy example.</li>
 * </ul>
 *
 * <p>Keeps to the CONSUMER rule set: owned DOM via {@code branch}/{@code
 * document.createElement} + {@code textContent} only — no {@code innerHTML}
 * (view-doctrine), no raw {@code className}/{@code href}, no CDN import. Replace
 * with the real widgets (Pricer W1, Surface Manager W2, Risk Blotter W4).</p>
 */
public final class RiskDashboardPlaceholderWidget
        extends WorkspaceWidget<WorkspaceWidget._None, RiskDashboardPlaceholderWidget> {

    public static final RiskDashboardPlaceholderWidget INSTANCE = new RiskDashboardPlaceholderWidget();

    private RiskDashboardPlaceholderWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, RiskDashboardPlaceholderWidget> {}

    @Override protected _Construct<_None, RiskDashboardPlaceholderWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Risk Dashboard (scaffold)"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
                "    var root = branch.createElement('root', 'div');",
                "    root.style.cssText = 'height:100%;overflow:auto;box-sizing:border-box;'",
                "        + 'padding:20px;font-family:system-ui,sans-serif;font-size:13px;';",
                "",
                "    function el(tag, css, text) {",
                "        var d = document.createElement(tag);",
                "        if (css) d.style.cssText = css;",
                "        if (text != null) d.textContent = text;",
                "        return d;",
                "    }",
                "",
                "    var h = el('div', 'font-size:18px;font-weight:700;color:#1a2332;', 'FX Options Risk Dashboard');",
                "    root.appendChild(h);",
                "    root.appendChild(el('div', 'color:#4a5568;margin:4px 0 14px;',",
                "        'Scaffold ready. Build the trader widgets here \\u2014 see KT.md.'));",
                "",
                "    // P2 \\u2014 status chip in a shared visual language (healthy example).",
                "    var chip = el('span', 'display:inline-block;font-size:11px;font-weight:600;'",
                "        + 'color:#1f5f8b;background:#e8f1f7;border-radius:999px;padding:2px 10px;', '\\u25cf healthy');",
                "    root.appendChild(chip);",
                "",
                "    // P1 \\u2014 lineage stamp, always visible (static example here).",
                "    root.appendChild(el('div', 'color:#8494a8;font-size:11px;margin-top:8px;',",
                "        'slice C204 \\u00b7 surface S513/87 \\u00b7 model VV-2.3  (explain on hover)'));",
                "",
                "    var list = el('div', 'margin-top:16px;color:#4a5568;line-height:1.7;');",
                "    list.appendChild(el('div', 'font-weight:600;color:#1a2332;', 'Planned widgets (UI study \\u00a73):'));",
                "    list.appendChild(el('div', null, '\\u2022 Pricer / deal ticket (W1)'));",
                "    list.appendChild(el('div', null, '\\u2022 Surface manager (W2)'));",
                "    list.appendChild(el('div', null, '\\u2022 Position & risk blotter (W4)'));",
                "    root.appendChild(list);",
                "",
                "    return { root: root, setActive: function (a) {}, partyDeregister: function () {} };");
    }
}
