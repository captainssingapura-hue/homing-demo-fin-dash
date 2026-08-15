package hue.captains.singapura.js.homing.findash.book.data;

import hue.captains.singapura.js.homing.findash.core.data.DeskData;
import hue.captains.singapura.js.homing.findash.core.data.TradeLifecycle;
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
 * {@code GET /fx/trade-lifecycle?id=T-4409} — one trade's simulated
 * post-execution life: the scheme its product implies, the ordered events, and
 * where it ended up.
 *
 * <p>The simulation is {@link TradeLifecycle}, seeded from the trade's own
 * identity, so the same id always returns the same journal. The widget is a
 * renderer: it asks for a trade and draws what comes back, with no lifecycle
 * knowledge of its own (P5 — UIs are consumers, not calculators).</p>
 */
public final class TradeLifecycleGetAction
        implements GetAction<RoutingContext, TradeLifecycleGetAction.Query,
                             EmptyParam.NoHeaders, DocContent> {

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
        DeskData.Trade trade = null;
        for (DeskData.Trade t : DeskData.TRADES) {
            if (t.id().equals(query.id())) { trade = t; break; }
        }

        JsonObject json;
        if (trade == null) {
            json = new JsonObject()
                    .put("slice", DeskData.SLICE)
                    .put("error", "unknown trade: " + query.id());
        } else {
            TradeLifecycle.Result r = TradeLifecycle.of(trade);
            var events = new JsonArray();
            for (TradeLifecycle.Event e : r.events()) {
                events.add(new JsonObject()
                        .put("at", e.at()).put("kind", e.kind()).put("label", e.label())
                        .put("detail", e.detail()).put("actor", e.actor())
                        .put("severity", e.severity()));
            }
            json = new JsonObject()
                    .put("slice", DeskData.SLICE)
                    .put("tradeId", r.tradeId())
                    .put("ticket", trade.ticket())
                    .put("scheme", r.scheme())
                    .put("schemeNote", r.schemeNote())
                    .put("outcome", r.outcome())
                    .put("events", events);
        }
        return CompletableFuture.completedFuture(
                new DocContent(json.encode(), "application/json; charset=utf-8"));
    }
}
