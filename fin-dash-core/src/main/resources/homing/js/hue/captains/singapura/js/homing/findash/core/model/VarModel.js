// VarModel — headless risk model (RISK_MODEL). Deterministic by conformance rule
// (deterministic-risk): no Math.random(), no Date.now(); every input is explicit.
// The framework appends the `export { varOf }` from VarModel.exports().
//
// Placeholder parametric VaR: a downstream replaces this with the real engine's
// math, or a thin fetch over the pricing/risk endpoints.
function varOf(portfolio, scenario) {
    var z = (scenario && scenario.confidence === 0.99) ? 2.326 : 1.645; // 99% vs 95%
    var horizon = (scenario && scenario.horizonDays) ? scenario.horizonDays : 1;
    var sqrtT = Math.sqrt(horizon);
    var variance = 0;
    var positions = (portfolio && portfolio.positions) ? portfolio.positions : [];
    for (var i = 0; i < positions.length; i++) {
        var p = positions[i];
        var sigma = (p.vega || 0) * (p.volOfVol || 0.1) + (p.delta || 0) * (p.spotVol || 0.007);
        variance += sigma * sigma;
    }
    return z * Math.sqrt(variance) * sqrtT;
}
