# Vendored libraries

Two files are served verbatim as `BundledExternalModule`s. Both are exempt
from the JS conformance rules because they are **rendering leaves** — nothing
outside a chart addresses their geometry by name — not because of who wrote
them. This file answers, for each, the three questions a vendored file owes:
where it came from, how you know it is that file, and what depends on what
in it.

## `three@0.128.0/three.module.js` — third-party

| | |
|---|---|
| what | three.js r128, ES-module build |
| from | `https://esm.sh/three@0.128.0/es2020/three.bundle.mjs` (recorded in `ThreeJs.java`) |
| released | April 2021 |
| size | 601,166 bytes |
| sha256 | `9f7b5a754db9b3e972ecd29938e462014394819c3f134f5f5ba929f880ebcb3e` |
| served as | `ThreeJs.INSTANCE`, `sourceUrl()` / `resourcePath()` in `fin-dash-viz` |

**Verify** the file is the one above:

```bash
sha256sum fin-dash-viz/src/main/resources/lib/three@0.128.0/three.module.js
```

**What the desk uses from it** — the whole contract, by grep over
`vol-surface.module.js`:

`Scene`, `PerspectiveCamera`, `WebGLRenderer`, `BufferGeometry`
(`setFromPoints`, `setAttribute`, `computeVertexNormals`),
`Float32BufferAttribute`, `Mesh`, `MeshPhongMaterial`, `MeshBasicMaterial`,
`LineBasicMaterial`, `Line`, `Sprite`, `SpriteMaterial`, `CanvasTexture`,
`Color`, `Vector2`/`Vector3`, `Raycaster`, `AmbientLight`,
`DirectionalLight`, `DoubleSide`.

**Refresh procedure.** The pin is four years old and there is no resolver to
do this for you, which is the bargain stated in the React study — one file,
no dependency tree, and a person refreshes it by hand.

1. Pick the release. Download the ES-module build from the same source
   (`https://esm.sh/three@<version>/es2020/three.bundle.mjs`) into
   `lib/three@<version>/three.module.js`; delete the old directory.
2. Update `ThreeJs.java`: `sourceUrl()`, `resourcePath()`, the version in
   the javadoc. Update this file: version, date, size, sha256.
3. Check the contract above against the release notes between r128 and the
   target. Known changes on that path that touch this desk's usage:
   - r152: `renderer.outputEncoding` → `renderer.outputColorSpace`, and
     colour management is on by default (`ColorManagement.enabled`) — the
     colormap's vertex colours will render differently unless the surface
     sets them in linear space or disables management.
   - r155+: lighting intensity units changed (`useLegacyLights` removed in
     r165); `AmbientLight`/`DirectionalLight` intensities need re-tuning.
   - `Geometry` was removed in r125 — already absent here; `BufferGeometry`
     throughout.
4. Verify in a browser with WebGL — the runtime sweep skips
   `VolSurfaceWidget` by name for exactly this reason, so the check is
   manual: mount the surface in the Trader workspace, change the colormap,
   drag to rotate, confirm no console errors and a lit surface.

The refresh was **not** performed when this file was written: the session
that wrote it had no WebGL-capable browser to run step 4 in, and a pinned old
library that is known to render beats a new one that is not known to.

## `volsurface@1.0.0/vol-surface.module.js` — the desk's own

| | |
|---|---|
| what | the 3D implied-vol renderer: colormap, scene helpers, surface controls, surface — concatenated |
| from | seeded from the `js-demos/VolSurface` sketch; **this copy is the real one** and is free to be reimplemented (its own header says so) |
| size | 20,069 bytes |
| served as | `VolSurfaceLib.INSTANCE` in `fin-dash-viz` |

Not third-party, so no provenance question — but it is bundled the same way
and exempt from the gates for the same reason (a rendering leaf). Edit it in
place; there is no upstream to pull from.
