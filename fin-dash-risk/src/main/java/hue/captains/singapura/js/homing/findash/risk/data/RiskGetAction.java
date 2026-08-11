package hue.captains.singapura.js.homing.findash.risk.data;

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
 * {@code GET /fx/risk} — the market-risk feed (study §8): limits with
 * utilization in risk's own hierarchy, the breach worklist, governed scenario
 * definitions with their matrices, and the concentration views FX risk
 * actually needs (barrier density, event-date vega). Snapshot cadence — the
 * payload carries an explicit as-of.
 */
public final class RiskGetAction
        implements GetAction<RoutingContext, RiskGetAction.Query, EmptyParam.NoHeaders, DocContent> {

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
        var limits = new JsonArray();
        for (DeskData.LimitRow l : DeskData.LIMITS) {
            limits.add(new JsonObject().put("book", l.book()).put("metric", l.metric())
                    .put("usage", l.usage()).put("limit", l.limit())
                    .put("frac", l.frac()).put("note", l.note()));
        }
        var breaches = new JsonArray();
        for (DeskData.Breach b : DeskData.BREACHES) {
            breaches.add(new JsonObject().put("what", b.what()).put("detail", b.detail())
                    .put("severity", b.severity()).put("state", b.state()));
        }
        var scenarios = new JsonArray();
        for (DeskData.Scenario s : DeskData.SCENARIOS) {
            var cols = new JsonArray(); s.matrixCols().forEach(cols::add);
            var rows = new JsonArray();
            for (var r : s.matrix()) { var row = new JsonArray(); r.forEach(row::add); rows.add(row); }
            scenarios.add(new JsonObject().put("id", s.id()).put("label", s.label())
                    .put("kind", s.kind()).put("governance", s.governance())
                    .put("cols", cols).put("rows", rows).put("worst", s.worst()));
        }
        var density = new JsonArray();
        for (DeskData.BarrierBand b : DeskData.BARRIER_DENSITY) {
            density.add(new JsonObject().put("pair", b.pair()).put("band", b.band())
                    .put("notional", b.notional()).put("note", b.note())
                    .put("severity", b.severity()));
        }
        var events = new JsonArray();
        for (DeskData.EventVega e : DeskData.EVENT_VEGA) {
            events.add(new JsonObject().put("date", e.date()).put("event", e.event())
                    .put("vega", e.vega()).put("severity", e.severity()));
        }
        var json = new JsonObject()
                .put("slice", DeskData.SLICE)
                .put("asOf", "14:30:00 snapshot")
                .put("limits", limits).put("breaches", breaches)
                .put("scenarios", scenarios).put("density", density)
                .put("eventVega", events);
        return CompletableFuture.completedFuture(
                new DocContent(json.encode(), "application/json; charset=utf-8"));
    }
}
