package hue.captains.singapura.js.homing.findash.etrading.data;

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
 * {@code GET /fx/quoting} — the W3 e-trading feed: master state, one row per
 * auto-quoting pair (state + reason when automatic, spread vs base, skew, age,
 * hit ratio, session volume), the event stream (automatic and manual actions
 * in the SAME stream), and the RFQ tape (every quote slice-stamped, P1).
 */
public final class QuotingGetAction
        implements GetAction<RoutingContext, QuotingGetAction.Query, EmptyParam.NoHeaders, DocContent> {

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
        var pairs = new JsonArray();
        for (DeskData.QuotePair p : DeskData.QUOTING) {
            pairs.add(new JsonObject()
                    .put("pair", p.pair()).put("state", p.state())
                    .put("stateNote", p.stateNote()).put("spread", p.spread())
                    .put("skew", p.skew()).put("age", p.age())
                    .put("hitPct", p.hitPct()).put("volume", p.volume()));
        }
        var events = new JsonArray();
        for (DeskData.QuoteEvent e : DeskData.QUOTE_EVENTS) {
            events.add(new JsonObject()
                    .put("time", e.time()).put("kind", e.kind()).put("text", e.text()));
        }
        var rfqs = new JsonArray();
        for (DeskData.Rfq r : DeskData.RFQS) {
            rfqs.add(new JsonObject()
                    .put("time", r.time()).put("source", r.source()).put("desc", r.desc())
                    .put("quote", r.quote()).put("outcome", r.outcome()).put("stamp", r.stamp()));
        }
        var json = new JsonObject()
                .put("slice", DeskData.SLICE)
                .put("model", DeskData.MODEL)
                .put("master", "STREAMING")
                .put("pairs", pairs)
                .put("events", events)
                .put("rfqs", rfqs);
        return CompletableFuture.completedFuture(
                new DocContent(json.encode(), "application/json; charset=utf-8"));
    }
}
