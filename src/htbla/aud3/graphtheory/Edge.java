package htbla.aud3.graphtheory;

/**
 * @author Jakob Wintersteiger, Stefan Wiesinger
 */
public class Edge {
    private Node from;
    private Node to;
    private int weight;
    private final int oldWeight;
    public boolean valid = true;

    public Edge (Node from, Node to, int length){
        this.from = from;
        this.to = to;
        this.weight = length;
        this.oldWeight = length;
    }


    public int getFirstNodeId() {
        return from.nodeId;
    }
    
    public int getSecondNodeId() {
        return to.nodeId;
    }

    public int getWeight() {
        return weight;
    }
    public void resetWeight(){
        weight = oldWeight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public Node to() {
        return to;
    }
    public Node from() {
        return from;
    }
}
