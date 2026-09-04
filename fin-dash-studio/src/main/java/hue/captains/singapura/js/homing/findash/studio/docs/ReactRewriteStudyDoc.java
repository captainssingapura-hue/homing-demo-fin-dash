package hue.captains.singapura.js.homing.findash.studio.docs;

import java.util.UUID;

/**
 * A study of what React development would cost for these papers, framed as
 * one side of a cold-start comparison: the stack a React team must source,
 * the measured dependency trees and their advisories, the second build
 * system, what of the papers' rules can be enforced, and the ownership
 * premise — then the experiment that would settle it.
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
    @Override public String title()    { return "Study :: The Desk in React, Cold Start"; }
    @Override public String summary()  { return "The React side of a cold-start ledger: what a team building the "
            + "desk from the papers would pay — in stack, packages, advisories, build systems, enforcement and "
            + "ownership — and the experiment that settles it."; }
    @Override public String category() { return "STUDY"; }

    @Override public String resourcePath() { return "findash-docs/react-rewrite-cost.md"; }
}
