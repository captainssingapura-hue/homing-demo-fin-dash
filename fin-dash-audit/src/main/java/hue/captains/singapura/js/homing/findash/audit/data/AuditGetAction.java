package hue.captains.singapura.js.homing.findash.audit.data;

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
 * {@code GET /fx/audit} — the audit-explorer feed (study §13): cross-journal events (quotes, trades, overrides, config promotions, operational actions), each stamped — the reconstruction substrate for time travel (P4).
 */
public final class AuditGetAction
        implements GetAction<RoutingContext, AuditGetAction.Query, EmptyParam.NoHeaders, DocContent> {

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
        var events = new JsonArray();
        for (DeskData.AuditEvent e : DeskData.AUDIT_EVENTS) {
            events.add(new JsonObject().put("time", e.time()).put("journal", e.journal())
                    .put("actor", e.actor()).put("text", e.text()).put("stamp", e.stamp()));
        }
        var json = new JsonObject()
                .put("slice", DeskData.SLICE)
                .put("events", events);
        return CompletableFuture.completedFuture(
                new DocContent(json.encode(), "application/json; charset=utf-8"));
    }
}
