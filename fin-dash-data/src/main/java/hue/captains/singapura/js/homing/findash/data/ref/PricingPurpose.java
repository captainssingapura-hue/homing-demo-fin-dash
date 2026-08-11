package hue.captains.singapura.js.homing.findash.data.ref;

/**
 * Stratum 2, Era 2 — <b>what a price is for</b>, which is the other axis of the
 * selection matrix.
 *
 * <p>The same instrument class may legitimately use different models for
 * different purposes: a fast model to stream, a richer one to mark officially.
 * The study's change package #412 turns on exactly this distinction — "barriers
 * × official: VV-2.3 → SLV-1.0; stream/RFQ and risk rows unchanged".</p>
 */
public enum PricingPurpose { STREAM, RFQ, OFFICIAL, RISK }
