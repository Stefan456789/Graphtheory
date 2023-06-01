package htbla.aud3.graphtheory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

/**
 * @author Jakob Wintersteiger, Stefan Wiesinger
 */
public class Graph {

    private Map<Integer, Node> graph;

    public Graph() {

    }

    /**
     * Reads a file that contains an adjacency matrix and stores it as a graph.
     *
     * @param adjacencyMatrix Graph as adjacency matrix.
     */

    // SOLLTE THEORETISCH FUNKTIONIEREN!!!
    public void read(File adjacencyMatrix) {
        graph = new HashMap<>();
        List<String> mapString = null;
        try {
            mapString = Files.readAllLines(adjacencyMatrix.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (int i = 0; i < mapString.size(); i++) {
            graph.put(i, new Node(i));
        }

        for (int i = 1; i < mapString.size(); i++) {
            String[] egdes = mapString.get(i).split(";");
            for (int j = 1; j < egdes.length; j++) {
                int edgeWeight = Integer.parseInt(egdes[j].trim());
                if (edgeWeight != 0) {
                    graph.get(i).addEdge(new Edge(graph.get(i), graph.get(j), edgeWeight));
                }
            }
        }
    }

    /**
     * Determines the shortest path between source nodeId and target nodeId an returns it
     * as path. The shortest path is defined as the path with the minimum sum of
     * edge weighs. If there is no path between the given nodeIds, the returned
     * path is empty (not null). If source and target nodeId are the same, the returned
     * path contains both nodeIds.
     *
     * @param sourceNodeId Node where the path starts.
     * @param targetNodeId Node where the path ends.
     * @return Shortest path
     */
    public Path determineShortestPath(int sourceNodeId, int targetNodeId) {
        if (sourceNodeId == targetNodeId){
            Node node = graph.get(sourceNodeId);
            node.distanceFromSource = 0;
            return new Path(node, node);
        }

        graph.values().forEach((path) -> {
            path.visited = false;
            path.distanceFromSource = Integer.MAX_VALUE;
            path.outgoingEdges.forEach((edge) -> {
                edge.valid = true;
            });
        });
        graph.get(sourceNodeId).distanceFromSource = 0;
        recursiveLabel(sourceNodeId, targetNodeId);
        var pathNodeIds = recursivePathBuilding(sourceNodeId, graph.get(targetNodeId), new ArrayList<>(), null);
        Collections.reverse(pathNodeIds);
        pathNodeIds.add(targetNodeId);
        if (pathNodeIds.size() < 2)
            return new Path();

        return new Path(pathNodeIds.stream().map(graph::get).toArray(Node[]::new));
    }

    private void recursiveLabel(int sourceNodeId, int targetNodeId) {
        Node current = graph.get(sourceNodeId);
        if (sourceNodeId == targetNodeId)
            return;
        for (Edge e : current.outgoingEdges) {
            if (!e.valid)
                return;
            int distanceToNextNode = e.getWeight() + current.distanceFromSource;
            if (e.to().distanceFromSource > distanceToNextNode)
                e.to().distanceFromSource = distanceToNextNode;
            else e.valid = false;
        }
        current.visited = true;
        graph.values().stream()
                .filter(n -> !n.visited)
                .min(Comparator.comparingInt(n -> n.distanceFromSource))
                .ifPresent((node) -> recursiveLabel(node.nodeId, targetNodeId));
    }

    private List<Integer> recursivePathBuilding(int sourceNodeId, Node targetNode, List<Integer> presentPath, Node lastNode) {
        if (sourceNodeId == targetNode.nodeId){
            return presentPath;
        }
        Node nextShortesedNode = targetNode.incomingEdges.stream()
                .filter(edge -> edge.valid)
                .map(Edge::from)
                .min(Comparator.comparingInt(n -> n.distanceFromSource)).orElse(null);
        if (nextShortesedNode == null)
            return presentPath;
        presentPath.add(nextShortesedNode.nodeId);
        recursivePathBuilding(sourceNodeId, nextShortesedNode, presentPath, targetNode);
        return presentPath;
    }

    /**
     * Determines the shortest path between source nodeId and target nodeId, considering
     * all via NodeIds in the given order, and returns it as path. The shortest path is
     * defined as the path with the minimum sum of edge weighs. If there is no path
     * between the given nodeIds, the returned path is empty (not null) ++
     * x. If source and
     * target nodeId are the same, the returned path contains both nodeIds.
     *
     * @param sourceNodeId Node where the path starts.
     * @param targetNodeId Node where the path ends.
     * @param viaNodeIds   Array of ordered via nodes.
     * @return Shortest path
     */
    public Path determineShortestPath(int sourceNodeId, int targetNodeId, int... viaNodeIds) {
        List<Path> paths = new ArrayList<>();

        if (viaNodeIds.length < 1) {
            return determineShortestPath(sourceNodeId, targetNodeId);
        }
        int sourceNode = sourceNodeId;
        int targetViaNode;
        Path path;

        for (int id : viaNodeIds) {
            targetViaNode = id;
            path = determineShortestPath(sourceNode, targetViaNode);
            List<Node> path2 = path.getNodes();
            path2.remove(path2.size() - 1);
            path.setPath(path2);
            paths.add(path);

            sourceNode = targetViaNode;
        }

        path = determineShortestPath(viaNodeIds[viaNodeIds.length - 1], targetNodeId);
        paths.add(path);
        Node[] pathNodes = paths.stream().flatMap(p -> p.getNodes().stream()).toArray(Node[]::new);

        return new Path(pathNodes);
    }

    public Path determineMaximumFlowPath(int sourceNodeId, int targetNodeId) {
        return null;
    }

    /**
     * Determines the maximum flow between source nodeId and target nodeId. The maximum
     * flow is defined as the maximum weight sum of all possible paths.
     *
     * @param sourceNodeId Node where the flow starts.
     * @param targetNodeId Node where the flow ends.
     * @return Maximum flow
     */
    public double determineMaximumFlow(int sourceNodeId, int targetNodeId) {
        return -1.0;
    }
    /**
     * Determines all edges that are used by the maximum flow and have a capacity of zero
     * (aka no weight) left. If there is no path between the given nodeIds, the
     * returned list is empty (not null).
     *
     * @param sourceNodeId ode where the flow starts.
     * @param targetNodeId Node where the flow ends.
     * @return List of edges with no capacity left
     */
    public List<Edge> determineBottlenecks(int sourceNodeId, int targetNodeId) {
        return new ArrayList<>();
    }

}
