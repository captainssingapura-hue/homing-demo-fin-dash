package hue.captains.singapura.js.homing.findash.viz;

import hue.captains.singapura.js.homing.core.BundledExternalModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;

import java.util.List;

/**
 * VolSurface — the 3D implied-volatility surface renderer. Seeded from the
 * {@code js-demos/VolSurface} sketch and bundled into a single ES module (four
 * source files concatenated in dependency order: colormap, scene-helpers,
 * surface-controls, vol-surface), then extended here.
 *
 * <p><b>This copy is the real one.</b> {@code js-demos} is a sketchbook, not an
 * upstream: there is no obligation to keep the two in step, and this file is
 * free to be reimplemented outright. It already diverges — {@code resize()}
 * exists here and not there.</p>
 *
 * <h2>Why this is exempt from the consumer rule set</h2>
 *
 * <p>Because it is a <b>rendering leaf</b>: an opaque WebGL surface with a
 * single mount point. DomOpsParty's value is composition, and nothing outside a
 * chart ever addresses its geometry by name. The same reasoning applies to any
 * charting library one might swap in — D3, ECharts, Plotly — none of which
 * would ever call {@code branch.createElement}, so a rule demanding it would be
 * unenforceable in principle rather than merely inconvenient here.</p>
 *
 * <p>{@code BundledExternalModule} is the mechanism that expresses this:
 * {@code ModuleClassifier} maps it to {@code BUNDLED_EXTERNAL}, whose rule set
 * is empty. Note the mechanism is named for the common case (third-party code)
 * while the justification here is the leaf boundary — the code being ours does
 * not change the argument, and would not have earned the exemption on its
 * own.</p>
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

    /**
     * Provenance, not a sync source. Nothing fetches from here; the bundle in
     * {@link #resourcePath()} is maintained directly and has already diverged.
     */
    @Override public String sourceUrl()    { return "seeded from js-demos/VolSurface; maintained here"; }
    @Override public String resourcePath() { return "lib/volsurface@1.0.0/vol-surface.module.js"; }
    @Override public String sha512()       {
        return "d4d6f2e303eed13faf2ea414f210e3f86a23b65187cf55d11673f33c465e570c"
             + "4dffb98451064b8e6b7f63d8102a06b94136c15a4f9ff2bfe9cbb140922e04d1";
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
