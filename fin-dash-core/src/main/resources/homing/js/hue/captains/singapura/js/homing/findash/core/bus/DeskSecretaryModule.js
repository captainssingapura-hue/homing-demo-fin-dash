// DeskSecretaryModule — the Secretary for the fin-dash DeskParty: the
// cross-widget selection bus every persona workspace shares. Blotter row click
// -> InstrumentSelected -> every widget refreshes for that instrument (the
// pricer prefills, the surface manager switches pair). Scenario picks flow the
// same way. Per the Diligent Secretaries doctrine: a pure
// (state, envelope) -> Step behavior + initial state; no DOM, no console, no
// side effects.
//
// Message kinds:
//   InstrumentSelected { instrument: { pair, tenor?, ref? } }
//       -> broadcast InstrumentChanged (same payload) to all members
//   PortfolioSelected { portfolio: { id, label, leafIds: [..] } }
//       -> broadcast PortfolioChanged to all members. The tree resolves a
//          node at ANY level to its leaf-portfolio ids before publishing,
//          so consumers only ever test membership.
//   ScenarioSelected { scenario: { id, label } }
//       -> broadcast ScenarioChanged to all members
//   CurrentInstrumentRequested   (late-join sync)
//       -> reply InstrumentChanged to the asker if a selection exists
//   CurrentPortfolioRequested    (late-join sync)
//       -> reply PortfolioChanged to the asker if a selection exists
//   anything else -> recentUnknown ring (bounded 10), no action
//
// The framework appends the `export { DeskSecretary }` from exports().
var DeskSecretary = {

    initial: {
        instrument:    null,
        portfolio:     null,
        scenario:      null,
        lastChangedBy: null,
        recentUnknown: []
    },

    behavior: function (state, envelope) {
        var msg = envelope.message;

        switch (msg.kind) {

            case "InstrumentSelected": {
                return {
                    newState: {
                        instrument:    msg.instrument,
                        portfolio:     state.portfolio,
                        scenario:      state.scenario,
                        lastChangedBy: envelope.from,
                        recentUnknown: state.recentUnknown
                    },
                    actions: [{
                        kind:    "BroadcastToMembers",
                        message: { kind: "InstrumentChanged", instrument: msg.instrument }
                    }]
                };
            }

            case "PortfolioSelected": {
                return {
                    newState: {
                        instrument:    state.instrument,
                        portfolio:     msg.portfolio,
                        scenario:      state.scenario,
                        lastChangedBy: envelope.from,
                        recentUnknown: state.recentUnknown
                    },
                    actions: [{
                        kind:    "BroadcastToMembers",
                        message: { kind: "PortfolioChanged", portfolio: msg.portfolio }
                    }]
                };
            }

            case "DataTypeSelected": {
                // The ontology workspace's selection. Same shape as every other
                // selection: an id in, an id out — the usage pane resolves it.
                return {
                    newState: state,
                    actions: [{
                        kind:    "BroadcastToMembers",
                        message: { kind: "DataTypeChanged", dataType: msg.dataType }
                    }]
                };
            }

            case "ScenarioSelected": {
                return {
                    newState: {
                        instrument:    state.instrument,
                        portfolio:     state.portfolio,
                        scenario:      msg.scenario,
                        lastChangedBy: envelope.from,
                        recentUnknown: state.recentUnknown
                    },
                    actions: [{
                        kind:    "BroadcastToMembers",
                        message: { kind: "ScenarioChanged", scenario: msg.scenario }
                    }]
                };
            }

            case "CurrentInstrumentRequested": {
                if (state.instrument == null) {
                    return { newState: state, actions: [] };
                }
                return {
                    newState: state,
                    actions: [{
                        kind:    "SendToMember",
                        to:      envelope.from,
                        message: { kind: "InstrumentChanged", instrument: state.instrument }
                    }]
                };
            }

            case "CurrentPortfolioRequested": {
                if (state.portfolio == null) {
                    return { newState: state, actions: [] };
                }
                return {
                    newState: state,
                    actions: [{
                        kind:    "SendToMember",
                        to:      envelope.from,
                        message: { kind: "PortfolioChanged", portfolio: state.portfolio }
                    }]
                };
            }

            default: {
                var recent = state.recentUnknown.concat([{
                    kind: msg.kind,
                    from: envelope.from
                }]).slice(-10);
                return {
                    newState: {
                        instrument:    state.instrument,
                        portfolio:     state.portfolio,
                        scenario:      state.scenario,
                        lastChangedBy: state.lastChangedBy,
                        recentUnknown: recent
                    },
                    actions: []
                };
            }
        }
    }
};
