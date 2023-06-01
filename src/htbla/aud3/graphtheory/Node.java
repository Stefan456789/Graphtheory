package htbla.aud3.graphtheory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jakob Wintersteiger, Stefan Wiesinger
 */
public class Node {
    final List<Edge> outgoingEdges;
    final List<Edge> incomingEdges;
    final int nodeId;
    int distanceFromSource = Integer.MAX_VALUE;
    int bottleNeck = 0;

    boolean visited = false;

    public Node(int nodeId) {
        this.outgoingEdges = new ArrayList<>();
        this.incomingEdges = new ArrayList<>();
        this.nodeId = nodeId;
    }

    public void addEdge(Edge edge) {
        outgoingEdges.add(edge);
        edge.to().incomingEdges.add(edge);
    }
}
