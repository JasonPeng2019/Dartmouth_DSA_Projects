import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class wikiVoteParser {
    public static String center = "30";
    public static AdjacencyMapGraph<String, Integer> buildMap (String filePath) throws IOException {
        AdjacencyMapGraph<String, Integer> returnGraph = new AdjacencyMapGraph<String, Integer>();
        Map<String, List<String>> map = new HashMap<>();
        // create a list of the lines
        List<String> lines = Files.readAllLines(Paths.get(filePath));

        // for each line, split it into two parts
        for (String line : lines) {
            String[] parts = line.split("\\t");
            // put part 1 as the key, part 2 as the value, and add it to the map
            if (parts.length == 2) {
                if (!map.containsKey(parts[0].trim())) {
                    map.put(parts[0].trim(), new ArrayList<String>());
                    map.get(parts[0].trim()).add(parts[1].trim());
                }
                else {
                    map.get(parts[0].trim()).add(parts[1].trim());
                }
            }
        }

        for (Map.Entry<String, List<String>> entry: map.entrySet()){
            if (!returnGraph.hasVertex(entry.getKey())) {
                returnGraph.insertVertex(entry.getKey());
            }
            for (String outID: entry.getValue()){
                if (!returnGraph.hasVertex(outID)) {
                    returnGraph.insertVertex(outID);
                }
                returnGraph.insertDirected(entry.getKey(), outID, 0);
            }
        }
        return returnGraph;
    }

    public static List<String> getRandomPath(AdjacencyMapGraph<String, Integer> graph, String target){
        ArrayList<String> path = new ArrayList<>();
        //current = center
        String current = center;
        //visited = emptySet
        HashSet<String> visited = new HashSet<>();
        //while current is not target:
        path.add(current);
        int depth = 0;
        while (!current.equals(target)) {
            //visited.add(current)
            visited.add(current);
            //get a list of all neighbors (minus visited)
            ArrayList<String> neighbors = new ArrayList<>();
            for (String neighbor: graph.vertices()){
                if (!visited.contains(neighbor)){
                    neighbors.add(neighbor);
                }
            }
            // if no neighbors, exit
            if (neighbors.isEmpty() || depth > 100000){
                System.out.println("Path could not be found this iteration of random walk. Path maxxed out" +
                        "or did not exist. Path may or may" +
                        "not exist");
                return new ArrayList<>();
            } else {
                depth ++;
                Random rand = new Random(); // Create a Random object
                int randomIndex = rand.nextInt(neighbors.size());
                String randomElement = neighbors.get(randomIndex);
                current = randomElement;
                path.add(current);
            }
        }
        return path;//return a path
    }
}
