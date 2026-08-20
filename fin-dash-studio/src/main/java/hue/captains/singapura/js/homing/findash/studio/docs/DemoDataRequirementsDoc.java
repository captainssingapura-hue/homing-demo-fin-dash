package hue.captains.singapura.js.homing.findash.studio.docs;

import java.util.UUID;

/**
 * The consolidated demo-dataset requirements — the spec for replacing the
 * per-feed data fragments with one closed, UI-agnostic entity graph: the
 * substrate for connecting widgets across workspaces. Served from the repo's
 * canonical {@code docs/demo-data-requirements.md}.
 */
public record DemoDataRequirementsDoc() implements MarkdownResourceDoc {

    private static final UUID ID = UUID.fromString("5c4b93a7-2e18-4d60-9f31-7a05b8e6c223");

    public static final DemoDataRequirementsDoc INSTANCE = new DemoDataRequirementsDoc();

    @Override public UUID   uuid()     { return ID; }
    @Override public String title()    { return "Demo Data Requirements"; }
    @Override public String summary()  { return "The consolidated desk dataset: one closed entity graph with stable ids, "
            + "structured lineage, and a ten-key join contract — what makes cross-widget, "
            + "cross-workspace connection possible."; }
    @Override public String category() { return "SPEC"; }

    @Override public String resourcePath() { return "findash-docs/demo-data-requirements.md"; }
}
