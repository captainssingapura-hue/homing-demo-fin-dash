package hue.captains.singapura.js.homing.findash.quant.data;

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
 * {@code GET /fx/calibration} — the quant-lab feed (study §7): per-pair calibration diagnostics — residuals by pillar, arb-gate margins (how close, not just pass/fail), solver-iteration trends (the early-warning signal), fast/slow-path agreement, model basis.
 */
public final class CalibrationGetAction
        implements GetAction<RoutingContext, CalibrationGetAction.Query, EmptyParam.NoHeaders, DocContent> {

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
        var diags = new JsonArray();
        for (DeskData.CalibDiag c : DeskData.CALIBRATION) {
            diags.add(new JsonObject().put("pair", c.pair()).put("epoch", c.epoch())
                    .put("residByPillar", c.residByPillar()).put("arbMargins", c.arbMargins())
                    .put("solverTrend", c.solverTrend()).put("pathAgreement", c.pathAgreement())
                    .put("modelBasis", c.modelBasis()).put("severity", c.severity()));
        }
        var json = new JsonObject()
                .put("slice", DeskData.SLICE)
                .put("diags", diags);
        return CompletableFuture.completedFuture(
                new DocContent(json.encode(), "application/json; charset=utf-8"));
    }
}
