package hue.captains.singapura.js.homing.findash.studio;

import hue.captains.singapura.js.homing.findash.studio.docs.DemoDataRequirementsDoc;
import hue.captains.singapura.js.homing.findash.studio.docs.EngineArchitectureDoc;
import hue.captains.singapura.js.homing.findash.studio.docs.ThemeGuideDoc;
import hue.captains.singapura.js.homing.findash.studio.docs.UiStudyDoc;
import hue.captains.singapura.js.homing.findash.studio.docs.UserGuideDoc;
import hue.captains.singapura.js.homing.studio.base.Doc;
import hue.captains.singapura.js.homing.studio.base.DocProvider;
import hue.captains.singapura.js.homing.studio.base.app.Entry;
import hue.captains.singapura.js.homing.studio.base.app.L1_Catalogue;

import java.util.List;

/**
 * The documentation branch of the fin-dash landing (L1 under
 * {@link FinDashLandingCatalogue}): the two design documents this demo is
 * built from — served verbatim as their self-styled HTML pages — and the
 * user guide, authored as a {@code ComposedDoc}. Implements
 * {@link DocProvider} so Bootstrap registers the prose docs in the
 * {@code DocRegistry} (catalogue leaves alone only harvest synthetic docs).
 */
public record FinDashDocCatalogue()
        implements L1_Catalogue<FinDashLandingCatalogue, FinDashDocCatalogue>, DocProvider {

    public static final FinDashDocCatalogue INSTANCE = new FinDashDocCatalogue();

    @Override public FinDashLandingCatalogue parent() { return FinDashLandingCatalogue.INSTANCE; }
    @Override public String name()    { return "Documentation"; }
    @Override public String summary() { return "The design docs this desk is built from, and the user guide."; }
    @Override public String badge()   { return "DOC"; }
    @Override public String icon()    { return "📖"; }

    @Override
    public List<Entry<FinDashDocCatalogue>> leaves() {
        return List.of(
                Entry.of(this, UserGuideDoc.INSTANCE),
                Entry.of(this, UiStudyDoc.INSTANCE),
                Entry.of(this, EngineArchitectureDoc.INSTANCE),
                Entry.of(this, DemoDataRequirementsDoc.INSTANCE),
                Entry.of(this, ThemeGuideDoc.INSTANCE)
        );
    }

    @Override
    public List<Doc> docs() {
        return List.of(UserGuideDoc.INSTANCE, UiStudyDoc.INSTANCE,
                EngineArchitectureDoc.INSTANCE, DemoDataRequirementsDoc.INSTANCE,
                ThemeGuideDoc.INSTANCE);
    }
}
