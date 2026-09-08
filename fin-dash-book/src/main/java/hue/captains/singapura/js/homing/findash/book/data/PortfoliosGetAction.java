package hue.captains.singapura.js.homing.findash.book.data;

import hue.captains.singapura.js.homing.findash.core.data.DeskData;
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

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * {@code GET /fx/portfolios} — the book hierarchy for the portfolio tree:
 * <ul>
 *   <li>{@code tree} — the canonical {@code TreeNode} JSON (desk → book →
 *       portfolio) the framework's {@code TreeRenderer} draws directly. Each
 *       node carries a {@link PortfolioNodeIdentity} — the portfolio id, which
 *       the widget reads back on selection — and its display is resolved at the
 *       edge through a {@link RowDisplaySource} (RFC 0053).</li>
 *   <li>{@code index} — id → {label, leafIds, positions, pairs} so the widget
 *       can resolve a selection at any level to its leaf-portfolio ids and the
 *       currency pairs it books, before broadcasting (consumers only test
 *       membership).</li>
 * </ul>
 */
public final class PortfoliosGetAction
        implements GetAction<RoutingContext, PortfoliosGetAction.Query, EmptyParam.NoHeaders, DocContent> {

    public record Query(String id) implements Param._QueryString {}

    private final TreeNodeJsonWriter writer = new TreeNodeJsonWriter();

    /**
     * What a node is, in this action's own vocabulary — modelled, not rendered.
     * The count stays an {@code int} right up to {@link #row()}: a string like
     * "39 pos" is a number you can no longer count with, and baking one into the
     * model is the mistake the retired dimension vocabulary encouraged.
     */
    private record PortfolioDetails(String label, String kind, int positions) {
        RowDisplay row() {
            // The count rides in the LABEL rather than the note. TreeRenderer
            // draws the note only when its `showNote` option is set, and the
            // workspace navigator trees deliberately leave it off — so a count
            // moved to the note would silently vanish from the tree.
            return new RowDisplay(
                    label + "  (" + positions + " pos)",   // label
                    "",                                    // badge — unused here
                    positions + " positions",              // note — for listings that show it
                    kind);                                 // kind — desk|book|portfolio
        }
    }

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

        // One walk fills both the tree and the details it will be rendered from.
        var details = new LinkedHashMap<NodeIdentity, PortfolioDetails>();
        NormalizedNode root = node(DeskData.PORTFOLIOS, TreeLevel.L0.INSTANCE, details);

        var json = new JsonObject()
                .put("slice", DeskData.SLICE)
                // NOTE the two-arg write: the one-arg overload still compiles and
                // emits no display block, which renders the tree unlabelled.
                .put("tree", new JsonObject(writer.write(root, rowsFrom(details))))
                .put("index", index);
        return CompletableFuture.completedFuture(
                new DocContent(json.encode(), "application/json; charset=utf-8"));
    }

    /** The projection handed to the writer — it never reads the node itself. */
    private static RowDisplaySource rowsFrom(Map<NodeIdentity, PortfolioDetails> details) {
        return node -> {
            PortfolioDetails d = node instanceof NormalizedNode n ? details.get(n.identity()) : null;
            return d == null ? RowDisplay.of("") : d.row();
        };
    }

    private static int positionCount(List<String> leafIds) {
        int n = 0;
        for (DeskData.Position p : DeskData.POSITIONS) {
            if (leafIds.contains(p.portfolioId())) n++;
        }
        return n;
    }

    /**
     * The currency pairs a portfolio books, in book order.
     *
     * <p>Published because it is a <b>desk fact</b>, not something a consumer
     * should infer. A leaf portfolio books one pair and a group books the union
     * of its leaves'; the alternative — reading "EURUSD" out of the label
     * "EURUSD vanillas" — is the same mistake as reading a machine field out of
     * a display slot, and it breaks the day a book is renamed.</p>
     *
     * <p>Consumers only test membership, exactly as they do with
     * {@code leafIds}.</p>
     */
    private static JsonArray pairsOf(List<String> leafIds) {
        var seen = new LinkedHashSet<String>();
        for (DeskData.Position p : DeskData.POSITIONS) {
            if (leafIds.contains(p.portfolioId())) seen.add(p.pair());
        }
        var out = new JsonArray();
        seen.forEach(out::add);
        return out;
    }

    private static void buildIndex(DeskData.PortfolioNode node, JsonObject index) {
        var leafIds = new JsonArray();
        node.leafIds().forEach(leafIds::add);
        index.put(node.id(), new JsonObject()
                .put("label", node.label())
                .put("leafIds", leafIds)
                .put("positions", positionCount(node.leafIds()))
                .put("pairs", pairsOf(node.leafIds())));
        for (DeskData.PortfolioNode c : node.children()) buildIndex(c, index);
    }

    private NormalizedNode node(DeskData.PortfolioNode n, TreeLevel level,
                                Map<NodeIdentity, PortfolioDetails> details) {
        TreeLevel deeper = level.below().orElse(level);
        List<NormalizedNode> children = n.children().stream()
                .map(c -> node(c, deeper, details)).toList();

        // The segment is slugged from the id, not the label: ids are already
        // unique across the book, so sibling uniqueness (Law 2) holds by
        // construction rather than by hoping two books are never named alike.
        NodeName segment = NodeName.slug(n.id());
        var identity = new PortfolioNodeIdentity(n.id());
        details.put(identity, new PortfolioDetails(n.label(), n.kind(), positionCount(n.leafIds())));

        return children.isEmpty()
                ? NormalizedNode.leaf(level, segment, identity, Map.of())
                : new NormalizedNode(level, segment, identity, Map.of(), children);
    }
}
