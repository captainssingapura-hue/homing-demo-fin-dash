package hue.captains.singapura.js.homing.findash.studio.docs;

import java.util.UUID;

/**
 * The pricing &amp; risk engine architecture — the domain context behind the
 * screens: data model, Greeks, two-speed risk, quality flags. Served verbatim
 * as the self-styled HTML page it was authored as.
 */
public record EngineArchitectureDoc() implements HtmlResourceDoc {

    private static final UUID ID = UUID.fromString("b8d1e6f2-4a07-49c3-9b58-2c93a1d7e044");

    public static final EngineArchitectureDoc INSTANCE = new EngineArchitectureDoc();

    @Override public UUID   uuid()     { return ID; }
    @Override public String title()    { return "Engine Architecture"; }
    @Override public String summary()  { return "The live FX options pricing & risk engine behind the screens: "
            + "market slices and epochs, Greeks, two-speed risk, quality flags."; }
    @Override public String category() { return "DESIGN DOC"; }

    @Override public String resourcePath() { return "findash-docs/fx-options-engine-architecture.html"; }
}
