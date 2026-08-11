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
 * {@code GET /fx/positions} — the portfolio feed: every position with its
 * greeks, barrier distance, freshness, and the {@code tradeId} door into the
 * trade journal. The positions decompose the blotter's pair▸tenor buckets —
 * the middle rung of the drill chain. Slice-stamped (P1).
 */
public final class PositionsGetAction
        implements GetAction<RoutingContext, PositionsGetAction.Query, EmptyParam.NoHeaders, DocContent> {

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
        var positions = new JsonArray();
        for (DeskData.Position p : DeskData.POSITIONS) {
            positions.add(new JsonObject()
                    .put("id", p.id()).put("pair", p.pair()).put("tenor", p.tenor())
                    .put("instrument", p.instrument()).put("notional", p.notional())
                    .put("pv", p.pv()).put("delta", p.delta()).put("vega", p.vega())
                    .put("barrierDist", p.barrierDist())
                    .put("freshSecs", p.freshSecs()).put("budgetFrac", p.budgetFrac())
                    .put("tradeId", p.tradeId()));
        }
        var json = new JsonObject()
                .put("slice", DeskData.SLICE)
                .put("model", DeskData.MODEL)
                .put("positions", positions);
        return CompletableFuture.completedFuture(
                new DocContent(json.encode(), "application/json; charset=utf-8"));
    }
}
