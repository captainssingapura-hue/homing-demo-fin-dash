package hue.captains.singapura.js.homing.findash.trader.data;

import hue.captains.singapura.js.homing.findash.core.data.DeskData;
import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.server.ResourceNotFound;
import hue.captains.singapura.js.homing.studio.base.DocContent;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.Param;
import hue.captains.singapura.tao.http.action.ParamMarshaller;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.concurrent.CompletableFuture;

/**
 * {@code GET /fx/surface?pair=EURUSD} — the W2 surface-manager feed: per-tenor
 * smile (market vs fit, delta space), pillar table, gate flags, two-speed
 * state (epoch, staleness + what recalibration waits on), event vols, arb
 * gates, quote provenance. No pair param ⇒ the pair list.
 */
public final class SurfaceGetAction
        implements GetAction<RoutingContext, SurfaceGetAction.Query, EmptyParam.NoHeaders, DocContent> {

    public record Query(String pair) implements Param._QueryString {}

    @Override
    public ParamMarshaller._QueryString<RoutingContext, Query> queryStrMarshaller() {
        return ctx -> new Query(ctx.request().getParam("pair"));
    }

    @Override
    public ParamMarshaller._Header<RoutingContext, EmptyParam.NoHeaders> headerMarshaller() {
        return ctx -> new EmptyParam.NoHeaders();
    }

    @Override
    public CompletableFuture<DocContent> execute(Query query, EmptyParam.NoHeaders headers) {
        if (query.pair() == null || query.pair().isBlank()) {
            var pairs = new JsonArray();
            DeskData.SURFACES.forEach(s -> pairs.add(new JsonObject()
                    .put("pair", s.pair()).put("epoch", s.epoch()).put("stale", s.stale())));
            return CompletableFuture.completedFuture(new DocContent(
                    new JsonObject().put("slice", DeskData.SLICE).put("pairs", pairs).encode(),
                    "application/json; charset=utf-8"));
        }
        var found = DeskData.surface(query.pair());
        if (found.isEmpty()) {
            return CompletableFuture.failedFuture(new ResourceNotFound(
                    new ResourceNotFound._InternalError(null, "unknown pair " + query.pair()),
                    new ResourceNotFound._ExternalError("fx-surface", "unknown pair " + query.pair())));
        }
        var s = found.get();
        var tenors = new JsonArray();
        for (DeskData.TenorSurface t : s.tenors()) {
            var smile = new JsonArray();
            for (DeskData.SmilePoint p : t.smile()) {
                smile.add(new JsonObject().put("label", p.label())
                        .put("mkt", p.mkt()).put("fit", p.fit()));
            }
            var pillars = new JsonArray();
            for (DeskData.PillarRow p : t.pillars()) {
                pillars.add(new JsonObject().put("name", p.name())
                        .put("mkt", p.mkt()).put("fit", p.fit()).put("ovr", p.ovr()));
            }
            tenors.add(new JsonObject()
                    .put("tenor", t.tenor()).put("flagged", t.flagged())
                    .put("flagNote", t.flagNote())
                    .put("resid", t.resid()).put("tol", t.tol())
                    .put("smile", smile).put("pillars", pillars));
        }
        var json = new JsonObject()
                .put("slice", DeskData.SLICE)
                .put("model", DeskData.MODEL)
                .put("pair", s.pair())
                .put("epoch", s.epoch())
                .put("stale", s.stale())
                .put("staleNote", s.staleNote())
                .put("anchorDrift", s.anchorDrift())
                .put("eventVols", s.eventVols())
                .put("arbGates", s.arbGates())
                .put("provenance", s.provenance())
                .put("tenors", tenors);
        return CompletableFuture.completedFuture(
                new DocContent(json.encode(), "application/json; charset=utf-8"));
    }
}
