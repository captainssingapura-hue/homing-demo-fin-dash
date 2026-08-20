# Adding a theme to fin-dash

A theme is a **cohesive palette swap** for the whole studio chrome (surfaces,
text, accent, borders, spacing, radius). This guide is the fin-dash-specific
**last mile** — where the theme lives in this multi-module project and how to
*wire it in* so it's actually reachable. For the palette-authoring craft (token
choices, contrast, dark mode, textures), the framework ships a thorough skill:
**`create-homing-theme`** (`homing-ssjs-core/skills/create-homing-theme/`). Read
that for depth; this doc gets you from zero to a working theme in fin-dash.

> **Worked example:** a **Bloomberg-terminal ("BBG") look** — near-black
> surfaces, amber text/accent, square corners, monospace body. Adapt the values;
> the wiring is identical for any theme.

## Mental model (30 seconds)

Every studio page renders through the same builders and CSS classes
(`st-card`, `st-header`, …), whose bodies reference **semantic tokens** —
`var(--color-surface)`, `var(--color-text-primary)`, `var(--space-4)`,
`var(--radius-md)`. A theme's only job is to **bind those tokens to values**.
The structural CSS is identical across themes and is reused verbatim
(`HomingDefault.STRUCTURAL_CSS`) — you write a value map + a dark-mode override,
nothing more (unless you want textures/fonts — the "layered" tier).

## Where things go in fin-dash

| Piece | Module / package |
|---|---|
| The theme(s) + registry | **`fin-dash-core`** → `…findash.core.theme` |
| The wiring (install the registry) | **`fin-dash-studio`** → `FinDashFixtures` |

`fin-dash-core` already depends on `homing-studio-starter`, which brings
`HomingDefault`, `StudioVars`, `StudioThemeRegistry`, and the `Theme` /
`ThemeVariables` / `ThemeGlobals` / `CssVar` types — no new dependency needed.

---

## Step 1 — Author the theme

Create `fin-dash-core/src/main/java/hue/captains/singapura/js/homing/findash/core/theme/FinDashTerminal.java`.
Bind **all ~15 colour tokens** (skip one and that role renders empty) plus the
spacing/radius scales. Here's the BBG-terminal palette — a single deliberately
dark identity (so the light and dark values are both dark):

```java
package hue.captains.singapura.js.homing.findash.core.theme;

import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.core.ThemeGlobals;
import hue.captains.singapura.js.homing.core.ThemeVariables;
import hue.captains.singapura.js.homing.studio.base.theme.HomingDefault;
import hue.captains.singapura.js.homing.studio.base.theme.StudioVars;

import java.util.Map;

/** BBG-terminal theme — near-black surfaces, amber text/accent, square corners,
 *  monospace body. A single dark identity (both modes stay dark). */
public record FinDashTerminal() implements Theme {

    public static final FinDashTerminal INSTANCE = new FinDashTerminal();

    @Override public String slug()  { return "fd-terminal"; }   // URL: ?theme=fd-terminal
    @Override public String label() { return "Terminal (amber)"; }

    public record Vars() implements ThemeVariables<FinDashTerminal> {
        public static final Vars INSTANCE = new Vars();
        @Override public FinDashTerminal theme() { return FinDashTerminal.INSTANCE; }
        @Override public Map<CssVar, String> values() { return VALUES; }

        private static final Map<CssVar, String> VALUES = Map.ofEntries(
                // Surfaces — near-black terminal
                Map.entry(StudioVars.COLOR_SURFACE,          "#0A0A0A"),
                Map.entry(StudioVars.COLOR_SURFACE_RAISED,   "#141414"),
                Map.entry(StudioVars.COLOR_SURFACE_RECESSED, "#000000"),
                Map.entry(StudioVars.COLOR_SURFACE_INVERTED, "#1C1C1C"),  // header band
                // Text — amber on black
                Map.entry(StudioVars.COLOR_TEXT_PRIMARY,           "#FF9900"),
                Map.entry(StudioVars.COLOR_TEXT_MUTED,             "#B0741A"),
                Map.entry(StudioVars.COLOR_TEXT_ON_INVERTED,       "#FFB84D"),
                Map.entry(StudioVars.COLOR_TEXT_ON_INVERTED_MUTED, "#C99A4A"),
                Map.entry(StudioVars.COLOR_TEXT_LINK,              "#4FC3F7"),  // functional cyan
                Map.entry(StudioVars.COLOR_TEXT_LINK_HOVER,        "#81D4FA"),
                // Borders
                Map.entry(StudioVars.COLOR_BORDER,          "#2A2A2A"),
                Map.entry(StudioVars.COLOR_BORDER_EMPHASIS, "#FF9900"),
                // Accent — amber
                Map.entry(StudioVars.COLOR_ACCENT,          "#FF9900"),
                Map.entry(StudioVars.COLOR_ACCENT_EMPHASIS, "#FFB74D"),
                Map.entry(StudioVars.COLOR_ACCENT_ON,       "#000000"),  // black text on amber
                // Spacing — copied verbatim
                Map.entry(StudioVars.SPACE_1, "4px"),  Map.entry(StudioVars.SPACE_2, "8px"),
                Map.entry(StudioVars.SPACE_3, "12px"), Map.entry(StudioVars.SPACE_4, "16px"),
                Map.entry(StudioVars.SPACE_5, "20px"), Map.entry(StudioVars.SPACE_6, "24px"),
                Map.entry(StudioVars.SPACE_7, "32px"), Map.entry(StudioVars.SPACE_8, "40px"),
                // Radius — 0px for the terminal square look
                Map.entry(StudioVars.RADIUS_SM, "0px"),
                Map.entry(StudioVars.RADIUS_MD, "0px"),
                Map.entry(StudioVars.RADIUS_LG, "0px")
        );
    }

    public record Globals() implements ThemeGlobals<FinDashTerminal> {
        public static final Globals INSTANCE = new Globals();
        @Override public FinDashTerminal theme() { return FinDashTerminal.INSTANCE; }
        // Order: dark-override, then the shared structural CSS, then our overlay.
        @Override public String css() { return DARK_OVERRIDE + HomingDefault.STRUCTURAL_CSS + OVERLAY; }

        // Single-identity dark theme: keep the same values under the dark @media.
        private static final String DARK_OVERRIDE = """
                :root { color-scheme: dark; }
                @media (prefers-color-scheme: dark) {
                    :root {
                        --color-surface:           #0A0A0A;
                        --color-surface-raised:    #141414;
                        --color-surface-recessed:  #000000;
                        --color-surface-inverted:  #1C1C1C;
                        --color-text-primary:            #FF9900;
                        --color-text-muted:              #B0741A;
                        --color-text-on-inverted:        #FFB84D;
                        --color-text-on-inverted-muted:  #C99A4A;
                        --color-text-link:               #4FC3F7;
                        --color-text-link-hover:         #81D4FA;
                        --color-border:           #2A2A2A;
                        --color-border-emphasis:  #FF9900;
                        --color-accent:           #FF9900;
                        --color-accent-emphasis:  #FFB74D;
                        --color-accent-on:        #000000;
                    }
                }
                """;

        // Tier-3 overlay: monospace body — the terminal feel. Must come AFTER the
        // structural CSS (its `background: var(--color-surface)` shorthand would
        // otherwise clear anything we set here).
        private static final String OVERLAY = """
                html, body {
                    font-family: "Cascadia Mono", "Consolas", "SF Mono", "Liberation Mono", monospace;
                }
                """;
    }
}
```

## Step 2 — A fin-dash registry that keeps the framework themes

A downstream installs its *own* `ThemeRegistry`. To **keep the framework's 9
themes and add yours**, compose them (the framework has no `compose()` helper
yet, so concatenate the three lists). Create
`fin-dash-core/.../findash/core/theme/FinDashThemes.java`:

```java
package hue.captains.singapura.js.homing.findash.core.theme;

import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.core.ThemeGlobals;
import hue.captains.singapura.js.homing.core.ThemeVariables;
import hue.captains.singapura.js.homing.server.ThemeRegistry;
import hue.captains.singapura.js.homing.studio.base.theme.StudioThemeRegistry;

import java.util.List;
import java.util.stream.Stream;

/** The framework's themes plus fin-dash's own. Slugs must stay unique. */
public final class FinDashThemes implements ThemeRegistry {

    public static final FinDashThemes INSTANCE = new FinDashThemes();

    private static final ThemeRegistry BASE = StudioThemeRegistry.INSTANCE;
    private static final List<Theme>              OWN_THEMES = List.of(FinDashTerminal.INSTANCE);
    private static final List<ThemeVariables<?>>  OWN_VARS   = List.of(FinDashTerminal.Vars.INSTANCE);
    private static final List<ThemeGlobals<?>>    OWN_GLOBALS= List.of(FinDashTerminal.Globals.INSTANCE);

    @Override public List<Theme> themes() {
        return Stream.concat(BASE.themes().stream(), OWN_THEMES.stream()).toList();
    }
    @Override public List<ThemeVariables<?>> variables() {
        return Stream.concat(BASE.variables().stream(), OWN_VARS.stream()).toList();
    }
    @Override public List<ThemeGlobals<?>> globals() {
        return Stream.concat(BASE.globals().stream(), OWN_GLOBALS.stream()).toList();
    }
}
```

> To ship **only** your own themes (drop the framework's), return `OWN_*`
> directly instead of concatenating with `BASE`.

## Step 3 — Wire it into the studio (the one line that matters)

This is the step the framework skill omits. `FinDashFixtures` (in
`fin-dash-studio`) already overrides `servableModuleClasses()`; add the theme
hooks next to it:

```java
import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.findash.core.theme.FinDashThemes;
import hue.captains.singapura.js.homing.findash.core.theme.FinDashTerminal;
import hue.captains.singapura.js.homing.server.ThemeRegistry;

@Override
public ThemeRegistry themeRegistry() {
    return FinDashThemes.INSTANCE;                 // framework themes + fin-dash's
}

@Override
public Theme defaultTheme() {
    return FinDashTerminal.INSTANCE;               // optional: open on the terminal look
}
```

Without the `themeRegistry()` override the theme compiles but is **never
reachable** — the server keeps serving `StudioThemeRegistry` (the default). The
`defaultTheme()` override is optional (omit it to keep `Default` active and let
users pick Terminal from the picker).

## Step 4 — Verify

```bash
mvn -o install                 # or: mvn -o -pl fin-dash-core,fin-dash-studio -am install
# then run the umbrella server (fin-dash-studio) and open the browser:
```

- Visit `/app?app=themes` — **Terminal (amber)** appears as a swatch row alongside the framework themes.
- Append `?theme=fd-terminal` to any URL, or pick it from the header picker — the chrome retints to black/amber, corners go square, body goes monospace.
- The header band uses `--color-surface-inverted`; links render cyan.

## Pitfalls (fin-dash-specific + general)

- **Forgetting Step 3.** The single most common miss: the theme is authored and registered but `FinDashFixtures.themeRegistry()` isn't overridden, so it never shows. Symptom: `mvn install` passes, but the picker doesn't list it.
- **Slug collision.** Each theme owns one slug forever; don't reuse `default` etc. (composing two registries with the same slug silently shadows one — keep fin-dash slugs prefixed, e.g. `fd-`).
- **Bind every colour token.** A missing token renders that role empty.
- **Status colours are NOT chrome tokens.** The UI-study's P2 language — breach / stale / degraded / VaR-limit — is a *widget* concern (a typed CSS-group with its own tokens consumed via `css.addClass`), not part of the chrome theme. A theme can *hold* those values (define extra `CssVar`s in `Vars`), but the widgets must reference them; reskinning the chrome alone won't recolour the status chips. See the `create-homing-theme` skill and the `css_type_safety` pattern.
- **Overlay ordering.** A tier-3 overlay (fonts/textures) must come *after* `HomingDefault.STRUCTURAL_CSS` in `css()`, or the structural `background: var(--color-surface)` shorthand clears it.

## See also

- `homing-ssjs-core/skills/create-homing-theme/SKILL.md` — the full authoring craft (token table with light/dark examples, contrast rules, three tiers, texture cookbook, reference themes to copy).
- Reference themes to copy the *shape* from: `HomingForest` (basic), `HomingBauhaus` (square corners), `HomingLetterpress` (textures + serif body).
