package hue.captains.singapura.js.homing.findash.book.data;

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
 * {@code GET /fx/trades} — the trade-journal feed: journal-ordered trades,
 * each stamped with the slice it was priced on (P1 — the risk system and the
 * eventual P&amp;L explain agree with what the trader saw at execution), with
 * the full amendment history visible per trade (the journal IS the audit
 * trail; the UI just renders it — study §10).
 */
public final class TradesGetAction
        implements GetAction<RoutingContext, TradesGetAction.Query, EmptyParam.NoHeaders, DocContent> {

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
        var trades = new JsonArray();
        for (DeskData.Trade t : DeskData.TRADES) {
            var amendments = new JsonArray();
            for (DeskData.Amendment a : t.amendments()) {
                amendments.add(new JsonObject()
                        .put("when", a.when()).put("who", a.who()).put("what", a.what())
                        .put("pnlImpact", a.pnlImpact()).put("approval", a.approval()));
            }
            trades.add(new JsonObject()
                    .put("id", t.id()).put("time", t.time()).put("trader", t.trader())
                    .put("ticket", t.ticket()).put("pair", t.pair())
                    .put("notional", t.notional()).put("pvAtBooking", t.pvAtBooking())
                    .put("stamp", t.stamp()).put("status", t.status())
                    .put("amendments", amendments));
        }
        var json = new JsonObject()
                .put("slice", DeskData.SLICE)
                .put("trades", trades);
        return CompletableFuture.completedFuture(
                new DocContent(json.encode(), "application/json; charset=utf-8"));
    }
}
