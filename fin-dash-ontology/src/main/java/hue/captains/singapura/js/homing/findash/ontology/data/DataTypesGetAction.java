package hue.captains.singapura.js.homing.findash.ontology.data;

import hue.captains.singapura.js.homing.findash.data.meta.DataOntology;
import hue.captains.singapura.js.homing.findash.data.meta.DataType;
import hue.captains.singapura.js.homing.findash.data.meta.Relation;
import hue.captains.singapura.js.homing.findash.data.meta.Stratum;
import hue.captains.singapura.js.homing.findash.data.meta.WidgetUsage;
import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.studio.base.DocContent;
import hue.captains.singapura.js.homing.studio.base.tree.CategoryValue;
import hue.captains.singapura.js.homing.studio.base.tree.KindValue;
import hue.captains.singapura.js.homing.tree.Category;
import hue.captains.singapura.js.homing.tree.DimensionKey;
import hue.captains.singapura.js.homing.tree.DimensionValue;
import hue.captains.singapura.js.homing.tree.DisplayLabel;
import hue.captains.singapura.js.homing.tree.Kind;
import hue.captains.singapura.js.homing.tree.NormalizedNode;
import hue.captains.singapura.js.homing.tree.Summary;
import hue.captains.singapura.js.homing.tree.TreeLevel;
import hue.captains.singapura.js.homing.tree.TreeNodeJsonWriter;
import hue.captains.singapura.js.homing.tree.dims.NameValue;
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
        final var json = new JsonObject()
                .put("tree", new JsonObject(writer.write(root())))
                .put("index", index());
        return CompletableFuture.completedFuture(
                new DocContent(json.encode(), "application/json; charset=utf-8"));
    }

    private NormalizedNode root() {
        final List<NormalizedNode> strata = new ArrayList<>();
        for (final Stratum s : Stratum.values()) {
            final List<DataType> types = ontology.inStratum(s);
            if (types.isEmpty()) {
                continue;
            }
            final List<NormalizedNode> leaves = new ArrayList<>();
            for (final DataType t : types) {
                leaves.add(NormalizedNode.leaf(TreeLevel.L2.INSTANCE,
                        dims(t.id().value() + "  (" + t.usages().size() + ")",
                                t.id().value(), markerSlug(t))));
            }
            strata.add(new NormalizedNode(TreeLevel.L1.INSTANCE,
                    dims(label(s) + "  (" + types.size() + ")", "stratum:" + s.name(), "stratum"),
                    leaves));
        }
        final int total = ontology.types().size();
        return new NormalizedNode(TreeLevel.L0.INSTANCE,
                dims("Data Ontology  (" + total + " types)", "stratum:ALL", "workspace"), strata);
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

    private Map<DimensionKey, DimensionValue> dims(final String label, final String summary,
                                                   final String kind) {
        final Map<DimensionKey, DimensionValue> m = new LinkedHashMap<>();
        m.put(DisplayLabel.INSTANCE, new NameValue(label));
        m.put(Summary.INSTANCE, new NameValue(summary));
        m.put(Category.INSTANCE, new CategoryValue(""));
        m.put(Kind.INSTANCE, new KindValue(kind));
        return m;
    }
}
