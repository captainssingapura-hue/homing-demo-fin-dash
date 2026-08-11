package hue.captains.singapura.js.homing.findash.studio.docs;

import hue.captains.singapura.js.homing.studio.base.Doc;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * A {@link Doc} whose contents are a complete, self-styled HTML page shipped
 * on the classpath — the shape of the fin-dash design documents (the UI study
 * and the engine architecture are standalone pages with their own CSS and
 * wireframe SVGs; converting them to markdown would lose the wireframes).
 *
 * <p>Served verbatim: {@link #contentType()} is {@code text/html}, and
 * {@link #url()} points at the raw {@code /doc?id=<uuid>} endpoint (which
 * serves {@link #contents()} with this content type) instead of the markdown
 * doc-reader — clicking the catalogue tile opens the page as authored.</p>
 *
 * <p>The canonical sources live in the repo's {@code docs/} folder (the
 * handover location README and KT.md point at); the classpath copies under
 * {@code findash-docs/} exist so the studio can serve them from the jar.
 * Update both together.</p>
 */
public interface HtmlResourceDoc extends Doc {

    /** Classpath location of the HTML page, e.g. {@code findash-docs/foo.html}. */
    String resourcePath();

    @Override default String contentType()   { return "text/html; charset=utf-8"; }
    @Override default String fileExtension() { return ".html"; }
    @Override default String url()           { return "/doc?id=" + uuid(); }

    @Override
    default String contents() {
        String path = resourcePath();
        ClassLoader loader = getClass().getClassLoader();
        try (var in = loader.getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException(
                        "Doc " + getClass().getName() + " missing classpath resource: " + path);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read " + path, e);
        }
    }
}
