package hue.captains.singapura.js.homing.findash.viz;

import hue.captains.singapura.js.homing.core.BundledExternalModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;

import java.util.List;

/**
 * Three.js — the WebGL renderer the implied-vol surface draws through. Bundled
 * at build time and served from the classpath, so no CDN call is made at
 * runtime (the upstream demo loaded r128 from cdnjs, which
 * {@code NoCdnImportRule} rightly forbids).
 *
 * <h2>The manifest is the dependency surface</h2>
 *
 * <p>The bundle exports several hundred names. Declared here are the
 * <b>exactly twenty</b> that {@link VolSurfaceLib} actually references — the
 * list is mechanically checkable:</p>
 *
 * <pre>grep -ohE "THREE\.[A-Za-z0-9_]+" vol-surface.module.js | sort -u</pre>
 *
 * <p>homing imports names, not namespaces, so {@link VolSurfaceWidget}
 * reassembles these into the single {@code THREE} object the library's
 * peer-dependency contract expects. That is a little more ceremony than
 * {@code import * as THREE}, and it buys something: the dependency cannot
 * quietly widen. A new {@code THREE.Something} in the library fails loudly at
 * construction rather than working by accident off a namespace import.</p>
 *
 * <p><b>Version.</b> Pinned to 0.128.0 to match the r128 the library was
 * written against — its scene setup relies on the pre-r15x lighting and
 * colour-management defaults. Provenance, the file's sha256, the exact
 * {@code THREE.*} contract the desk uses, and the refresh procedure with
 * the r128→current API changes that touch it are in
 * {@code lib/README.md} beside the file.</p>
 *
 * @see <a href="https://threejs.org/">threejs.org</a>
 */
public record ThreeJs() implements BundledExternalModule<ThreeJs> {

    public static final ThreeJs INSTANCE = new ThreeJs();

    @Override public String sourceUrl()    { return "https://esm.sh/three@0.128.0/es2020/three.bundle.mjs"; }
    @Override public String resourcePath() { return "lib/three@0.128.0/three.module.js"; }
    @Override public String sha512()       {
        return "94ce84ed9f371994682cfc435bf0f512bb8c4408de41ee9f90a030816bc973b4"
             + "c7c781a84fdc0f1f79c727fc4adeb925b078bf15553605d88c9b5772a2eabfce";
    }

    // ---- scene graph -----------------------------------------------------
    public record Scene()             implements Exportable._Constant<ThreeJs> {}
    public record PerspectiveCamera() implements Exportable._Constant<ThreeJs> {}
    public record WebGLRenderer()     implements Exportable._Constant<ThreeJs> {}

    // ---- geometry --------------------------------------------------------
    public record BufferGeometry()          implements Exportable._Constant<ThreeJs> {}
    public record Float32BufferAttribute()  implements Exportable._Constant<ThreeJs> {}

    // ---- materials -------------------------------------------------------
    public record MeshPhongMaterial() implements Exportable._Constant<ThreeJs> {}
    public record MeshBasicMaterial() implements Exportable._Constant<ThreeJs> {}
    public record LineBasicMaterial() implements Exportable._Constant<ThreeJs> {}
    public record SpriteMaterial()    implements Exportable._Constant<ThreeJs> {}
    public record DoubleSide()        implements Exportable._Constant<ThreeJs> {}

    // ---- renderables -----------------------------------------------------
    public record Mesh()          implements Exportable._Constant<ThreeJs> {}
    public record Line()          implements Exportable._Constant<ThreeJs> {}
    public record Sprite()        implements Exportable._Constant<ThreeJs> {}
    public record CanvasTexture() implements Exportable._Constant<ThreeJs> {}

    // ---- lighting --------------------------------------------------------
    public record AmbientLight()     implements Exportable._Constant<ThreeJs> {}
    public record DirectionalLight() implements Exportable._Constant<ThreeJs> {}

    // ---- math + picking --------------------------------------------------
    public record Vector2()   implements Exportable._Constant<ThreeJs> {}
    public record Vector3()   implements Exportable._Constant<ThreeJs> {}
    public record Color()     implements Exportable._Constant<ThreeJs> {}
    public record Raycaster() implements Exportable._Constant<ThreeJs> {}

    @Override
    public ExportsOf<ThreeJs> exports() {
        return new ExportsOf<>(INSTANCE, List.of(
                new Scene(), new PerspectiveCamera(), new WebGLRenderer(),
                new BufferGeometry(), new Float32BufferAttribute(),
                new MeshPhongMaterial(), new MeshBasicMaterial(),
                new LineBasicMaterial(), new SpriteMaterial(), new DoubleSide(),
                new Mesh(), new Line(), new Sprite(), new CanvasTexture(),
                new AmbientLight(), new DirectionalLight(),
                new Vector2(), new Vector3(), new Color(), new Raycaster()));
    }
}
