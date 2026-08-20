package hue.captains.singapura.js.homing.findash.marketdata.data;

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
 * {@code GET /fx/overrides} — the override inventory (study §6): every live
 * Ring-3 mark and data action across the whole stack — who, why, when it
 * expires — as one queryable view, because "scattered overrides that nobody
 * can enumerate are how yesterday's judgment becomes next year's mystery".
 */
public final class OverridesGetAction
        implements GetAction<RoutingContext, OverridesGetAction.Query, EmptyParam.NoHeaders, DocContent> {

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
        var overrides = new JsonArray();
        for (DeskData.LiveOverride o : DeskData.OVERRIDES) {
            overrides.add(new JsonObject()
                    .put("scope", o.scope()).put("what", o.what()).put("who", o.who())
                    .put("why", o.why()).put("expires", o.expires())
                    .put("age", o.age()).put("severity", o.severity()));
        }
        var json = new JsonObject()
                .put("slice", DeskData.SLICE)
                .put("overrides", overrides);
        return CompletableFuture.completedFuture(
                new DocContent(json.encode(), "application/json; charset=utf-8"));
    }
}
