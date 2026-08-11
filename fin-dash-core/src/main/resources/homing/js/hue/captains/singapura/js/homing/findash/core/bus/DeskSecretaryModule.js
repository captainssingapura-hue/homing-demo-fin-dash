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
//   ScenarioSelected { scenario: { id, label } }
//       -> broadcast ScenarioChanged to all members
//   CurrentInstrumentRequested   (late-join sync)
//       -> reply InstrumentChanged to the asker if a selection exists
//   anything else -> recentUnknown ring (bounded 10), no action
//
// The framework appends the `export { DeskSecretary }` from exports().
var DeskSecretary = {

    initial: {
        instrument:    null,
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

            case "ScenarioSelected": {
                return {
                    newState: {
                        instrument:    state.instrument,
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

            default: {
                var recent = state.recentUnknown.concat([{
                    kind: msg.kind,
                    from: envelope.from
                }]).slice(-10);
                return {
                    newState: {
                        instrument:    state.instrument,
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
