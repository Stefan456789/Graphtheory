package htbla.aud3.graphtheory;

// JUnit 5 (IntelliJ)
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

// JUnit 4 (NetBeans)
//import org.junit.Test;
//import static org.junit.Assert.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * @author twelsch
 */
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
public class GraphTestFlow {

    public File file = findFile("Linz_Flussproblem.csv");

    public GraphTestFlow() {}

    @Test
    // Testing simple flows: 1->2 (2250), 25->26 (500) and 29->28 (500)
    public void b1_determineMaximumFlowBasic() {
        System.out.println("b1_determineMaximumFlowBasic");

        Graph g = new Graph();
        g.read(file);

        double flow;

        flow = g.determineMaximumFlow(1, 2);
        assertEquals(2250, flow, 0.1);

        flow = g.determineMaximumFlow(25, 26);
        assertEquals(500, flow, 0.1);

        flow = g.determineMaximumFlow(29, 28);
        assertEquals(500, flow, 0.1);
    }

    @Test
    // Testing more complex flows, including one-way streets: 4->3 (2000) and 1->31 (1250)
    public void b2_determineMaximumFlowAdvanced() {
        System.out.println("b2_determineMaximumFlowAdvanced");

        Graph g = new Graph();
        g.read(file);

        double flow;

        flow = g.determineMaximumFlow(4, 3);
        assertEquals(2000, flow, 0.1);

        flow = g.determineMaximumFlow(1, 31);
        assertEquals(1250, flow, 0.1);

    }

    @Test
    // Testing flows from one node to itself and unreachable nodes: 48->48 (0), 1->55 (0)
    public void b3_determineMaximumFlowExpert() {
        System.out.println("b3_determineMaximumFlowAdvanced");

        Graph g = new Graph();
        g.read(file);

        double flow;

        flow = g.determineMaximumFlow(48, 48);
        assertEquals(0, flow, 0.1);

        flow = g.determineMaximumFlow(1, 55);
        assertEquals(0, flow, 0.1);
    }

    @Test
    // Testing simple flows and their bottlenecks: 1->2 (Edge[1,2], Edge[1,29]), 25->26 (Edge[25,26]) and 29->28 (Edge[28,29], Edge[27,28])
    public void c1_determineBottlenecksBasic() {
        System.out.println("c1_determineMaximumFlowBasic");

        Graph g = new Graph();
        g.read(file);

        List<Edge> edges;
        List<TestEdge> expected;

        edges = g.determineBottlenecks(1, 2);
        expected = Arrays.asList(
                new TestEdge(1, 2),
                new TestEdge(1, 29)
        );
        assertContainsEdges(expected, edges);


        edges = g.determineBottlenecks(25, 26);
        expected = Arrays.asList(
                new TestEdge(25, 26)
        );
        assertContainsEdges(expected, edges);

        edges = g.determineBottlenecks(29, 28);
        expected = Arrays.asList(
                new TestEdge(28, 29),
                new TestEdge(27, 28)
        );
        assertContainsEdges(expected, edges);
    }

    @Test
    // Testing more complex flows and their bottlenecks, including one-way streets: 4->3 (Edge[4,3], Edge[34,3], Edge[31,2]) and 1->31 (Edge[1,29], Edge[32,31], Edge[30,31])
    public void c2_determineBottlenecksAdvanced() {
        System.out.println("c2_determineMaximumFlowAdvanced");

        Graph g = new Graph();
        g.read(file);

        List<Edge> edges;
        List<TestEdge> expected;

        edges = g.determineBottlenecks(4, 3);
        expected = Arrays.asList(
                new TestEdge(4, 3),
                new TestEdge(34, 3),
                new TestEdge(31, 2)
        );
        assertContainsEdges(expected, edges);

        edges = g.determineBottlenecks(1, 31);
        expected = Arrays.asList(
                new TestEdge(1, 29),
                new TestEdge(32, 31),
                new TestEdge(30, 31)
        );
        assertContainsEdges(expected, edges);
    }

    @Test
    // Testing flows and their bottlenecks from one node to itself and unreachable nodes: 48->48 (no Edges), 1->55 (no Edges)
    public void c3_determineBottlenecksExpert() {
        System.out.println("c3_determineMaximumFlowExpert");

        Graph g = new Graph();
        g.read(file);

        List<Edge> edges;
        List<TestEdge> expected;

        edges = g.determineBottlenecks(48, 48);
        assertTrue(edges.isEmpty());

        edges = g.determineBottlenecks(1, 55);
        assertTrue(edges.isEmpty());
    }

    private void assertContainsEdges(List<TestEdge> expected, List<Edge> actual) {
        for (TestEdge te : expected) {
            boolean found = false;
            for (Edge e : actual) {
                if (te.equalTo(e)) {
                    found = true;
                    break;
                }
            }

            if (!found) fail();
        }
    }

    private File getFile(String fileName) {
        System.out.println(GraphTestFlow.class.getResource(fileName));

        InputStream is = GraphTestFlow.class.getResourceAsStream(fileName);
        File file = new File(fileName);

        try (FileOutputStream outputStream = new FileOutputStream(file, false)) {
            int read;
            byte[] bytes = new byte[8192];
            while ((read = is.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        }
        catch (IOException e) {
            fail("file " + fileName + " not found");
        }

        return file;
    }

    private File findFile(String fileName) {
        File result = null;

        try (Stream<java.nio.file.Path> walkStream = Files.walk(Paths.get(System.getProperty("user.dir")))) {
            java.nio.file.Path path = walkStream.filter(p -> p.toFile().isFile()).filter(f -> f.toString().endsWith(fileName)).findFirst().orElseThrow(() -> new NoSuchElementException("No value present"));
            result = path.toFile();
        }
        catch (IOException e) {
            fail("file " + fileName + " not found");
        }

        return result;
    }

    private static class TestEdge {

        int from;
        int to;

        TestEdge(int from, int to) {
            this.from = from;
            this.to = to;
        }

        boolean equalTo(Edge e) {
            if (from == e.getFirstNodeId() && to == e.getSecondNodeId()
                    || to == e.getFirstNodeId() && from == e.getSecondNodeId()) {
                return true;
            }

            return false;
        }

    }
}
