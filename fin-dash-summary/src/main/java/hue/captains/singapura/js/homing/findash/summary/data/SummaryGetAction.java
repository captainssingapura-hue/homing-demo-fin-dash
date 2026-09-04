package hue.captains.singapura.js.homing.findash.summary.data;

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
 * {@code GET /fx/summary} — the management-summary feed (study §13): a glanceable rollup where every tile is a projection of the owning persona's data (P5 — no parallel truth) and names its owning workspace (every tile a door).
 */
public final class SummaryGetAction
        implements GetAction<RoutingContext, SummaryGetAction.Query, EmptyParam.NoHeaders, DocContent> {

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
        // Every tile is a projection of another persona's data — no numbers
        // computed specially for management (P5).
        int streaming = 0, widened = 0, pulled = 0;
        for (DeskData.QuotePair p : DeskData.QUOTING) {
            switch (p.state()) {
                case "streaming" -> streaming++;
                case "auto-widened" -> widened++;
                default -> pulled++;
            }
        }
        int pending = DeskData.CHANGES.size(), aging = 0;
        for (DeskData.ChangePackage c : DeskData.CHANGES) {
            if ("serious".equals(c.severity())) aging++;
        }
        int sloWarns = 0;
        for (DeskData.Slo s : DeskData.SLOS) {
            if (!DeskTolerances.OK.equals(DeskTolerances.budgetState(s.budgetFrac()))) sloWarns++;
        }

        java.util.function.Function<String[], JsonObject> tile = a -> new JsonObject()
                .put("title", a[0]).put("value", a[1]).put("detail", a[2])
                .put("severity", a[3]).put("owner", a[4]);
        var tiles = new JsonArray()
                .add(tile.apply(new String[]{"P&L (explained)", DeskData.PNL_TOTAL,
                        "unexplained +€6k, within tolerance", "good", "Product Control & IPV"}))
                .add(tile.apply(new String[]{"Risk vs limits", "vega 61%",
                        "USDJPY book at 92% — worklist open", "warn", "Market Risk"}))
                .add(tile.apply(new String[]{"Quoting posture", streaming + " streaming",
                        widened + " auto-widened · " + pulled + " pulled",
                        widened + pulled > 0 ? "warn" : "good", "e-Trading Supervision"}))
                .add(tile.apply(new String[]{"Open overrides", DeskData.OVERRIDES.size() + " live",
                        "oldest 3 d (EURJPY corr mark)", "serious", "Market Data Operations"}))
                .add(tile.apply(new String[]{"Ring-2 queue", pending + " pending",
                        aging + " aging > 5 d", aging > 0 ? "warn" : "good", "Model Governance"}))
                .add(tile.apply(new String[]{"System health", sloWarns == 0 ? "healthy" : "1 warning",
                        "reval-wave lag at 95% budget", sloWarns > 0 ? "warn" : "good",
                        "Platform Operations"}));
        var json = new JsonObject()
                .put("slice", DeskData.SLICE)
                .put("tiles", tiles);
        return CompletableFuture.completedFuture(
                new DocContent(json.encode(), "application/json; charset=utf-8"));
    }
}
