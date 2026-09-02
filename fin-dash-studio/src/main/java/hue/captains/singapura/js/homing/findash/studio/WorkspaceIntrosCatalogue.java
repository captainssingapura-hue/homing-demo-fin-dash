package hue.captains.singapura.js.homing.findash.studio;

import hue.captains.singapura.js.homing.findash.studio.docs.WorkspaceIntroDocs;
import hue.captains.singapura.js.homing.studio.base.Doc;
import hue.captains.singapura.js.homing.studio.base.DocProvider;
import hue.captains.singapura.js.homing.studio.base.app.Entry;
import hue.captains.singapura.js.homing.studio.base.app.L2_Catalogue;
import hue.captains.singapura.js.homing.studio.base.composed.ComposedDoc;
import hue.captains.singapura.js.homing.studio.base.composed.ComposedViewer;

import java.util.ArrayList;
import java.util.List;

/**
 * The participant map as reading material (L2 under {@link FinDashDocCatalogue}):
 * one introduction per persona workspace.
 *
 * <p>This is where the thirteen former <i>Workspaces</i> tiles went. As tiles
 * they were thirteen doors with nothing written on them; as documents they can
 * say what the desk is accountable for and what to try, and each one links into
 * its workspace at the end. The workspace itself is now a single leaf on the
 * landing — switching kind happens inside the app, from the workspace title.</p>
 *
 * <p>Implements {@link DocProvider} so Bootstrap registers the intros in the
 * {@code DocRegistry}: catalogue leaves alone only harvest synthetic docs.</p>
 */
public record WorkspaceIntrosCatalogue()
        implements L2_Catalogue<FinDashDocCatalogue, WorkspaceIntrosCatalogue>, DocProvider {

    public static final WorkspaceIntrosCatalogue INSTANCE = new WorkspaceIntrosCatalogue();

    @Override public FinDashDocCatalogue parent() { return FinDashDocCatalogue.INSTANCE; }
    @Override public String name()    { return "Workspace Intros"; }
    @Override public String summary() { return "One page per participant — what the desk owns, its screens, and what to try."; }
    @Override public String badge()   { return "INTRO"; }
    @Override public String icon()    { return "🧭"; }

    /**
     * RFC 0053: the entry names the viewer. Every intro is a {@code ComposedDoc},
     * so they all route through {@code ComposedViewer} — the doc itself no longer
     * carries a url.
     */
    @Override
    public List<Entry<WorkspaceIntrosCatalogue>> leaves() {
        var entries = new ArrayList<Entry<WorkspaceIntrosCatalogue>>();
        for (ComposedDoc d : WorkspaceIntroDocs.ALL) {
            entries.add(Entry.of(this, ComposedViewer.INSTANCE,
                    new ComposedViewer.Params(d.uuid().toString()), d));
        }
        return List.copyOf(entries);
    }

    @Override
    public List<Doc> docs() {
        return List.copyOf(WorkspaceIntroDocs.ALL);
    }
}
