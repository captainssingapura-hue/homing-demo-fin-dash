package hue.captains.singapura.js.homing.findash.book.data;

import hue.captains.singapura.js.homing.findash.core.data.DeskData;
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * {@code GET /fx/portfolios} — the book hierarchy for the portfolio tree:
 * <ul>
 *   <li>{@code tree} — the canonical {@code TreeNode} JSON (desk → book →
 *       portfolio) the framework's {@code TreeRenderer} draws directly. A
 *       node's {@code summary} carries its portfolio <b>id</b> (the machine
 *       field the widget reads back on selection — the {@code ModuleTreeWidget}
 *       pattern); {@code kind} is {@code desk|book|portfolio}.</li>
 *   <li>{@code index} — id → {label, leafIds, positions} so the widget can
 *       resolve a selection at any level to its leaf-portfolio ids before
 *       broadcasting (consumers only test membership).</li>
 * </ul>
 */
public final class PortfoliosGetAction
        implements GetAction<RoutingContext, PortfoliosGetAction.Query, EmptyParam.NoHeaders, DocContent> {

    public record Query(String id) implements Param._QueryString {}

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
    public CompletableFuture<DocContent> execute(Query query, EmptyParam.NoHeaders headers) {
        var index = new JsonObject();
        buildIndex(DeskData.PORTFOLIOS, index);
        var json = new JsonObject()
                .put("slice", DeskData.SLICE)
                .put("tree", new JsonObject(writer.write(node(DeskData.PORTFOLIOS, TreeLevel.L0.INSTANCE))))
                .put("index", index);
        return CompletableFuture.completedFuture(
                new DocContent(json.encode(), "application/json; charset=utf-8"));
    }

    private static int positionCount(List<String> leafIds) {
        int n = 0;
        for (DeskData.Position p : DeskData.POSITIONS) {
            if (leafIds.contains(p.portfolioId())) n++;
        }
        return n;
    }

    private static void buildIndex(DeskData.PortfolioNode node, JsonObject index) {
        var leafIds = new JsonArray();
        node.leafIds().forEach(leafIds::add);
        index.put(node.id(), new JsonObject()
                .put("label", node.label())
                .put("leafIds", leafIds)
                .put("positions", positionCount(node.leafIds())));
        for (DeskData.PortfolioNode c : node.children()) buildIndex(c, index);
    }

    private NormalizedNode node(DeskData.PortfolioNode n, TreeLevel level) {
        TreeLevel deeper = level.below().orElse(level);
        List<NormalizedNode> children = n.children().stream()
                .map(c -> node(c, deeper)).toList();
        int count = positionCount(n.leafIds());
        var dims = new LinkedHashMap<DimensionKey, DimensionValue>();
        dims.put(DisplayLabel.INSTANCE, new NameValue(
                n.label() + "  (" + count + (count == 1 ? " pos" : " pos") + ")"));
        dims.put(Summary.INSTANCE,      new NameValue(n.id()));   // machine field: the portfolio id
        dims.put(Category.INSTANCE,     new CategoryValue(""));
        dims.put(Kind.INSTANCE,         new KindValue(n.kind()));
        return children.isEmpty()
                ? NormalizedNode.leaf(level, dims)
                : new NormalizedNode(level, dims, children);
    }
}
