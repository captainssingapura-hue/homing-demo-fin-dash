package hue.captains.singapura.js.homing.findash.studio.docs;

import java.util.UUID;

/**
 * How to add a theme to fin-dash — the last mile the framework's
 * {@code create-homing-theme} skill leaves out: where the theme lives in this
 * multi-module project, and the {@code themeRegistry()} override that makes it
 * reachable at all. Served from the repo's canonical
 * {@code docs/adding-a-theme.md}.
 */
public record ThemeGuideDoc() implements MarkdownResourceDoc {

    private static final UUID ID = UUID.fromString("a1f7d24e-6b93-4c05-8e71-3d92b40af618");

    public static final ThemeGuideDoc INSTANCE = new ThemeGuideDoc();

    @Override public UUID   uuid()     { return ID; }
    @Override public String title()    { return "Adding a Theme"; }
    @Override public String summary()  { return "Author a cohesive palette swap for the studio chrome and wire it in "
            + "so it is actually reachable — with a worked terminal-look example."; }
    @Override public String category() { return "HOW-TO"; }

    @Override public String resourcePath() { return "findash-docs/adding-a-theme.md"; }
}
