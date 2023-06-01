package htbla.aud3.graphtheory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jakob Wintersteiger, Stefan Wiesinger
 */
public class Main {
    
    public static void main(String[] args) {
        Graph graph = new Graph();
        graph.read(new File("Linz_Flussproblem.csv"));
        System.out.println(graph.determineMaximumFlow(1,40));
        //test.getNodes().stream().map(n -> n.nodeId).forEach(System.out::println);
    }
}
