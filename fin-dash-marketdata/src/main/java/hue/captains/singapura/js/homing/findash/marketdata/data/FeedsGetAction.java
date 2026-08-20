package hue.captains.singapura.js.homing.findash.marketdata.data;

import hue.captains.singapura.js.homing.findash.core.data.DeskData;
import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.studio.base.DocContent;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.Param;
import hue.captains.singapura.tao.http.action.ParamMarshaller;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.concurrent.CompletableFuture;

/**
 * {@code GET /fx/feeds} — the market-data quality feed (study §6): the
 * source × quote-kind health grid (each bad cell carrying its downstream
 * impact — "this stale forward feeds these surfaces"), the composite /
 * arbitrage monitors, and the quarantine review queue.
 */
public final class FeedsGetAction
        implements GetAction<RoutingContext, FeedsGetAction.Query, EmptyParam.NoHeaders, DocContent> {

    public record Query(String id) implements Param._QueryString {}

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
        var kinds = new JsonArray();
        DeskData.FEED_KINDS.forEach(kinds::add);
        var cells = new JsonArray();
        for (DeskData.FeedCell c : DeskData.FEED_GRID) {
            cells.add(new JsonObject()
                    .put("source", c.source()).put("kind", c.kind())
                    .put("state", c.state()).put("note", c.note())
                    .put("downstream", c.downstream()));
        }
        var monitors = new JsonArray();
        DeskData.FEED_MONITORS.forEach(monitors::add);
        var quarantine = new JsonArray();
        for (DeskData.Quarantined q : DeskData.QUARANTINE) {
            quarantine.add(new JsonObject()
                    .put("instrument", q.instrument()).put("pair", q.pair())
                    .put("reason", q.reason()).put("since", q.since())
                    .put("severity", q.severity()));
        }
        var json = new JsonObject()
                .put("slice", DeskData.SLICE)
                .put("kinds", kinds)
                .put("cells", cells)
                .put("monitors", monitors)
                .put("quarantine", quarantine);
        return CompletableFuture.completedFuture(
                new DocContent(json.encode(), "application/json; charset=utf-8"));
    }
}
