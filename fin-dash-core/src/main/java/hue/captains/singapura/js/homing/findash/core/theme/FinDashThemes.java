package hue.captains.singapura.js.homing.findash.core.theme;

import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.core.ThemeGlobals;
import hue.captains.singapura.js.homing.core.ThemeVariables;
import hue.captains.singapura.js.homing.server.ThemeRegistry;
import hue.captains.singapura.js.homing.studio.base.theme.StudioThemeRegistry;

import java.util.List;
import java.util.stream.Stream;

/**
 * The fin-dash theme registry: <b>the framework's themes plus this demo's own.</b>
 *
 * <p>A downstream installs one registry, so keeping the framework's nine themes
 * means composing rather than replacing. The demo deliberately keeps them: a
 * desk that can only be looked at one way is a weaker demonstration than one
 * that retints wholesale, and the framework themes are the evidence that the
 * chrome is genuinely token-driven.</p>
 *
 * <p>Slugs are the join key and must stay unique across the composed list —
 * fin-dash prefixes its own with {@code fd-} so a future framework theme can
 * never silently shadow one of ours (or vice versa).</p>
 */
public final class FinDashThemes implements ThemeRegistry {

    public static final FinDashThemes INSTANCE = new FinDashThemes();

    private FinDashThemes() {}

    private static final ThemeRegistry BASE = StudioThemeRegistry.INSTANCE;

    private static final List<Theme> OWN_THEMES =
            List.of(FinDashTerminal.INSTANCE);
    private static final List<ThemeVariables<?>> OWN_VARS =
            List.of(FinDashTerminal.Vars.INSTANCE);
    private static final List<ThemeGlobals<?>> OWN_GLOBALS =
            List.of(FinDashTerminal.Globals.INSTANCE);

    @Override
    public List<Theme> themes() {
        return Stream.concat(BASE.themes().stream(), OWN_THEMES.stream()).toList();
    }

    @Override
    public List<ThemeVariables<?>> variables() {
        return Stream.concat(BASE.variables().stream(), OWN_VARS.stream()).toList();
    }

    @Override
    public List<ThemeGlobals<?>> globals() {
        return Stream.concat(BASE.globals().stream(), OWN_GLOBALS.stream()).toList();
    }
}
