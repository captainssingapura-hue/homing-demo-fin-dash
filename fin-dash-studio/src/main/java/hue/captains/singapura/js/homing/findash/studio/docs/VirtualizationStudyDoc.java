package hue.captains.singapura.js.homing.findash.studio.docs;

import java.util.UUID;

/**
 * A study on grid virtualization as a pseudo-requirement — a requirement on
 * a mechanism (rows the grid can render) standing in for a requirement on an
 * outcome (what the reader needs to see, and how they get to it).
 *
 * <p>Companion to {@link RelationGridCaseStudyDoc}, whose §10 first raised the
 * question. Served from the repo's canonical
 * {@code docs/virtualization-pseudo-requirement.md}, so the studio and the
 * repo cannot drift.</p>
 */
public record VirtualizationStudyDoc() implements MarkdownResourceDoc {

    private static final UUID ID = UUID.fromString("5d2f8a41-9c6b-4e07-b3a5-1f7e9d0c6b28");

    public static final VirtualizationStudyDoc INSTANCE = new VirtualizationStudyDoc();

    @Override public UUID   uuid()     { return ID; }
    @Override public String title()    { return "Study :: Virtualization is a Pseudo-Requirement"; }
    @Override public String summary()  { return "Why would a user scroll something too large for a browser to "
            + "handle? They would not — and what they are doing instead, the domain serves better."; }
    @Override public String category() { return "STUDY"; }

    @Override public String resourcePath() { return "findash-docs/virtualization-pseudo-requirement.md"; }
}
