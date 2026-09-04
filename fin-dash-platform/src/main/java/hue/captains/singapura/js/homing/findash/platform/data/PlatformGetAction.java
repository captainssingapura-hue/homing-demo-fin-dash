package hue.captains.singapura.js.homing.findash.platform.data;

import hue.captains.singapura.js.homing.findash.core.data.DeskData;
import hue.captains.singapura.js.homing.findash.core.data.DeskTolerances;
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
 * {@code GET /fx/platform} — the W5 system-console feed: latency SLOs with
 * budget consumption, journal health, the nightly replay-determinism
 * scoreboard, per-pair epoch flow (age vs expected cadence, with root cause),
 * and the Ring-3 controls — each carrying its blast radius in consumer terms.
 */
public final class PlatformGetAction
        implements GetAction<RoutingContext, PlatformGetAction.Query, EmptyParam.NoHeaders, DocContent> {

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
        var slos = new JsonArray();
        for (DeskData.Slo s : DeskData.SLOS) {
            slos.add(new JsonObject().put("path", s.path()).put("p99", s.p99())
                    .put("budgetFrac", s.budgetFrac())
                    .put("budgetState", DeskTolerances.budgetState(s.budgetFrac())));
        }
        var epochs = new JsonArray();
        for (DeskData.EpochFlow e : DeskData.EPOCH_FLOW) {
            epochs.add(new JsonObject().put("pair", e.pair()).put("epoch", e.epoch())
                    .put("age", e.age()).put("expected", e.expected())
                    .put("state", e.state()).put("note", e.note()));
        }
        var controls = new JsonArray();
        for (DeskData.PlatformControl c : DeskData.PLATFORM_CONTROLS) {
            controls.add(new JsonObject().put("label", c.label()).put("scope", c.scope())
                    .put("blastRadius", c.blastRadius()).put("fourEyes", c.fourEyes()));
        }
        var json = new JsonObject()
                .put("slice", DeskData.SLICE)
                .put("slos", slos)
                .put("journalHealth", DeskData.JOURNAL_HEALTH)
                .put("journalQuotaFrac", DeskData.JOURNAL_QUOTA_FRAC)
                .put("replayDeterminism", DeskData.REPLAY_DETERMINISM)
                .put("epochs", epochs)
                .put("curveSet", DeskData.CURVE_SET_FLOW)
                .put("controls", controls);
        return CompletableFuture.completedFuture(
                new DocContent(json.encode(), "application/json; charset=utf-8"));
    }
}
