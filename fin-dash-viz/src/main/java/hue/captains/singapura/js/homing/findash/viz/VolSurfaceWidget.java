package hue.captains.singapura.js.homing.findash.viz;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.css.FdControlCss;
import hue.captains.singapura.js.homing.findash.core.css.FdFrameCss;
import hue.captains.singapura.js.homing.findash.core.css.FdStatusCss;
import hue.captains.singapura.js.homing.findash.core.css.FdTextCss;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * The 3D implied-volatility surface — the whole smile term structure at once,
 * where {@link hue.captains.singapura.js.homing.findash.trader.SurfaceManagerWidget}
 * shows one tenor's slice at a time. Rendered by the vendored
 * {@link VolSurfaceLib} on {@link ThreeJs}; the data is the desk's own
 * {@code /fx/surface}, so the mesh is the same fitted surface the pricer quotes
 * from, carrying the same slice and epoch stamp (P1).
 *
 * <p>Joins the desk party: an {@code InstrumentChanged} — a blotter row click,
 * a pair tab in the surface manager — reloads the surface for that pair.</p>
 *
 * <h2>The rendering-leaf contract</h2>
 *
 * <p>This widget is the conformant half of the arrangement described on
 * {@link VolSurfaceLib}. It owns:</p>
 *
 * <ul>
 *   <li><b>the mount point</b> — a branch-owned element carrying
 *       {@code fd-render-target}, into which the library builds its canvas,
 *       tooltip and legend. Nothing the library makes escapes that subtree, so a
 *       dissolve detaches all of it by ordinary DOM containment;</li>
 *   <li><b>the disposal</b> — {@code partyDeregister} calls the library's
 *       {@code destroy()}, which cancels the animation frame, detaches the
 *       {@code window} mouse listeners the orbit controls install, and frees the
 *       WebGL geometries and materials. The framework invokes
 *       {@code partyDeregister} <em>before</em> dissolving the branch, so the
 *       DOM the library wants to clean up is still there when it runs.</li>
 * </ul>
 *
 * <p>Everything this widget builds itself goes through {@code branch} and the
 * typed CSS vocabulary, exactly like any other widget.</p>
 */
public final class VolSurfaceWidget
        extends WorkspaceWidget<WorkspaceWidget._None, VolSurfaceWidget> {

    public static final VolSurfaceWidget INSTANCE = new VolSurfaceWidget();

    private VolSurfaceWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, VolSurfaceWidget> {}

    @Override protected _Construct<_None, VolSurfaceWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Vol Surface 3D"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(
                new ModuleImports<>(List.of(new FinDashKitModule.fdk()), FinDashKitModule.INSTANCE),
                // The exact twenty Three.js names VolSurfaceLib references. homing
                // imports names rather than namespaces, so the widget reassembles
                // them below into the single THREE object the library expects.
                new ModuleImports<>(List.of(
                        new ThreeJs.Scene(), new ThreeJs.PerspectiveCamera(),
                        new ThreeJs.WebGLRenderer(),
                        new ThreeJs.BufferGeometry(), new ThreeJs.Float32BufferAttribute(),
                        new ThreeJs.MeshPhongMaterial(), new ThreeJs.MeshBasicMaterial(),
                        new ThreeJs.LineBasicMaterial(), new ThreeJs.SpriteMaterial(),
                        new ThreeJs.DoubleSide(),
                        new ThreeJs.Mesh(), new ThreeJs.Line(), new ThreeJs.Sprite(),
                        new ThreeJs.CanvasTexture(),
                        new ThreeJs.AmbientLight(), new ThreeJs.DirectionalLight(),
                        new ThreeJs.Vector2(), new ThreeJs.Vector3(),
                        new ThreeJs.Color(), new ThreeJs.Raycaster()),
                        ThreeJs.INSTANCE),
                new ModuleImports<>(List.of(
                        new VolSurfaceLib.VolSurface(), new VolSurfaceLib.COLORMAPS()),
                        VolSurfaceLib.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdFrameCss.fd_widget_root(),
                        new FdFrameCss.fd_header_row(),
                        new FdFrameCss.fd_cluster(),
                        new FdFrameCss.fd_section(),
                        new FdFrameCss.fd_render_target(),
                        new FdFrameCss.fd_stack()),
                        FdFrameCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdTextCss.fd_title(),
                        new FdTextCss.fd_caption(),
                        new FdTextCss.fd_muted()),
                        FdTextCss.INSTANCE),
                new ModuleImports<>(List.of(new FdControlCss.fd_input()), FdControlCss.INSTANCE),
                new ModuleImports<>(List.of(new FdStatusCss.fd_error_text()), FdStatusCss.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
            "    var root = branch.createElement('root', 'div');",
            "    css.setClass(root, fd_widget_root);",
            "    css.addClass(root, fd_stack);",
            "",
            "    var head = fdk.el(branch, 'head', 'div', fd_header_row);",
            "    var titleEl = fdk.el(branch, 'title', 'span', fd_title, 'VOL SURFACE \\u2014 3D');",
            "    head.appendChild(titleEl);",
            "    var stampSlot = fdk.el(branch, 'stamp-slot', 'span', null);",
            "    head.appendChild(stampSlot);",
            "    root.appendChild(head);",
            "",
            "    var controls = fdk.el(branch, 'controls', 'div', [fd_cluster, fd_section]);",
            "    controls.appendChild(fdk.el(branch, 'cmap-label', 'span', [fd_caption, fd_muted], 'colour'));",
            "    var cmap = fdk.el(branch, 'cmap', 'select', fd_input);",
            "    ['plasma', 'viridis', 'inferno', 'magma'].forEach(function (name) {",
            "        cmap.appendChild(fdk.el(branch, 'cmap-opt-' + name, 'option', null, name));",
            "    });",
            "    controls.appendChild(cmap);",
            "    var wire = fdk.el(branch, 'wire', 'button', fd_input, 'wireframe: off');",
            "    controls.appendChild(wire);",
            "    var HINT = 'drag to orbit \\u00b7 wheel to zoom \\u00b7 right-drag to pan';",
            "    var hoverSlot = fdk.el(branch, 'hover', 'span', [fd_caption, fd_muted], HINT);",
            "    controls.appendChild(hoverSlot);",
            "    root.appendChild(controls);",
            "",
            "    // The library's mount point. Branch-owned, so the canvas, tooltip and",
            "    // legend it builds inside are detached with it when the branch dissolves.",
            "    var mount = fdk.el(branch, 'mount', 'div', fd_render_target);",
            "    root.appendChild(mount);",
            "",
            "    // The library's peer-dependency contract wants one THREE object.",
            "    // homing imports names, so it is reassembled here — which keeps the",
            "    // dependency explicit: a new THREE.Something in the library fails",
            "    // loudly here rather than resolving off an ambient namespace.",
            "    var THREE = {",
            "        Scene: Scene, PerspectiveCamera: PerspectiveCamera, WebGLRenderer: WebGLRenderer,",
            "        BufferGeometry: BufferGeometry, Float32BufferAttribute: Float32BufferAttribute,",
            "        MeshPhongMaterial: MeshPhongMaterial, MeshBasicMaterial: MeshBasicMaterial,",
            "        LineBasicMaterial: LineBasicMaterial, SpriteMaterial: SpriteMaterial,",
            "        DoubleSide: DoubleSide,",
            "        Mesh: Mesh, Line: Line, Sprite: Sprite, CanvasTexture: CanvasTexture,",
            "        AmbientLight: AmbientLight, DirectionalLight: DirectionalLight,",
            "        Vector2: Vector2, Vector3: Vector3, Color: Color, Raycaster: Raycaster",
            "    };",
            "",
            "    // The library measures its container ONCE, in the constructor, and",
            "    // never resizes (vol-surface.js:131,138 read clientWidth/clientHeight).",
            "    // In a dockable workspace the pane has no size at construct time and",
            "    // changes size whenever a split is dragged, so the widget owns sizing:",
            "    // build on the first non-zero measurement, and rebuild when it changes.",
            "    var surface = null, wireOn = false, lastData = null, lastW = 0, lastH = 0;",
            "",
            "    function build() {",
            "        if (surface) { try { surface.destroy(); } catch (e) {} }",
            "        surface = new VolSurface(mount, { THREE: THREE, colormap: cmap.value || 'plasma' });",
            "        surface.setWireframe(wireOn);",
            "        surface.on('hover', function (p) {",
            "            hoverSlot.textContent = p",
            "                ? p.tenor + ' \\u00b7 ' + p.delta + '\\u0394 \\u00b7 ' + p.value.toFixed(2) + ' vol'",
            "                : HINT;",
            "        });",
            "        if (lastData) surface.setData(lastData);",
            "    }",
            "",
            "    // Build whenever the mount has a usable size and that size has",
            "    // meaningfully changed. Called from two places on purpose:",
            "    //   - setActive(true), when the tab is shown. Not frame-driven, so it",
            "    //     works even where ResizeObserver never fires (a hidden tab does",
            "    //     not composite, and RO callbacks are delivered on a frame);",
            "    //   - the ResizeObserver, for splits dragged while the tab is open.",
            "    function fit() {",
            "        var box = mount.getBoundingClientRect();",
            "        var w = Math.round(box.width), h = Math.round(box.height);",
            "        if (w < 1 || h < 1) return;",
            "        if (surface && Math.abs(w - lastW) < 8 && Math.abs(h - lastH) < 8) return;",
            "        lastW = w; lastH = h;",
            "        build();",
            "    }",
            "",
            "    var ro = new ResizeObserver(fit);",
            "    ro.observe(mount);",
            "    fit();   // in case the pane is already laid out",
            "",
            "    cmap.onchange = function () { if (surface) surface.setColormap(cmap.value); };",
            "    wire.onclick = function () {",
            "        wireOn = !wireOn;",
            "        if (surface) surface.setWireframe(wireOn);",
            "        wire.textContent = 'wireframe: ' + (wireOn ? 'on' : 'off');",
            "    };",
            "",
            "    // The five smile points per tenor ARE the delta axis, in order:",
            "    // 10ΔP · 25ΔP · ATM · 25ΔC · 10ΔC. The fitted values are the surface;",
            "    // mkt is the raw quotes the fit was struck against.",
            "    var DELTAS = [10, 25, 50, 75, 90];",
            "",
            "    var stampBranch = null;",
            "    function load(pair) {",
            "        fetch('/fx/surface?pair=' + encodeURIComponent(pair))",
            "            .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
            "            .then(function (d) {",
            "                var grid = [], tenors = [];",
            "                for (var t = 0; t < d.tenors.length; t++) {",
            "                    var row = [];",
            "                    for (var s = 0; s < d.tenors[t].smile.length; s++) {",
            "                        row.push(d.tenors[t].smile[s].fit);",
            "                    }",
            "                    grid.push(row);",
            "                    tenors.push(d.tenors[t].tenor);",
            "                }",
            "                lastData = { grid: grid, tenors: tenors, deltas: DELTAS, label: d.pair };",
            "                if (surface) surface.setData(lastData);",
            "                titleEl.textContent = 'VOL SURFACE \\u2014 ' + d.pair;",
            "                if (stampBranch) stampBranch.dissolve();",
            "                stampBranch = branch.createBranch('stamp');",
            "                stampBranch.activate(root);",
            "                stampSlot.appendChild(fdk.stamp(stampBranch, 'stamp',",
            "                    { slice: d.slice, surface: d.epoch, model: d.model }));",
            "                if (d.stale) {",
            "                    stampSlot.appendChild(fdk.chip(stampBranch, 'stale', 'warn',",
            "                        'stale \\u2014 ' + d.staleNote));",
            "                }",
            "            })",
            "            .catch(function (e) {",
            "                hoverSlot.textContent = 'surface load failed: ' + (e && e.message ? e.message : e);",
            "                css.setClass(hoverSlot, fd_error_text);",
            "            });",
            "    }",
            "    load('EURUSD');",
            "",
            "    var party = (workspaceCtx && workspaceCtx.deskParty) ? workspaceCtx.deskParty : null;",
            "    var actorId = null;",
            "    var current = 'EURUSD';",
            "    if (party) {",
            "        actorId = 'viz/volsurface-' + Math.random().toString(36).slice(2, 8);",
            "        party.joinActor({",
            "            id: actorId,",
            "            parentSecretary: 'desk',",
            "            reactors: {",
            "                InstrumentChanged: function (msg) {",
            "                    var ins = msg.instrument || {};",
            "                    if (ins.pair && ins.pair !== current) { current = ins.pair; load(ins.pair); }",
            "                }",
            "            }",
            "        });",
            "    }",
            "",
            "    return {",
            "        root: root,",
            "        setActive: function (active) { if (active) fit(); },",
            "        partyDeregister: function () {",
            "            // Runs BEFORE the branch dissolves (RFC 0028 order), so the",
            "            // library's DOM still exists for it to unwind. This is the",
            "            // disposer half of the rendering-leaf contract: without it the",
            "            // rAF loop and the window mouse listeners would outlive the tab.",
            "            try { ro.disconnect(); } catch (e) {}",
            "            if (surface) { try { surface.destroy(); } catch (e) {} }",
            "            if (actorId && party) { try { party.leave(actorId); } catch (e) {} }",
            "        }",
            "    };");
    }
}
