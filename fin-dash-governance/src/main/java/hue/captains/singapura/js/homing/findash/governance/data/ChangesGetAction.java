package hue.captains.singapura.js.homing.findash.governance.data;

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
 * {@code GET /fx/changes} — the W6 governance feed: the Ring-2 promotion queue
 * (each pending change a full package: semantic diff in domain terms, golden
 * replay impact, rationale, approval chain, staged activation), the model
 * inventory, the revalidation worklist, and the reverse-query results.
 */
public final class ChangesGetAction
        implements GetAction<RoutingContext, ChangesGetAction.Query, EmptyParam.NoHeaders, DocContent> {

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
        var changes = new JsonArray();
        for (DeskData.ChangePackage c : DeskData.CHANGES) {
            var diff = new JsonArray();   c.semanticDiff().forEach(diff::add);
            var impact = new JsonArray(); c.replayImpact().forEach(impact::add);
            var chain = new JsonArray();
            for (DeskData.ApprovalStep s : c.chain()) {
                chain.add(new JsonObject().put("role", s.role()).put("who", s.who())
                        .put("state", s.state()).put("when", s.when()));
            }
            changes.add(new JsonObject()
                    .put("id", c.id()).put("title", c.title()).put("summary", c.summary())
                    .put("aging", c.aging()).put("severity", c.severity())
                    .put("semanticDiff", diff).put("replayImpact", impact)
                    .put("rationale", c.rationale()).put("chain", chain)
                    .put("activation", c.activation()));
        }
        var models = new JsonArray();
        for (DeskData.ModelEntry m : DeskData.MODELS) {
            models.add(new JsonObject()
                    .put("name", m.name()).put("version", m.version())
                    .put("status", m.status()).put("usedBy", m.usedBy())
                    .put("doc", m.doc())
                    .put("reverseQuery", DeskData.reverseQuery(m.name())));
        }
        var revalidation = new JsonArray();
        DeskData.REVALIDATION.forEach(revalidation::add);

        var json = new JsonObject()
                .put("slice", DeskData.SLICE)
                .put("changes", changes)
                .put("models", models)
                .put("revalidation", revalidation);
        return CompletableFuture.completedFuture(
                new DocContent(json.encode(), "application/json; charset=utf-8"));
    }
}
