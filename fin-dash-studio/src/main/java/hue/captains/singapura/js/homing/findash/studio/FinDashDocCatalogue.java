package hue.captains.singapura.js.homing.findash.studio;

import hue.captains.singapura.js.homing.findash.studio.docs.DemoDataRequirementsDoc;
import hue.captains.singapura.js.homing.findash.studio.docs.EngineArchitectureDoc;
import hue.captains.singapura.js.homing.findash.studio.docs.RelationGridCaseStudyDoc;
import hue.captains.singapura.js.homing.findash.studio.docs.ThemeGuideDoc;
import hue.captains.singapura.js.homing.findash.studio.docs.UiStudyDoc;
import hue.captains.singapura.js.homing.findash.studio.docs.UserGuideDoc;
import hue.captains.singapura.js.homing.studio.base.Doc;
import hue.captains.singapura.js.homing.studio.base.DocProvider;
import hue.captains.singapura.js.homing.studio.base.app.DocReader;
import hue.captains.singapura.js.homing.studio.base.app.Entry;
import hue.captains.singapura.js.homing.studio.base.app.L1_Catalogue;
import hue.captains.singapura.js.homing.studio.base.app.L2_Catalogue;
import hue.captains.singapura.js.homing.studio.base.composed.ComposedViewer;

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

    /** The participant map, as one page per persona. */
    @Override
    public List<? extends L2_Catalogue<FinDashDocCatalogue, ?>> subCatalogues() {
        return List.of(WorkspaceIntrosCatalogue.INSTANCE);
    }

    /**
     * RFC 0053: an entry names the viewer that renders the doc. {@code Doc} no
     * longer carries a {@code url()} — "a doc no longer knows how it is viewed" —
     * so the choice of reader moves here, where the catalogue is authored.
     */
    @Override
    public List<Entry<FinDashDocCatalogue>> leaves() {
        return List.of(
                Entry.of(this, ComposedViewer.INSTANCE,
                        new ComposedViewer.Params(UserGuideDoc.INSTANCE.uuid().toString()),
                        UserGuideDoc.INSTANCE),
                Entry.of(this, DocReader.INSTANCE,
                        new DocReader.Params(UiStudyDoc.INSTANCE.uuid().toString()),
                        UiStudyDoc.INSTANCE),
                Entry.of(this, DocReader.INSTANCE,
                        new DocReader.Params(EngineArchitectureDoc.INSTANCE.uuid().toString()),
                        EngineArchitectureDoc.INSTANCE),
                Entry.of(this, DocReader.INSTANCE,
                        new DocReader.Params(DemoDataRequirementsDoc.INSTANCE.uuid().toString()),
                        DemoDataRequirementsDoc.INSTANCE),
                Entry.of(this, DocReader.INSTANCE,
                        new DocReader.Params(ThemeGuideDoc.INSTANCE.uuid().toString()),
                        ThemeGuideDoc.INSTANCE),
                Entry.of(this, DocReader.INSTANCE,
                        new DocReader.Params(RelationGridCaseStudyDoc.INSTANCE.uuid().toString()),
                        RelationGridCaseStudyDoc.INSTANCE)
        );
    }

    @Override
    public List<Doc> docs() {
        return List.of(UserGuideDoc.INSTANCE, UiStudyDoc.INSTANCE,
                EngineArchitectureDoc.INSTANCE, DemoDataRequirementsDoc.INSTANCE,
                ThemeGuideDoc.INSTANCE, RelationGridCaseStudyDoc.INSTANCE);
    }
}
