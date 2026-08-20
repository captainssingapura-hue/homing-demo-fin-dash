package hue.captains.singapura.js.homing.findash.middleoffice.data;

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
 * {@code GET /fx/lifecycle} — the middle-office feed (study §10): the lifecycle workstation (expiries with cut countdowns, barrier watches, fixings, deliveries) and the breaks dashboard with aging + ownership.
 */
public final class LifecycleGetAction
        implements GetAction<RoutingContext, LifecycleGetAction.Query, EmptyParam.NoHeaders, DocContent> {

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
        var items = new JsonArray();
        for (DeskData.LifecycleItem i : DeskData.LIFECYCLE) {
            items.add(new JsonObject().put("kind", i.kind()).put("desc", i.desc())
                    .put("due", i.due()).put("state", i.state()).put("severity", i.severity()));
        }
        var breaks = new JsonArray();
        for (DeskData.BreakItem b : DeskData.BREAKS) {
            breaks.add(new JsonObject().put("vs", b.vs()).put("desc", b.desc())
                    .put("age", b.age()).put("owner", b.owner()).put("severity", b.severity()));
        }
        var json = new JsonObject()
                .put("slice", DeskData.SLICE)
                .put("items", items).put("breaks", breaks);
        return CompletableFuture.completedFuture(
                new DocContent(json.encode(), "application/json; charset=utf-8"));
    }
}
