package hue.captains.singapura.js.homing.findash.studio.docs;

import java.util.UUID;

/**
 * A study costing the whole demo against a React rewrite — the inventory of
 * what the desk is, what React replaces and what it leaves to be sourced, a
 * line-by-line day count, the measured dependency trees, and the finding that
 * React is not a dependency this demo could add but a different premise that
 * would replace the one it exists to validate.
 *
 * <p>Third of the series after {@link RelationGridCaseStudyDoc} and
 * {@link VirtualizationStudyDoc}. Served from the repo's canonical
 * {@code docs/react-rewrite-cost.md}, so the studio and the repo cannot
 * drift.</p>
 */
public record ReactRewriteStudyDoc() implements MarkdownResourceDoc {

    private static final UUID ID = UUID.fromString("a3c9e7b2-4d16-4f58-9e0a-7b2c5d8f1e64");

    public static final ReactRewriteStudyDoc INSTANCE = new ReactRewriteStudyDoc();

    @Override public UUID   uuid()     { return ID; }
    @Override public String title()    { return "Study :: What the Demo Would Cost in React"; }
    @Override public String summary()  { return "Eighty to a hundred and thirty-five developer-days, 552 packages "
            + "— and the premise the demo exists to show. Measured, not estimated."; }
    @Override public String category() { return "STUDY"; }

    @Override public String resourcePath() { return "findash-docs/react-rewrite-cost.md"; }
}
