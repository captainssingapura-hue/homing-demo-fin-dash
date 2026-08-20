package hue.captains.singapura.js.homing.findash.studio;

import hue.captains.singapura.js.homing.studio.base.Studio;
import hue.captains.singapura.js.homing.studio.base.app.StudioBrand;

/**
 * The fin-dash studio identity: landing catalogue + brand. Deliberately thin —
 * the real UI is the set of persona workspaces (one {@code WorkspaceSpec} per
 * human actor in the UI study); this exists to satisfy Bootstrap and brand the
 * deploy.
 */
public record FinDashStudio() implements Studio<FinDashLandingCatalogue> {

    public static final FinDashStudio INSTANCE = new FinDashStudio();

    @Override
    public FinDashLandingCatalogue home() { return FinDashLandingCatalogue.INSTANCE; }

    @Override
    public StudioBrand standaloneBrand() {
        return new StudioBrand("FX Options Desk", FinDashLandingCatalogue.class);
    }
}
