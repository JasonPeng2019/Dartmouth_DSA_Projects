import java.io.IOException;
import java.util.*;

/**
 * main method for implementing kevin bacon game
 */
public class KevinBaconMain {
    public static void main(String[] args) {
        try {
            KevinBaconAlgo kb = new KevinBaconAlgo();
            AdjacencyMapGraph<String, Set<String>> graph = kb.buildGraph(
                    "C:\\Users\\14005\\IdeaProjects\\cs10\\assignments\\PS4\\bacon\\actors.txt",
                    "C:\\Users\\14005\\IdeaProjects\\cs10\\assignments\\PS4\\bacon\\movies.txt",
                    "C:\\Users\\14005\\IdeaProjects\\cs10\\assignments\\PS4\\bacon\\movie-actors.txt");

            // Initialize scanner, set Kevin Bacon to be initial center of universe
            Scanner scanner = new Scanner(System.in);
            String currentCenter = "Kevin Bacon";

            // Print possible commands to console
            System.out.println("Commands:");
            System.out.println("i: list actors with infinite separation from the current center");
            System.out.println("p <name>: find path from <name> to the current center of the universe");
            System.out.println("u <name>: make <name> the center of the universe");
            System.out.println("j: calculate average separation of current center");
            System.out.println("q: quit game");

            // Initialize a list and in int that will be used in the "u" and "i" commands
            Set<String> missingActors = new HashSet<>();
            AdjacencyMapGraph<String, Set<String>> tree = staticAlgos.bfs(graph, currentCenter, "Path");
            int unreachableActors = staticAlgos.missingVertices(graph, tree).size();

            while (true) {
                int connectedActors = 9235-unreachableActors;
                // Print how many reachable actors from the current center of the universe
                System.out.println(currentCenter + " game: " + currentCenter + " is connected to " + connectedActors + "/9235 actors");

                String input = scanner.nextLine();

                // Q - quit
                if (input.equals("q")) {
                    break;
                }
                // P - BFS, print path
                else if (input.startsWith("p ")) {
                    missingActors = staticAlgos.missingVertices(graph, tree);
                    String goal = input.substring(2);
                    String pathString = "";
                    if (missingActors.contains(goal)){
                        System.out.println("No path found between " + currentCenter + " and " + goal);
                    } else{
                        List<String> path = staticAlgos.getPath(tree, goal);
                        for (String j: path){
                            pathString += (j + " ");
                        }
                        System.out.println(pathString);
                    }
                }
                // U - change center of the universe
                else if (input.startsWith("u ")) {
                    String newCenter = input.substring(2);
                    currentCenter = newCenter;
                    // Recalculate how many disconnected actors
                    tree = staticAlgos.bfs(graph, currentCenter, "Path");
                    unreachableActors = staticAlgos.missingVertices(graph, tree).size();
                }
                // I - List all disconnected actors
                else if (input.equals("i")) {
                    // Generate list of all disconnected actors
                    missingActors = staticAlgos.missingVertices(graph, tree);
                    if (missingActors.isEmpty()) {
                        System.out.println("No actors with infinite separation from " + currentCenter);
                    } else {
                        System.out.println("Actors with infinite separation from " + currentCenter + ":");
                        // Iterate through list and print each actor
                        for (String actor : missingActors) {
                            System.out.println(actor);
                        }
                    }
                } else if (input.equals("j")){
                    System.out.println(staticAlgos.averageSeparation(graph, currentCenter));
                }
                // All other commands are invalid
                else {
                    System.out.println("Invalid command.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
