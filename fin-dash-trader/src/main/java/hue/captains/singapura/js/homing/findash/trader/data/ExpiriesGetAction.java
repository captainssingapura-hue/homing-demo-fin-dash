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
 * {@code GET /fx/expiries} — the expiry / pin-risk feed: same-cut expiry
 * clusters (the bulk-processing unit at cuts like NY 10am / Tokyo 3pm) and
 * pin candidates (expiry-day open interest near spot). Slice-stamped (P1).
 */
public final class ExpiriesGetAction
        implements GetAction<RoutingContext, ExpiriesGetAction.Query, EmptyParam.NoHeaders, DocContent> {

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
        var clusters = new JsonArray();
        for (DeskData.ExpiryCluster e : DeskData.EXPIRIES) {
            clusters.add(new JsonObject()
                    .put("cut", e.cut()).put("pair", e.pair())
                    .put("notional", e.notional()).put("note", e.note())
                    .put("severity", e.severity()));
        }
        var pins = new JsonArray();
        for (DeskData.PinCandidate p : DeskData.PINS) {
            pins.add(new JsonObject()
                    .put("pair", p.pair()).put("strike", p.strike())
                    .put("notional", p.notional()).put("note", p.note())
                    .put("severity", p.severity()));
        }
        var json = new JsonObject()
                .put("slice", DeskData.SLICE)
                .put("model", DeskData.MODEL)
                .put("clusters", clusters)
                .put("pins", pins);
        return CompletableFuture.completedFuture(
                new DocContent(json.encode(), "application/json; charset=utf-8"));
    }
}
