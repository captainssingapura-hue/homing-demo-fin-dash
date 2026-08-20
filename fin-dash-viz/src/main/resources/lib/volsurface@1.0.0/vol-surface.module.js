// VolSurface — the 3D implied-vol renderer for the FXO desk.
//
// Seeded from the js-demos/VolSurface sketch (colormap -> scene-helpers ->
// surface-controls -> vol-surface, concatenated; the inter-file imports are
// dropped because the definitions now share a scope), then maintained here.
// js-demos is a sketchbook, not an upstream — this copy is the real one and is
// free to be reimplemented. It already diverges: resize() is local.
//
// Served verbatim as a BundledExternalModule, which classifies it
// BUNDLED_EXTERNAL and so exempts it from the JS rule sets. The reason is the
// RENDERING LEAF boundary, not authorship: nothing outside a chart addresses
// its geometry by name, so DomOpsParty's composition guarantees buy nothing
// inside it. What the exemption costs is paid back by the widget, which owns
// the branch-owned mount and calls destroy() from partyDeregister.

// ==================== colormap.js ====================
/* colormap.js — Colormap definitions and interpolation */

export const COLORMAPS = {
  plasma:   [[13,8,135],[84,2,163],[139,10,165],[185,50,137],[219,92,104],[244,136,73],[254,188,43],[240,249,33]],
  viridis:  [[68,1,84],[72,40,120],[62,83,160],[49,104,142],[38,130,142],[31,158,137],[53,183,121],[110,206,88],[181,222,43],[253,231,37]],
  rdylgn:   [[165,0,38],[215,48,39],[244,109,67],[253,174,97],[254,224,139],[255,255,191],[217,239,139],[166,217,106],[102,189,99],[26,152,80],[0,104,55]],
  spectral: [[158,1,66],[213,62,79],[244,109,67],[253,174,97],[254,224,139],[255,255,191],[230,245,152],[171,221,164],[102,194,165],[50,136,189],[94,79,162]],
};

/**
 * Interpolate a colormap at position t (0–1). Returns [r, g, b] in 0–255 range.
 */
export function sampleColormap(t, name) {
  const c = COLORMAPS[name];
  const idx = t * (c.length - 1);
  const lo = Math.floor(idx);
  const hi = Math.min(lo + 1, c.length - 1);
  const f  = idx - lo;
  return [
    c[lo][0] * (1 - f) + c[hi][0] * f,
    c[lo][1] * (1 - f) + c[hi][1] * f,
    c[lo][2] * (1 - f) + c[hi][2] * f,
  ];
}

// ==================== scene-helpers.js ====================
/* scene-helpers.js — Three.js geometry and axis construction helpers */


/**
 * Build a BufferGeometry for the vol surface grid.
 * @param {object}     THREE    - Three.js namespace
 * @param {number[][]} grid     - N_T × N_D vol values
 * @param {number}     N_T      - number of tenor points
 * @param {number}     N_D      - number of delta points
 * @param {string}     colormap - colormap name
 * @returns {THREE.BufferGeometry}
 */
export function buildSurfaceGeometry(THREE, grid, N_T, N_D, colormap) {
  const geo = new THREE.BufferGeometry();
  const positions = [], colors = [], uvs = [], indices = [];

  let vmin = Infinity, vmax = -Infinity;
  grid.forEach(row => row.forEach(v => {
    if (v < vmin) vmin = v;
    if (v > vmax) vmax = v;
  }));

  for (let ti = 0; ti < N_T; ti++) {
    for (let di = 0; di < N_D; di++) {
      const x = (ti / (N_T - 1)) * 4 - 2;
      const z = (di / (N_D - 1)) * 4 - 2;
      const v = grid[ti][di];
      const y = (v - 7) * 0.18;
      positions.push(x, y, z);

      const t = (v - vmin) / (vmax - vmin + 0.001);
      const [r, g, b] = sampleColormap(t, colormap);
      colors.push(r / 255, g / 255, b / 255);
      uvs.push(ti / (N_T - 1), di / (N_D - 1));
    }
  }

  for (let ti = 0; ti < N_T - 1; ti++) {
    for (let di = 0; di < N_D - 1; di++) {
      const a = ti * N_D + di;
      const b = a + 1;
      const c = (ti + 1) * N_D + di;
      const d = c + 1;
      indices.push(a, b, c, b, d, c);
    }
  }

  geo.setAttribute('position', new THREE.Float32BufferAttribute(positions, 3));
  geo.setAttribute('color',    new THREE.Float32BufferAttribute(colors,    3));
  geo.setAttribute('uv',       new THREE.Float32BufferAttribute(uvs,       2));
  geo.setIndex(indices);
  geo.computeVertexNormals();
  return geo;
}

/**
 * Add axis lines, labels (sprites), and floor grid to the scene.
 */
export function buildAxes(THREE, scene, tenors, deltas) {
  const N_T = tenors.length;
  const N_D = deltas.length;
  const lineMat = new THREE.LineBasicMaterial({ color: 0x555566 });

  const line = pts => {
    const g = new THREE.BufferGeometry().setFromPoints(
      pts.map(([x, y, z]) => new THREE.Vector3(x, y, z))
    );
    scene.add(new THREE.Line(g, lineMat));
  };

  line([[-2.3, -0.5, -2], [2.3, -0.5, -2]]);
  line([[-2, -0.5, -2.3], [-2, -0.5, 2.3]]);
  line([[-2, -0.5, -2],   [-2,  1.5, -2]]);

  const sprite = (text, [x, y, z], scaleX = 0.72, scaleY = 0.18) => {
    const canvas = document.createElement('canvas');
    canvas.width  = 256;
    canvas.height = 64;
    const ctx = canvas.getContext('2d');
    ctx.clearRect(0, 0, 256, 64);
    ctx.fillStyle = 'rgba(180,180,210,0.92)';
    ctx.font = "bold 28px 'IBM Plex Mono', monospace";
    ctx.textAlign = 'center';
    ctx.fillText(text, 128, 44);
    const tex = new THREE.CanvasTexture(canvas);
    const mat = new THREE.SpriteMaterial({ map: tex, transparent: true, depthTest: false });
    const sp  = new THREE.Sprite(mat);
    sp.position.set(x, y, z);
    sp.scale.set(scaleX, scaleY, 1);
    scene.add(sp);
  };

  if (tenors.length) {
    tenors.forEach((t, i) => {
      const x = (i / (N_T - 1)) * 4 - 2;
      sprite(t, [x, -0.75, 2.45]);
    });
  }
  if (deltas.length) {
    deltas.forEach((d, i) => {
      const z = (i / (N_D - 1)) * 4 - 2;
      sprite(d + '\u0394', [-2.7, -0.6, z]);
    });
  }
  sprite('Maturity', [0, -0.95, 2.75], 1.0, 0.22);
  sprite('Delta',    [-3.2, -0.5, 0],  0.9, 0.22);
  sprite('IV %',     [-2.4, 0.7, -2.35], 0.72, 0.2);

  const gridMat = new THREE.LineBasicMaterial({ color: 0x1e1e3a, transparent: true, opacity: 0.7 });
  for (let ti = 0; ti < N_T; ti++) {
    const x = (ti / (N_T - 1)) * 4 - 2;
    const pts = Array.from({ length: N_D }, (_, di) =>
      new THREE.Vector3(x, -0.5, (di / (N_D - 1)) * 4 - 2)
    );
    scene.add(new THREE.Line(new THREE.BufferGeometry().setFromPoints(pts), gridMat));
  }
  for (let di = 0; di < N_D; di++) {
    const z = (di / (N_D - 1)) * 4 - 2;
    const pts = Array.from({ length: N_T }, (_, ti) =>
      new THREE.Vector3((ti / (N_T - 1)) * 4 - 2, -0.5, z)
    );
    scene.add(new THREE.Line(new THREE.BufferGeometry().setFromPoints(pts), gridMat));
  }
}

// ==================== surface-controls.js ====================
/* surface-controls.js — Mouse interaction for VolSurface
   Orbit, pan, zoom, and raycaster tooltip — all as closures over
   the surface instance so the class stays lean.
*/

/**
 * Attach mouse controls to a VolSurface instance.
 * Returns a detach function for cleanup.
 * @param {VolSurface} s - the surface instance
 */
export function attachControls(s) {
  const el = s._renderer.domElement;
  let isMouseDown = false, lastX = 0, lastY = 0, isRightClick = false;

  function onMouseDown(e) {
    isMouseDown  = true;
    lastX        = e.clientX;
    lastY        = e.clientY;
    isRightClick = e.button === 2;
    e.preventDefault();
  }

  function onMouseUp() {
    isMouseDown = false;
  }

  function onContextMenu(e) {
    e.preventDefault();
  }

  function onMouseMove(e) {
    // Orbit / pan when dragging
    if (isMouseDown) {
      const dx = e.clientX - lastX;
      const dy = e.clientY - lastY;
      lastX = e.clientX;
      lastY = e.clientY;
      if (isRightClick) {
        s._panX -= dx * 0.01;
        s._panY += dy * 0.01;
      } else {
        s._theta -= dx * 0.01;
        s._phi = Math.max(0.1, Math.min(Math.PI - 0.1, s._phi + dy * 0.01));
      }
      s._updateCamera();
    }

    // Tooltip (only when hovering our canvas)
    if (e.target !== el) return;

    const rect = el.getBoundingClientRect();
    s._mouse.x =  ((e.clientX - rect.left) / rect.width)  * 2 - 1;
    s._mouse.y = -((e.clientY - rect.top)  / rect.height) * 2 + 1;
    s._raycaster.setFromCamera(s._mouse, s._camera);

    if (!s._mesh) return;
    const hits = s._raycaster.intersectObject(s._mesh);
    if (hits.length > 0) {
      const pt  = hits[0].point;
      const N_T = s._tenors.length;
      const N_D = s._deltas.length;
      const ti  = Math.max(0, Math.min(N_T - 1, Math.round(((pt.x + 2) / 4) * (N_T - 1))));
      const di  = Math.max(0, Math.min(N_D - 1, Math.round(((pt.z + 2) / 4) * (N_D - 1))));

      if (s._volData[ti]?.[di] !== undefined) {
        const iv    = s._volData[ti][di];
        const tenor = s._tenors[ti];
        const delta = s._deltas[di];

        s._tooltipEl.innerHTML =
          `<b>${s._label}</b><br>` +
          `Tenor: ${tenor}<br>` +
          `Delta: ${delta}\u0394<br>` +
          `IV: <b>${iv.toFixed(2)}%</b>`;
        s._tooltipEl.style.display = 'block';
        s._tooltipEl.style.left    = (e.clientX - rect.left + 14) + 'px';
        s._tooltipEl.style.top     = (e.clientY - rect.top  - 12) + 'px';

        s._emit('hover', { tenor, delta, iv });
      }
    } else {
      s._tooltipEl.style.display = 'none';
      s._emit('hover', null);
    }
  }

  function onWheel(e) {
    s._radius = Math.max(4, Math.min(20, s._radius + e.deltaY * 0.02));
    s._updateCamera();
    e.preventDefault();
  }

  function onMouseLeave() {
    s._tooltipEl.style.display = 'none';
    s._emit('hover', null);
  }

  function onResize() {
    s._camera.aspect = s._container.clientWidth / s._container.clientHeight;
    s._camera.updateProjectionMatrix();
    s._renderer.setSize(s._container.clientWidth, s._container.clientHeight);
  }

  // Observe container resizes (manual drag-resize + window resize)
  const resizeObserver = new ResizeObserver(() => onResize());
  resizeObserver.observe(s._container);

  // Attach
  el.addEventListener('mousedown',   onMouseDown);
  el.addEventListener('contextmenu', onContextMenu);
  el.addEventListener('wheel',       onWheel, { passive: false });
  el.addEventListener('mousemove',   onMouseMove);
  el.addEventListener('mouseleave',  onMouseLeave);
  window.addEventListener('mouseup',   onMouseUp);
  window.addEventListener('mousemove', onMouseMove);

  // Return detach function
  return function detach() {
    resizeObserver.disconnect();
    el.removeEventListener('mousedown',   onMouseDown);
    el.removeEventListener('contextmenu', onContextMenu);
    el.removeEventListener('wheel',       onWheel);
    el.removeEventListener('mousemove',   onMouseMove);
    el.removeEventListener('mouseleave',  onMouseLeave);
    window.removeEventListener('mouseup',   onMouseUp);
    window.removeEventListener('mousemove', onMouseMove);
  };
}

// ==================== vol-surface.js ====================
/* vol-surface.js — FX Implied Volatility Surface Library
   ES Module · Three.js is a peer dependency (pass via options.THREE)
*/


// Re-export colormap utilities for library consumers

export class VolSurface {
  /**
   * @param {HTMLElement} container  - Element to render into
   * @param {object}      options
   * @param {object}      options.THREE     - Three.js namespace (peer dep)
   * @param {string}      [options.colormap='plasma']
   * @param {boolean}     [options.wireframe=false]
   */
  constructor(container, options = {}) {
    const { THREE, colormap = 'plasma', wireframe = false, provider, initialData } = options;
    if (!THREE) throw new Error('VolSurface: options.THREE is required');

    this._THREE     = THREE;
    this._container = container;
    this._colormap  = colormap;
    this._wireframe = wireframe;

    // Provider (factory → instance)
    this._providerFactory = provider || null;
    this._provider        = null;
    this._initialData     = initialData || null;

    // Vol data
    this._volData = null;
    this._tenors  = [];
    this._deltas  = [];
    this._label   = '';

    // Camera state
    this._phi    = 0.65;
    this._theta  = 0.7;
    this._radius = 10;
    this._panX   = 0;
    this._panY   = 0;

    // Event handlers map
    this._handlers = { hover: [] };

    this._animFrameId   = null;
    this._mesh          = null;
    this._wireframeMesh = null;
    this._detachControls = null;

    this._init();
  }

  // ── Public API ────────────────────────────────────────────────────────────

  /**
   * Set the volatility surface data and re-render.
   * @param {object} data
   * @param {number[][]} data.grid    - N_T × N_D array of IV % values
   * @param {string[]}   data.tenors  - tenor labels
   * @param {number[]}   data.deltas  - delta values
   * @param {string}     [data.label] - display label
   */
  setData({ grid, tenors, deltas, label = '' }) {
    this._volData = grid;
    this._tenors  = tenors;
    this._deltas  = deltas;
    this._label   = label;
    this._rebuildSurface();
    this._buildLegend();
  }

  setColormap(name) {
    this._colormap = name;
    if (this._volData) {
      this._rebuildSurface();
      this._buildLegend();
    }
  }

  setWireframe(enabled) {
    this._wireframe = enabled;
    if (this._wireframeMesh) this._wireframeMesh.visible = enabled;
  }

  /**
   * LOCAL ADDITION (not in the upstream js-demos/VolSurface).
   *
   * Upstream measures the container once in the constructor and never again,
   * which is fine for the fixed-size <div> in its own demo but wrong inside a
   * dockable pane. Without this, the only way to follow a resize is to destroy
   * and reconstruct — and reconstruction runs _init(), which builds a NEW
   * WebGLRenderer: a fresh GL context, shader compilation, buffer allocation.
   * That is far too expensive to run at any interactive rate.
   *
   * This is the operation Three.js actually intends for a resize. It touches
   * no geometry and creates no context, so it is cheap enough to drive
   * straight from a resize observer.
   *
   * @param {number} width
   * @param {number} height
   */
  resize(width, height) {
    if (!(width > 0 && height > 0)) return;
    if (!this._renderer || !this._camera) return;
    this._renderer.setSize(width, height);
    this._camera.aspect = width / height;
    this._camera.updateProjectionMatrix();
  }

  on(event, handler) {
    if (this._handlers[event]) this._handlers[event].push(handler);
  }

  setBaseline(data) {
    if (!this._provider) throw new Error('VolSurface: no provider configured');
    this._provider.setBaseline(data);
  }

  destroy() {
    if (this._provider) {
      this._provider.destroy();
      this._provider = null;
    }
    cancelAnimationFrame(this._animFrameId);
    if (this._detachControls) this._detachControls();

    if (this._mesh) {
      this._mesh.geometry.dispose();
      this._mesh.material.dispose();
    }
    if (this._wireframeMesh) {
      this._wireframeMesh.geometry.dispose();
      this._wireframeMesh.material.dispose();
    }
    this._renderer.dispose();
    this._container.removeChild(this._renderer.domElement);
    if (this._tooltipEl) this._container.removeChild(this._tooltipEl);
    if (this._legendEl)  this._container.removeChild(this._legendEl);
  }

  // ── Private ───────────────────────────────────────────────────────────────

  _init() {
    const THREE = this._THREE;
    const container = this._container;

    this._scene = new THREE.Scene();
    this._scene.background = new THREE.Color(0x0d0d14);

    this._camera = new THREE.PerspectiveCamera(
      45, container.clientWidth / container.clientHeight, 0.1, 100
    );
    this._raycaster = new THREE.Raycaster();
    this._mouse     = new THREE.Vector2();

    this._renderer = new THREE.WebGLRenderer({ antialias: true });
    this._renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
    this._renderer.setSize(container.clientWidth, container.clientHeight);
    container.appendChild(this._renderer.domElement);

    // Lighting
    this._scene.add(new THREE.AmbientLight(0xffffff, 0.4));
    const dir = new THREE.DirectionalLight(0xffffff, 0.8);
    dir.position.set(5, 8, 5);
    this._scene.add(dir);
    const dir2 = new THREE.DirectionalLight(0x8888ff, 0.3);
    dir2.position.set(-5, 3, -5);
    this._scene.add(dir2);

    // Tooltip element
    this._tooltipEl = document.createElement('div');
    Object.assign(this._tooltipEl.style, {
      position: 'absolute', display: 'none', pointerEvents: 'none',
      background: 'rgba(10,10,24,0.92)', color: '#c8c8e8',
      border: '1px solid #2a2a5a', borderRadius: '6px',
      padding: '8px 12px', fontSize: '12px', fontFamily: 'IBM Plex Mono, monospace',
      lineHeight: '1.6', zIndex: '10',
    });
    container.appendChild(this._tooltipEl);

    // Legend element
    this._legendEl = document.createElement('canvas');
    this._legendEl.width  = 160;
    this._legendEl.height = 12;
    Object.assign(this._legendEl.style, {
      position: 'absolute', bottom: '8px', left: '50%',
      transform: 'translateX(-50%)', display: 'block', pointerEvents: 'none',
    });
    container.appendChild(this._legendEl);

    this._detachControls = attachControls(this);
    this._updateCamera();
    this._buildAxesOnce();
    this._animate();

    // Connect provider and load first snapshot
    if (this._providerFactory) {
      this._provider = this._providerFactory(data => this.setData(data));
      if (this._initialData) {
        this._provider.setBaseline(this._initialData);
        this._initialData = null;
      }
    }
  }

  _buildAxesOnce() {
    buildAxes(this._THREE, this._scene, this._tenors, this._deltas);
  }

  _rebuildSurface() {
    const THREE = this._THREE;
    if (this._mesh) {
      this._scene.remove(this._mesh);
      this._mesh.geometry.dispose();
      this._mesh.material.dispose();
    }
    if (this._wireframeMesh) {
      this._scene.remove(this._wireframeMesh);
      this._wireframeMesh.geometry.dispose();
      this._wireframeMesh.material.dispose();
    }

    const N_T = this._tenors.length;
    const N_D = this._deltas.length;
    const geo = buildSurfaceGeometry(THREE, this._volData, N_T, N_D, this._colormap);

    this._mesh = new THREE.Mesh(geo, new THREE.MeshPhongMaterial({
      vertexColors: true, side: THREE.DoubleSide,
      shininess: 60, specular: new THREE.Color(0x333366),
    }));
    this._scene.add(this._mesh);

    this._wireframeMesh = new THREE.Mesh(geo.clone(), new THREE.MeshBasicMaterial({
      color: 0x1a1a40, wireframe: true, transparent: true, opacity: 0.4,
    }));
    this._wireframeMesh.visible = this._wireframe;
    this._scene.add(this._wireframeMesh);
  }

  _buildLegend() {
    if (!this._legendEl) return;
    const ctx = this._legendEl.getContext('2d');
    for (let i = 0; i < 160; i++) {
      const [r, g, b] = sampleColormap(i / 159, this._colormap);
      ctx.fillStyle = `rgb(${Math.round(r)},${Math.round(g)},${Math.round(b)})`;
      ctx.fillRect(i, 0, 1, 12);
    }
  }

  _updateCamera() {
    const x = this._radius * Math.sin(this._phi) * Math.sin(this._theta) + this._panX;
    const y = this._radius * Math.cos(this._phi) + this._panY;
    const z = this._radius * Math.sin(this._phi) * Math.cos(this._theta);
    this._camera.position.set(x, y, z);
    this._camera.lookAt(this._panX, this._panY, 0);
  }

  _emit(event, data) {
    (this._handlers[event] || []).forEach(fn => fn(data));
  }

  _animate() {
    this._animFrameId = requestAnimationFrame(() => this._animate());
    this._renderer.render(this._scene, this._camera);
  }
}

