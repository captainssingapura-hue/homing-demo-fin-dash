package hue.captains.singapura.js.homing.findash.book.data;

import hue.captains.singapura.js.homing.tree.NodeIdentity;

/**
 * What a portfolio-tree node <b>is</b>: the portfolio it stands for (RFC 0053).
 *
 * <p>Intrinsic and global, never positional — the same portfolio carries the
 * same identity wherever it appears in the tree, and moving a book between
 * parents does not change it. Equality is the contract: this is a map key, and
 * the id alone decides it.</p>
 *
 * <p>Before RFC 0053 this value travelled in the node's {@code Summary} display
 * slot, because there was no other channel for a machine field — the widget read
 * it back on selection. That is the anti-pattern the RFC retired, which is why
 * the port is mechanical here: the value was already isolated, it just had the
 * wrong home.</p>
 */
public record PortfolioNodeIdentity(String portfolioId) implements NodeIdentity {

    public PortfolioNodeIdentity {
        if (portfolioId == null || portfolioId.isBlank()) {
            throw new IllegalArgumentException("PortfolioNodeIdentity.portfolioId must not be blank");
        }
    }
}
