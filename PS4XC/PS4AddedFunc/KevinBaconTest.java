import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * test for graph builder
 */
public class KevinBaconTest {
    public static void main(String[] args) {
        try {
            KevinBaconAlgo kb = new KevinBaconAlgo();

            AdjacencyMapGraph<String, Set<String>> graph = kb.buildGraph("C:\\Users\\14005\\IdeaProjects\\cs10\\assignments\\PS4\\bacon\\actorsTest.txt",
                    "C:\\Users\\14005\\IdeaProjects\\cs10\\assignments\\PS4\\bacon\\moviesTest.txt",
                    "C:\\Users\\14005\\IdeaProjects\\cs10\\assignments\\PS4\\bacon\\movie-actorsTest.txt");

            String sourceActor = "Kevin Bacon";
            String targetActor = "Bob";

            if (!graph.hasVertex(sourceActor) || !graph.hasVertex(targetActor)) {
                System.out.println("Source or target actor not found in the graph.");
            } else {
                AdjacencyMapGraph<String, Set<String>> pathTree = staticAlgos.bfs(graph, sourceActor, "Path");
                List<String> path = staticAlgos.getPath(pathTree, targetActor);
                if (path.isEmpty()) {
                    System.out.println("No path found between " + sourceActor + " and " + targetActor);
                } else {
                    System.out.println("Shortest path from " + sourceActor + " to " + targetActor + ":");
                    for (String actor : path) {
                        System.out.println(actor);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}