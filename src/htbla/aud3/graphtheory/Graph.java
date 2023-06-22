package htbla.aud3.graphtheory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

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
        if (sourceNodeId == targetNodeId){
            Node node = graph.get(sourceNodeId);
            return new Path(node, node);
        }

        graph.values().forEach((path) -> {
            path.visited = false;
            path.bottleNeck = -1;
            path.distanceFromSource = Integer.MAX_VALUE;
            path.outgoingEdges.forEach((edge) -> {
                edge.valid = true;
            });
        });
        graph.get(sourceNodeId).distanceFromSource = 0;
        graph.get(sourceNodeId).bottleNeck = Integer.MAX_VALUE;
        recursiveLabelFlowPath(sourceNodeId, targetNodeId, 0);
        var pathNodeIds = recursiveFlowPathBuilding(sourceNodeId, graph.get(targetNodeId), new ArrayList<>(), null);//TODO: Validate if old methode is still working for new calculations - Spoiler it is not
        Collections.reverse(pathNodeIds);
        pathNodeIds.add(targetNodeId);
        if (pathNodeIds.size() < 2 || pathNodeIds.stream().distinct().toList().size() != pathNodeIds.size())
            return new Path();

        return new Path(pathNodeIds.stream().map(graph::get).toArray(Node[]::new));
    }

    private List<Integer> recursiveFlowPathBuilding(int sourceNodeId, Node targetNode, List<Integer> presentPath, Node lastNode) {
        if (sourceNodeId == targetNode.nodeId){
            return presentPath;
        }
        Node nextMaxBottleneck = targetNode.incomingEdges.stream()
                .filter(edge -> edge.valid)
                .filter(edge -> edge.getWeight() > 0)
                .map(Edge::from)
                .max(Comparator.comparingInt(n -> n.bottleNeck)).orElse(null);
        if (nextMaxBottleneck == null)
            return presentPath;
        Node finalNextMaxBottleneck = nextMaxBottleneck;
        nextMaxBottleneck = targetNode.incomingEdges.stream()
                .filter(edge -> edge.valid && edge.from().bottleNeck == finalNextMaxBottleneck.bottleNeck)
                .filter(edge -> edge.getWeight() > 0)
                .map(Edge::from)
                .min(Comparator.comparingInt(n -> n.distanceFromSource)).orElse(null);
        if (nextMaxBottleneck == null)
            return presentPath;
        if (presentPath.contains(nextMaxBottleneck.nodeId))
            return new ArrayList<>();
        presentPath.add(nextMaxBottleneck.nodeId);
        recursiveFlowPathBuilding(sourceNodeId, nextMaxBottleneck, presentPath, targetNode);
        return presentPath;
    }

    private void recursiveLabelFlowPath(int sourceNodeId, int targetNodeId, int distanceFromSource) {
        Node current = graph.get(sourceNodeId);
        current.distanceFromSource = distanceFromSource;
        if (sourceNodeId == targetNodeId)
            return;
        for (Edge e : current.outgoingEdges) {
            if (!e.valid)
                return;

            int bottleneck = Math.min(e.getWeight(), current.bottleNeck); // Setze alle visited incoming Nodes deren min bottleneck kleiner als current zu invalid && bei Pathbuilding gehe ich nur zu valid und visited.
            if (e.to().bottleNeck < bottleneck){
                e.to().incomingEdges.stream().filter(edge -> edge.from().visited && edge.from().nodeId != sourceNodeId).forEach(edge -> edge.valid = false);
                e.to().bottleNeck = bottleneck;
            }
            else e.valid = false;
        }
        current.visited = true;
        graph.values().stream()
                .filter(n -> !n.visited)
                .max(Comparator.comparingInt(n -> n.bottleNeck))
                .ifPresent((node) -> recursiveLabelFlowPath(node.nodeId, targetNodeId, distanceFromSource + 1));
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
        int maxFlow = 0;
        while(true){
            Path path = determineMaximumFlowPath(sourceNodeId, targetNodeId);
            List<Edge> edges = path.getEdges();
            if (edges.stream().noneMatch(Objects::nonNull))
                break;
            int currentFlow = edges.stream().mapToInt(Edge::getWeight).min().orElse(0);
            edges.forEach(edge -> edge.setWeight(edge.getWeight() - currentFlow));
            if (currentFlow <= 0)
                break;
            maxFlow += currentFlow;
        }

        return maxFlow;
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
        List<Edge> usedEdges = new ArrayList<>();
        while(true){
            Path path = determineMaximumFlowPath(sourceNodeId, targetNodeId);
            List<Edge> edges = path.getEdges();
            usedEdges.addAll(edges);
            if (edges.stream().noneMatch(Objects::nonNull))
                break;
            int currentFlow = edges.stream().mapToInt(Edge::getWeight).min().orElse(0);
            edges.forEach(edge -> edge.setWeight(0));
            if (currentFlow <= 0)
                break;
        }
        return usedEdges;
    }

}
