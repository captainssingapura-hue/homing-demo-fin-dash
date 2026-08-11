package hue.captains.singapura.js.homing.findash.data.ref;

import hue.captains.singapura.js.homing.findash.data.id.PortfolioNodeId;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Stratum 2 — a node of the book tree: desk → book → portfolio.
 *
 * <p>Era 1 already needs this. A single trader with more than a handful of
 * trades immediately wants "my barriers" separate from "my vanillas" — the
 * hierarchy is not an institutional artefact, it is how a desk thinks from day
 * one. What later eras add is not the tree but <i>who else</i> selects on it
 * (risk limits, P&amp;L attribution, IPV scope).</p>
 *
 * <p>{@link #leafIds()} is the whole connection mechanism: a selection at any
 * level resolves to the leaves beneath it, so every consumer filters by plain
 * membership and never needs to know the shape of the tree (R6).</p>
 */
public record PortfolioNode(PortfolioNodeId id, String label, PortfolioKind kind,
                            List<PortfolioNode> children) implements ValueObject {

    public PortfolioNode {
        Objects.requireNonNull(id, "PortfolioNode.id");
        Objects.requireNonNull(label, "PortfolioNode.label");
        Objects.requireNonNull(kind, "PortfolioNode.kind");
        children = List.copyOf(Objects.requireNonNull(children, "PortfolioNode.children"));
    }

    public boolean isLeaf() { return children.isEmpty(); }

    /** The leaf-portfolio ids beneath this node (itself, when it is a leaf). */
    public List<PortfolioNodeId> leafIds() {
        if (isLeaf()) {
            return List.of(id);
        }
        final List<PortfolioNodeId> out = new ArrayList<>();
        for (final PortfolioNode child : children) {
            out.addAll(child.leafIds());
        }
        return List.copyOf(out);
    }

    /** Depth-first lookup of any node in this subtree. */
    public Optional<PortfolioNode> find(final PortfolioNodeId target) {
        if (id.equals(target)) {
            return Optional.of(this);
        }
        for (final PortfolioNode child : children) {
            final Optional<PortfolioNode> hit = child.find(target);
            if (hit.isPresent()) {
                return hit;
            }
        }
        return Optional.empty();
    }

    /** Every node in this subtree, parents before children. */
    public List<PortfolioNode> flattened() {
        final List<PortfolioNode> out = new ArrayList<>();
        out.add(this);
        for (final PortfolioNode child : children) {
            out.addAll(child.flattened());
        }
        return List.copyOf(out);
    }
}
