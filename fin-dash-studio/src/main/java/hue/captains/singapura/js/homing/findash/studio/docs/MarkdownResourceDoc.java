package hue.captains.singapura.js.homing.findash.studio.docs;

import hue.captains.singapura.js.homing.studio.base.Doc;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * A markdown {@link Doc} whose bytes are the repo's own canonical file under
 * {@code docs/}, copied into the jar at build time (see the
 * {@code copy-design-docs} execution in this module's pom). Renders through
 * the framework's doc-reader (the {@link Doc} default {@code url()}).
 *
 * <p><b>Why not {@code ComposedDoc}</b> (the framework's preferred kind for
 * new docs): these documents are authored as markdown files that live in
 * {@code docs/} — the location README and KT.md point at, and the form the
 * specs are reviewed and diffed in. Transcribing them into typed segments
 * would create a second copy free to drift from the canonical file, which is
 * exactly what the demo-data requirements' own R1 (single source of truth)
 * forbids. Docs authored <i>for</i> the studio (the User Guide) are
 * {@code ComposedDoc}s; docs the repo already owns are served from source.</p>
 *
 * <p>Deliberately not the framework's {@code ResourceMarkdownDoc}: that
 * interface is deprecated. This is the markdown sibling of
 * {@link HtmlResourceDoc}.</p>
 */
public interface MarkdownResourceDoc extends Doc {

    /** Classpath location of the markdown file, e.g. {@code findash-docs/foo.md}. */
    String resourcePath();

    @Override default String contentType()   { return "text/markdown; charset=utf-8"; }
    @Override default String fileExtension() { return ".md"; }

    @Override
    default String contents() {
        String path = resourcePath();
        ClassLoader loader = getClass().getClassLoader();
        try (var in = loader.getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException(
                        "Doc " + getClass().getName() + " missing classpath resource: " + path
                                + " (built by the copy-design-docs execution from ../docs)");
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read " + path, e);
        }
    }
}
