package hue.captains.singapura.js.homing.findash.studio;

import hue.captains.singapura.js.homing.studio.base.Studio;
import hue.captains.singapura.js.homing.studio.base.app.StudioBrand;

/**
 * The fin-dash studio identity: landing catalogue + brand. Deliberately thin —
 * the real UI is the {@code "risk-dashboard"} {@code WorkspaceSpec}; this exists
 * to satisfy Bootstrap and brand the deploy.
 */
public record RiskDashboardStudio() implements Studio<RiskDashboardLandingCatalogue> {

    public static final RiskDashboardStudio INSTANCE = new RiskDashboardStudio();

    @Override
    public RiskDashboardLandingCatalogue home() { return RiskDashboardLandingCatalogue.INSTANCE; }

    @Override
    public StudioBrand standaloneBrand() {
        return new StudioBrand("FX Options Risk Dashboard", RiskDashboardLandingCatalogue.class);
    }
}
