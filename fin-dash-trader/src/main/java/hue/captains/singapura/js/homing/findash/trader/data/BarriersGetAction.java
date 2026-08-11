package hue.captains.singapura.js.homing.findash.trader.data;

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
 * {@code GET /fx/barriers} — the barrier-watch feed (W4's first-class barrier
 * display, now its own widget): positions within the configured band of their
 * trigger, sorted by proximity, with distance, notional, and the tightened
 * budget state. Slice-stamped (P1).
 */
public final class BarriersGetAction
        implements GetAction<RoutingContext, BarriersGetAction.Query, EmptyParam.NoHeaders, DocContent> {

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
        var barriers = new JsonArray();
        for (DeskData.BarrierWatch b : DeskData.BARRIERS) {
            barriers.add(new JsonObject()
                    .put("pair", b.pair()).put("type", b.type()).put("level", b.level())
                    .put("pips", b.pips()).put("notional", b.notional())
                    .put("note", b.note()).put("severity", b.severity()));
        }
        var json = new JsonObject()
                .put("slice", DeskData.SLICE)
                .put("model", DeskData.MODEL)
                .put("barriers", barriers);
        return CompletableFuture.completedFuture(
                new DocContent(json.encode(), "application/json; charset=utf-8"));
    }
}
