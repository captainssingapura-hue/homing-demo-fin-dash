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
 * {@code GET /fx/book} — the W4 blotter feed: totals and pair▸tenor bucket
 * rows (delta / smile-bucket vega / theta / freshness / reval budget). The
 * barrier watch and expiry/pin clusters have their own feeds
 * ({@link BarriersGetAction}, {@link ExpiriesGetAction}) and widgets.
 * Deterministic demo data from {@link DeskData}; slice-stamped (P1).
 */
public final class BookGetAction
        implements GetAction<RoutingContext, BookGetAction.Query, EmptyParam.NoHeaders, DocContent> {

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
        var rows = new JsonArray();
        for (DeskData.BucketRow r : DeskData.BOOK) {
            rows.add(new JsonObject()
                    .put("pair", r.pair())
                    .put("tenor", r.tenor())
                    .put("group", r.tenor() == null)
                    .put("delta", r.delta())
                    .put("vAtm", r.vAtm())
                    .put("vRr", r.vRr())
                    .put("vBf", r.vBf())
                    .put("theta", r.theta())
                    .put("freshSecs", r.freshSecs())
                    .put("budgetFrac", r.budgetFrac())
                    .put("note", r.note()));
        }
        var t = DeskData.TOTALS;
        var json = new JsonObject()
                .put("slice", DeskData.SLICE)
                .put("model", DeskData.MODEL)
                .put("totals", new JsonObject()
                        .put("delta", t.delta()).put("vega", t.vega()).put("theta", t.theta())
                        .put("vegaLimitFrac", t.vegaLimitFrac())
                        .put("revalWaveFrac", t.revalWaveFrac()))
                .put("rows", rows);
        return CompletableFuture.completedFuture(
                new DocContent(json.encode(), "application/json; charset=utf-8"));
    }
}
