package hue.captains.singapura.js.homing.findash.studio.docs;

import java.util.UUID;

/**
 * The FX Options UI Requirements Study — <b>the build target</b>: personas,
 * screens, wireframes W1–W6, principles P1–P5. Served verbatim as the
 * self-styled HTML page it was authored as.
 */
public record UiStudyDoc() implements HtmlResourceDoc {

    private static final UUID ID = UUID.fromString("7f2c5a10-93b4-4e1d-8c27-fd1e64a90b11");

    public static final UiStudyDoc INSTANCE = new UiStudyDoc();

    @Override public UUID   uuid()     { return ID; }
    @Override public String title()    { return "UI Requirements Study"; }
    @Override public String summary()  { return "The build target: screens, workflows, and entitlements for every participant "
            + "in the FX options stack — personas, wireframes W1–W6, cross-cutting principles P1–P5."; }
    @Override public String category() { return "DESIGN DOC"; }

    @Override public String resourcePath() { return "findash-docs/fx-options-ui-study.html"; }
}
