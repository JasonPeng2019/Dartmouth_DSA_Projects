import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * main method for testing the map creation methods
 */
public class ActorMovieTest {
    public static void main(String[] args) {
        try {
            KevinBaconAlgo kb = new KevinBaconAlgo();
            Map<String, String> actorMap = kb.actorsMap("C:\\Users\\14005\\IdeaProjects\\cs10\\assignments\\PS4\\bacon\\actorsTest.txt");
            Map<String, String> movieMap = kb.moviesMap("C:\\Users\\14005\\IdeaProjects\\cs10\\assignments\\PS4\\bacon\\moviesTest.txt");

            System.out.println("Actor Map:");
            for (Map.Entry<String, String> entry : actorMap.entrySet()) {
                System.out.println("ID: " + entry.getKey() + " Name: " + entry.getValue());
            }

            System.out.println("\nMovie Map:");
            for (Map.Entry<String, String> entry : movieMap.entrySet()) {
                System.out.println("ID: " + entry.getKey() + " Name: " + entry.getValue());
            }

            Map<String, List<String>> movieActorsMap = kb.movieActorsMap("C:\\Users\\14005\\IdeaProjects\\cs10\\assignments\\PS4\\bacon\\movie-actorsTest.txt");
            Map<String, List<String>> bigMap = kb.totalMap(actorMap, movieMap, movieActorsMap);

            System.out.println("\nTotal Map:");
            for (Map.Entry<String, List<String>> entry : bigMap.entrySet()) {
                System.out.println("Movie: " + entry.getKey() + " Actors: " + entry.getValue());
            }

            AdjacencyMapGraph<String, Set<String>> graph = kb.buildGraph(
                    "C:\\Users\\14005\\IdeaProjects\\cs10\\assignments\\PS4\\bacon\\actorsTest.txt",
                    "C:\\Users\\14005\\IdeaProjects\\cs10\\assignments\\PS4\\bacon\\moviesTest.txt",
                    "C:\\Users\\14005\\IdeaProjects\\cs10\\assignments\\PS4\\bacon\\movie-actorsTest.txt");

            System.out.println("\nActor-Movie Graph:");

            for (String source : graph.vertices()) {
                for (String target : graph.outNeighbors(source)) {
                    Set<String> labels = graph.getLabel(source, target);
                    System.out.println("Edge from " + source + " to " + target + " Labels: " + labels);
                }
            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
