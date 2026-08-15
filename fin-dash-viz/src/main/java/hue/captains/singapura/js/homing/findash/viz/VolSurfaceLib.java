package hue.captains.singapura.js.homing.findash.viz;

import hue.captains.singapura.js.homing.core.BundledExternalModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;

import java.util.List;

/**
 * VolSurface — the 3D implied-volatility surface renderer, vendored from
 * {@code js-demos/VolSurface} and bundled into a single ES module (its four
 * source files concatenated in dependency order: colormap, scene-helpers,
 * surface-controls, vol-surface).
 *
 * <h2>Why this is a bundled external and not a consumer module</h2>
 *
 * <p>It is a standalone library — its own README, version, and Three.js
 * peer-dependency contract — and it is a <b>rendering leaf</b>: an opaque
 * WebGL surface with a single mount point. DomOpsParty's value is composition,
 * and nothing outside a chart ever addresses its geometry by name. Holding it
 * to the consumer rule set would also be unenforceable in principle, since the
 * same argument applies to any third-party charting library (D3, ECharts,
 * Plotly), none of which will ever call {@code branch.createElement}.</p>
 *
 * <p>Classifying it {@code BUNDLED_EXTERNAL} exempts it by <i>category</i>
 * rather than by exception, which is the honest description of what it is.</p>
 *
 * <h2>The cleanup contract</h2>
 *
 * <p>Exemption from the DOM rules is only safe because the subtree is
 * contained and disposable:</p>
 *
 * <ul>
 *   <li>the library builds everything under the single container element the
 *       caller passes in, which is <b>branch-owned</b> — so a dissolve detaches
 *       the whole subtree by ordinary DOM containment;</li>
 *   <li>what escapes that subtree — the rAF loop, {@code window} mouse
 *       listeners for orbit controls, WebGL buffers — is released by
 *       {@link VolSurfaceWidget}'s call to {@code destroy()} from
 *       {@code partyDeregister}, which the framework invokes <em>before</em>
 *       dissolving the branch (RFC 0028 lifecycle order).</li>
 * </ul>
 *
 * @see <a href="https://threejs.org/">Three.js</a> (peer dependency, see {@link ThreeJs})
 */
public record VolSurfaceLib() implements BundledExternalModule<VolSurfaceLib> {

    public static final VolSurfaceLib INSTANCE = new VolSurfaceLib();

    @Override public String sourceUrl()    { return "vendored: js-demos/VolSurface (colormap + scene-helpers + surface-controls + vol-surface)"; }
    @Override public String resourcePath() { return "lib/volsurface@1.0.0/vol-surface.module.js"; }
    @Override public String sha512()       {
        return "bfc18a648aac1494113336cfb1d65a7fc35b4bc8bcccb5bb7bd8f0f8e613a4cc"
             + "2383d5136d080d0b46644cc9fc4863055c92337553ba1e91cf719c6bbab99c9b";
    }

    /** {@code new VolSurface(container, { THREE })} — the renderer itself. */
    public record VolSurface() implements Exportable._Constant<VolSurfaceLib> {}

    /** Named colour ramps; the widget offers these as the palette choice. */
    public record COLORMAPS() implements Exportable._Constant<VolSurfaceLib> {}

    @Override
    public ExportsOf<VolSurfaceLib> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new VolSurface(), new COLORMAPS()));
    }
}
