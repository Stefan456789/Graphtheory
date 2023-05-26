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

/**
 * @author twelsch
 */
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
public class GraphTestPath {

    public File file = findFile("Linz_Suchproblem.csv");

    public GraphTestPath() {}

    @Test
    // Testing simple paths.
    public void a1_determineShortestPathBasic() {
        System.out.println("a1_determineShortestPathBasic");

        Graph g = new Graph();

        g.read(file);

        Path path;

        path = g.determineShortestPath(1, 4);
        assertEquals(780.0, path.computeWeight(), 0.1);
        assertArrayEquals(new int[]{1, 2, 3, 4}, path.getNodeIds());

        path = g.determineShortestPath(13, 41);
        assertEquals(410, path.computeWeight(), 0.1);
        assertArrayEquals(new int[]{13, 43, 42, 41}, path.getNodeIds());

    }

    @Test
    // Testing more complex paths, including one-way streets.
    public void a2_determineShortestPathAdvanced() {
        System.out.println("a2_determineShortestPathAdvanced");

        Graph g = new Graph();
        g.read(file);

        Path path;

        path = g.determineShortestPath(1, 31);
        assertEquals(930.0, path.computeWeight(), 0.1);
        assertArrayEquals(new int[]{1, 2, 3, 34, 33, 32, 31}, path.getNodeIds());

        path = g.determineShortestPath(5, 4);
        assertEquals(350.0, path.computeWeight(), 0.1);
        assertArrayEquals(new int[]{5, 34, 3, 4}, path.getNodeIds());

    }

    @Test
    // Testing paths from one node to itself, unreachable nodes and via-paths.
    public void a3_determineShortestPathExpert() {
        System.out.println("a3_determineShortestPathExpert");

        Graph g = new Graph();
        g.read(file);

        Path path;

//        path = g.determineShortestPath(48, 48);
//        assertEquals(0.0, path.computeWeight(), 0.1);
//        assertArrayEquals(new int[]{48, 48}, path.getNodeIds());
//
//        path = g.determineShortestPath(1, 55);
//        assertEquals(-1.0, path.computeWeight(), 0.1);
//        assertArrayEquals(new int[]{}, path.getNodeIds());

        path = g.determineShortestPath(1, 2, 30);
        assertEquals(1290.0, path.computeWeight(), 0.1);
        assertArrayEquals(new int[]{1, 29, 30, 31, 2}, path.getNodeIds());

    }

    private File getFile(String fileName) {
        System.out.println(GraphTestPath.class.getResource(fileName));

        InputStream is = GraphTestPath.class.getResourceAsStream(fileName);
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

}