package hue.captains.singapura.js.homing.findash.ipv.data;

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
 * {@code GET /fx/pnl} — the product-control feed (study §11): the P&amp;L attribution terms (every term from the same journaled slices the desk traded on, P5), the IPV variance workbench, and the official-marks sign-off state.
 */
public final class PnlGetAction
        implements GetAction<RoutingContext, PnlGetAction.Query, EmptyParam.NoHeaders, DocContent> {

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
        var terms = new JsonArray();
        for (DeskData.PnlTerm t : DeskData.PNL_EXPLAIN) {
            terms.add(new JsonObject().put("term", t.term()).put("amount", t.amount())
                    .put("emphasis", t.emphasis()));
        }
        var ipv = new JsonArray();
        for (DeskData.IpvRow r : DeskData.IPV_ROWS) {
            ipv.add(new JsonObject().put("scope", r.scope()).put("desk", r.desk())
                    .put("independent", r.independent()).put("variance", r.variance())
                    .put("severity", r.severity()).put("note", r.note()));
        }
        var json = new JsonObject()
                .put("slice", DeskData.SLICE)
                .put("terms", terms).put("total", DeskData.PNL_TOTAL)
                .put("unexplainedTol", DeskData.PNL_UNEXPLAINED_TOL)
                .put("ipv", ipv).put("marksState", DeskData.MARKS_STATE);
        return CompletableFuture.completedFuture(
                new DocContent(json.encode(), "application/json; charset=utf-8"));
    }
}
