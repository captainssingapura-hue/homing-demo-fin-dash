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
 * {@code GET /fx/price?ticket=eurusd+3m+1.0850+ko+1.1200+10} — the W1 pricer
 * feed: desk shorthand in, parsed echo + decomposed price out. The UI is a
 * consumer (P5): parsing and pricing happen here, the widget only renders.
 * Errors come back as {@code {error}} JSON so the widget can show them inline
 * (a mistyped ticket is a user act, not a server fault).
 */
public final class PriceGetAction
        implements GetAction<RoutingContext, PriceGetAction.Query, EmptyParam.NoHeaders, DocContent> {

    public record Query(String ticket) implements Param._QueryString {}

    @Override
    public ParamMarshaller._QueryString<RoutingContext, Query> queryStrMarshaller() {
        return ctx -> new Query(ctx.request().getParam("ticket"));
    }

    @Override
    public ParamMarshaller._Header<RoutingContext, EmptyParam.NoHeaders> headerMarshaller() {
        return ctx -> new EmptyParam.NoHeaders();
    }

    @Override
    public CompletableFuture<DocContent> execute(Query query, EmptyParam.NoHeaders headers) {
        JsonObject json;
        try {
            DeskData.Ticket t = DeskData.parse(query.ticket() == null ? "" : query.ticket());
            DeskData.Priced p = DeskData.price(t);
            var ladder = new JsonArray();
            for (double[] rung : p.spotLadder()) {
                ladder.add(new JsonObject().put("shift", rung[0]).put("pv", rung[1]));
            }
            json = new JsonObject()
                    .put("ticket", new JsonObject()
                            .put("pair", t.pair()).put("tenor", t.tenor())
                            .put("strike", t.strike()).put("barrierType", t.barrierType())
                            .put("barrier", t.barrier()).put("notionalM", t.notionalM())
                            .put("type", t.type()))
                    .put("vol", p.vol())
                    .put("pvAmount", p.pvAmount()).put("pvPct", p.pvPct())
                    .put("bid", p.bid()).put("offer", p.offer())
                    .put("mid", p.mid()).put("spread", p.spread()).put("skew", p.skew())
                    .put("greeks", new JsonObject()
                            .put("delta", p.delta()).put("gamma", p.gamma())
                            .put("vega", p.vega()).put("vanna", p.vanna())
                            .put("volga", p.volga()).put("theta", p.theta()))
                    .put("barrierNote", p.barrierNote())
                    .put("spotLadder", ladder)
                    .put("volUp", p.volUp()).put("volDown", p.volDown())
                    .put("slice", p.slice()).put("epoch", p.epoch()).put("model", p.model());
        } catch (RuntimeException e) {
            json = new JsonObject().put("error",
                    e.getMessage() == null ? "could not parse ticket" : e.getMessage());
        }
        return CompletableFuture.completedFuture(
                new DocContent(json.encode(), "application/json; charset=utf-8"));
    }
}
