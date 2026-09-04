package hue.captains.singapura.js.homing.findash.studio.docs;

import java.util.UUID;

/**
 * The first downstream consumer of RFC 0050's {@code RelationGrid}, written up
 * as a case study: what a grouped risk ladder needed, what the grid owns, what
 * the domain owns, and the three times we concluded the grid needed changing
 * and were wrong.
 *
 * <p>Served from the repo's canonical
 * {@code docs/relation-grid-risk-ladder.md}, so the studio and the repo cannot
 * drift.</p>
 */
public record RelationGridCaseStudyDoc() implements MarkdownResourceDoc {

    private static final UUID ID = UUID.fromString("c7e41b06-2f58-4d9a-9c33-6a1e5b70d284");

    public static final RelationGridCaseStudyDoc INSTANCE = new RelationGridCaseStudyDoc();

    @Override public UUID   uuid()     { return ID; }
    @Override public String title()    { return "Case Study :: A Risk Ladder on RelationGrid"; }
    @Override public String summary()  { return "Grouped rows, subtotals and folding on a grid with none of those "
            + "concepts — and why almost nothing had to be added to it."; }
    @Override public String category() { return "CASE STUDY"; }

    @Override public String resourcePath() { return "findash-docs/relation-grid-risk-ladder.md"; }
}
