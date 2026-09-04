package hue.captains.singapura.js.homing.findash.ontology.data;

import hue.captains.singapura.js.homing.findash.data.meta.DataOntology;
import hue.captains.singapura.js.homing.findash.data.meta.DataType;
import hue.captains.singapura.js.homing.findash.data.meta.Relation;
import hue.captains.singapura.js.homing.findash.data.meta.Stratum;
import hue.captains.singapura.js.homing.findash.data.meta.WidgetUsage;
import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.studio.base.DocContent;
import hue.captains.singapura.js.homing.tree.NodeIdentity;
import hue.captains.singapura.js.homing.tree.NodeName;
import hue.captains.singapura.js.homing.tree.NormalizedNode;
import hue.captains.singapura.js.homing.tree.RowDisplay;
import hue.captains.singapura.js.homing.tree.RowDisplaySource;
import hue.captains.singapura.js.homing.tree.TreeLevel;
import hue.captains.singapura.js.homing.tree.TreeNodeJsonWriter;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.Param;
import hue.captains.singapura.tao.http.action.ParamMarshaller;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * {@code GET /fx/data-types} — the ontology catalogue.
 *
 * <ul>
 *   <li>{@code tree} — canonical {@code TreeNode} JSON, stratum → type, drawn
 *       by the framework's {@code TreeRenderer}. A node's {@code summary}
 *       carries the type id (the machine field the widget reads back on
 *       selection — the established {@code ModuleTreeWidget} pattern).</li>
 *   <li>{@code index} — type id → everything the detail pane needs: marker,
 *       era, summary, relations out and in, and the widgets that require it.</li>
 * </ul>
 *
 * <p>The payload is a pure projection of {@code DataOntology}: this action
 * authors nothing, exactly as a feed over real facts will not.</p>
 */
public final class DataTypesGetAction
        implements GetAction<RoutingContext, DataTypesGetAction.Query, EmptyParam.NoHeaders, DocContent> {

    public record Query(String id) implements Param._QueryString {}

    private final DataOntology ontology = new DataOntology();
    private final TreeNodeJsonWriter writer = new TreeNodeJsonWriter();

    @Override
    public ParamMarshaller._QueryString<RoutingContext, Query> queryStrMarshaller() {
        return ctx -> new Query(ctx.request().getParam("id"));
    }

    @Override
    public ParamMarshaller._Header<RoutingContext, EmptyParam.NoHeaders> headerMarshaller() {
        return ctx -> new EmptyParam.NoHeaders();
    }

    @Override
    public CompletableFuture<DocContent> execute(final Query query, final EmptyParam.NoHeaders headers) {
        // One walk fills both the tree and the details it will be rendered from.
        final var details = new LinkedHashMap<NodeIdentity, DataTypeDetails>();
        final NormalizedNode root = root(details);
        final var json = new JsonObject()
                // NOTE the two-arg write: the one-arg overload still compiles and
                // emits no display block, which renders the tree unlabelled.
                .put("tree", new JsonObject(writer.write(root, rowsFrom(details))))
                .put("index", index());
        return CompletableFuture.completedFuture(
                new DocContent(json.encode(), "application/json; charset=utf-8"));
    }

    /**
     * What a node is, in this action's own vocabulary (RFC 0053). Counts stay
     * {@code int} until {@link #row()} renders them — "12 types" is a number you
     * can no longer count with.
     *
     * <p>Each {@code row()} folds its count into the LABEL rather than the note:
     * {@code TreeRenderer} draws the note only when its {@code showNote} option
     * is set, and the navigator trees deliberately leave it off, so a count moved
     * to the note would silently disappear from the tree.</p>
     */
    private sealed interface DataTypeDetails {
        RowDisplay row();

        record OfRoot(int types) implements DataTypeDetails {
            public RowDisplay row() {
                return new RowDisplay("Data Ontology  (" + types + " types)", "",
                        types + " types", "workspace");
            }
        }

        record OfStratum(String label, int types) implements DataTypeDetails {
            public RowDisplay row() {
                return new RowDisplay(label + "  (" + types + ")", "",
                        types + " types", "stratum");
            }
        }

        record OfType(String id, String marker, int usages) implements DataTypeDetails {
            public RowDisplay row() {
                return new RowDisplay(id + "  (" + usages + ")", "",
                        usages + " usages", marker);
            }
        }
    }

    /** The projection handed to the writer — it never reads the node itself. */
    private static RowDisplaySource rowsFrom(final Map<NodeIdentity, DataTypeDetails> details) {
        return node -> {
            final DataTypeDetails d = node instanceof NormalizedNode n ? details.get(n.identity()) : null;
            return d == null ? RowDisplay.of("") : d.row();
        };
    }

    private NormalizedNode root(final Map<NodeIdentity, DataTypeDetails> details) {
        final List<NormalizedNode> strata = new ArrayList<>();
        for (final Stratum s : Stratum.values()) {
            final List<DataType> types = ontology.inStratum(s);
            if (types.isEmpty()) {
                continue;
            }
            final List<NormalizedNode> leaves = new ArrayList<>();
            for (final DataType t : types) {
                final var id = DataTypeNodeIdentity.type(t.id().value());
                details.put(id, new DataTypeDetails.OfType(
                        t.id().value(), markerSlug(t), t.usages().size()));
                leaves.add(NormalizedNode.leaf(TreeLevel.L2.INSTANCE,
                        NodeName.slug(t.id().value()), id, Map.of()));
            }
            final var stratumId = DataTypeNodeIdentity.stratum(s.name());
            details.put(stratumId, new DataTypeDetails.OfStratum(label(s), types.size()));
            strata.add(new NormalizedNode(TreeLevel.L1.INSTANCE,
                    NodeName.slug(s.name()), stratumId, Map.of(), leaves));
        }
        final var rootId = DataTypeNodeIdentity.root();
        details.put(rootId, new DataTypeDetails.OfRoot(ontology.types().size()));
        return new NormalizedNode(TreeLevel.L0.INSTANCE,
                NodeName.slug("data-ontology"), rootId, Map.of(), strata);
    }

    private JsonObject index() {
        final var index = new JsonObject();
        for (final DataType t : ontology.types()) {
            final var relations = new JsonArray();
            for (final Relation r : t.relations()) {
                relations.add(new JsonObject()
                        .put("edge", r.edge()).put("to", r.to().value())
                        .put("cardinality", r.cardinality().name())
                        .put("purpose", r.purpose()));
            }
            final var incoming = new JsonArray();
            for (final Relation r : ontology.relationsTo(t.id())) {
                incoming.add(new JsonObject()
                        .put("from", r.from().value()).put("edge", r.edge())
                        .put("cardinality", r.cardinality().name())
                        .put("purpose", r.purpose()));
            }
            final var usages = new JsonArray();
            for (final WidgetUsage u : t.usages()) {
                usages.add(new JsonObject()
                        .put("widgetId", u.widgetId()).put("widget", u.widgetLabel())
                        .put("workspace", u.workspaceKind()).put("role", u.role().name())
                        .put("note", u.note()));
            }
            index.put(t.id().value(), new JsonObject()
                    .put("javaType", t.javaType())
                    .put("stratum", t.stratum().name())
                    .put("marker", t.marker().name())
                    .put("era", t.era())
                    .put("summary", t.summary())
                    .put("relations", relations)
                    .put("incoming", incoming)
                    .put("usages", usages)
                    .put("workspaces", new JsonArray(t.workspaces())));
        }
        return index;
    }

    private String label(final Stratum s) {
        return switch (s) {
            case IDENTITY -> "0 · Identity";
            case QUANTITY -> "1 · Quantity";
            case REFERENCE -> "2 · Reference data";
            case MARKET_STATE -> "3 · Market state";
            case BOOK -> "4 · Book";
            case JOURNAL -> "5 · Journal";
            case DERIVED -> "6 · Derived";
            case BEHAVIOUR -> "7 · Behaviour";
        };
    }

    private String markerSlug(final DataType t) {
        return t.marker().name().toLowerCase().replace('_', '-');
    }

}
