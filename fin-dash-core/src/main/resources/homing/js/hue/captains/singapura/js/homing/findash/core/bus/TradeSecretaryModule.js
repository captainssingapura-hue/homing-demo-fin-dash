// TradeSecretaryModule — the Secretary for the fin-dash TradeParty: the
// ENTITY level of the drill-down, where the DeskParty carries the SCOPE level.
//
//   desk  : portfolio / instrument / scenario   "what am I looking at"
//   trade : one trade                            "which record"
//
// The split exists because a trade selection used to travel as an
// InstrumentSelected with an extra `ref` field, which every consumer then had
// to filter back out. One channel, two meanings. Now each bus means one thing.
//
// Per the Diligent Secretaries doctrine: a pure (state, envelope) -> Step
// behavior + initial state; no DOM, no console, no side effects.
//
// Message kinds:
//   TradeSelected { trade: { id, ... } }
//       -> broadcast TradeChanged (same payload) to all members
//   TradeCleared {}
//       -> forget the selection, broadcast TradeChanged { trade: null } so
//          followers can fall back to their unfiltered view
//   CurrentTradeRequested   (late-join sync)
//       -> reply TradeChanged to the asker if a selection exists
//   anything else -> recentUnknown ring (bounded 10), no action
//
// The framework appends the `export { TradeSecretary }` from exports().
var TradeSecretary = {

    initial: {
        trade:         null,
        lastChangedBy: null,
        recentUnknown: []
    },

    behavior: function (state, envelope) {
        var msg = envelope.message;

        switch (msg.kind) {

            case "TradeSelected": {
                return {
                    newState: {
                        trade:         msg.trade,
                        lastChangedBy: envelope.from,
                        recentUnknown: state.recentUnknown
                    },
                    actions: [{
                        kind:    "BroadcastToMembers",
                        message: { kind: "TradeChanged", trade: msg.trade }
                    }]
                };
            }

            case "TradeCleared": {
                return {
                    newState: {
                        trade:         null,
                        lastChangedBy: envelope.from,
                        recentUnknown: state.recentUnknown
                    },
                    actions: [{
                        kind:    "BroadcastToMembers",
                        message: { kind: "TradeChanged", trade: null }
                    }]
                };
            }

            case "CurrentTradeRequested": {
                if (state.trade == null) {
                    return { newState: state, actions: [] };
                }
                return {
                    newState: state,
                    actions: [{
                        kind:    "SendToMember",
                        to:      envelope.from,
                        message: { kind: "TradeChanged", trade: state.trade }
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
                        trade:         state.trade,
                        lastChangedBy: state.lastChangedBy,
                        recentUnknown: recent
                    },
                    actions: []
                };
            }
        }
    }
};
