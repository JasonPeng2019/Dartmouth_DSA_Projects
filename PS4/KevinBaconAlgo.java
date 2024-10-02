import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * main Kevin Bacon algorithms in a class
 */
public class KevinBaconAlgo {

    public static String center = "Kevin Bacon";

    /**
     * create a map for the actors with Key <actor ID> and value <actor>
     * @param filePath
     * @return
     * @throws IOException
     */
    public Map<String, String> actorsMap(String filePath) throws IOException {
        // make a new map to return
        Map<String, String> map = new HashMap<>();
        // create a list of the lines
        List<String> lines = Files.readAllLines(Paths.get(filePath));

        // for each line, split it into two parts
        for (String line : lines) {
            String[] parts = line.split("\\|");
            // put part 1 as the key, part 2 as the value, and add it to the map
            if (parts.length == 2) {
                map.put(parts[0].trim(), parts[1].trim());
            }
        }

        return map;
    }

    /**
     * create a map for movies with key <movie ID> and value <movie>
     * @param filePath
     * @return
     * @throws IOException
     */
    public Map<String, String> moviesMap(String filePath) throws IOException {
        // make a new map to return
        Map<String, String> map = new HashMap<>();
        // create a list of the lines
        List<String> lines = Files.readAllLines(Paths.get(filePath));

        // for each line, split it into two parts
        for (String line : lines) {
            String[] parts = line.split("\\|");
            // put part 1 as the key, part 2 as the value, and add it to the map
            if (parts.length == 2) {
                map.put(parts[0].trim(), parts[1].trim());
            }
        }

        return map;
    }

    /**
     * create a map that with Key <Movie ID> and Value <List of actor IDs in that movie>
     * * @param filePath
     * @return
     * @throws IOException
     */
    public Map<String, List<String>> movieActorsMap(String filePath) throws IOException {
        // create a new map to return
        Map<String, List<String>> movieActorsMap = new HashMap<>();
        // create a list of lines
        List<String> lines = Files.readAllLines(Paths.get(filePath));

        // for each line in the list of lines. split it into two parts. movieID as one part, actorID as the 2nd.

        for (String line : lines) {
            String[] parts = line.split("\\|");
            if (parts.length == 2) {
                String movieID = parts[0].trim();
                String actorID = parts[1].trim();
                // then if the movieID isn't in the map, make a new entry and add the corresponding actor ID map
                if (!movieActorsMap.containsKey(movieID)) {
                    movieActorsMap.put(movieID, new ArrayList<String>());
                    movieActorsMap.get(movieID).add(actorID);
                } // if its in the map, add the actor ID to the existing list of actor IDs for each movie ID
                else {
                    movieActorsMap.get(movieID).add(actorID);
                }
            }
        }

        return movieActorsMap;
    }

    /**
     * create a map with Key <movie> and Value <List of actors that participated in that movie>
     * @param actorsMap
     * @param moviesMap
     * @param movieActorsMap
     * @return
     * @throws IOException
     */
    public Map<String, List<String>> totalMap(Map<String, String> actorsMap, Map<String, String> moviesMap, Map<String,
            List<String>> movieActorsMap) throws IOException {
        // create a new total map to return
        Map<String, List<String>> totalMap = new HashMap<>();

        // for each entry in the map of <MovieID, List of Actor IDs>.
        // retrieve the MovieID and List of Actor IDs from the map. Then retrieve
        // the movie name using the movie ID, and retrieve a list of actors for the list of actor IDs.
        for (Map.Entry<String, List<String>> entry : movieActorsMap.entrySet()) {
            String movieID = entry.getKey();
            List<String> actorIDs = entry.getValue();
            String movieName = moviesMap.get(movieID);
            List<String> actorNames = new ArrayList<>();

            // for each actor in the list of actorIDs, add the actornames to a new map.
            for (String actorID : actorIDs) {
                String actorName = actorsMap.get(actorID);
                if (actorName != null) {
                    actorNames.add(actorName);
                }
            }

            // if there is an actual movie name and actor name, add it to the totalMap
            if (movieName != null && !actorNames.isEmpty()) {
                totalMap.put(movieName, actorNames);
            }
        }
        // return a map of <movie name, list of actors in movie>
        return totalMap;
    }

    /**
     * build a graph based on the actors, movies, and actorsMovies files
     * @param actorsPath
     * @param moviesPath
     * @param actorsMoviesPath
     * @return
     * @throws IOException
     */
    public AdjacencyMapGraph<String, Set<String>> buildGraph(String actorsPath, String moviesPath, String
            actorsMoviesPath) throws IOException {
        AdjacencyMapGraph<String, Set<String>> finalGraph = new AdjacencyMapGraph<>();
        // for each each key in actorMap, add the key (actor name) to the final Graph as vertex.
        Map<String, String> mapActors = actorsMap(actorsPath);
        Map<String, String> mapMovies = moviesMap(moviesPath);
        Map<String, List<String>> mapMoviesActorsID = movieActorsMap(actorsMoviesPath);
        Map<String, List<String>> mapMoviesActors = totalMap(mapActors, mapMovies, mapMoviesActorsID);

        for (Map.Entry<String, String> entry : mapActors.entrySet()) {
            finalGraph.insertVertex(entry.getValue());
        }
        // then for each movie in the totalMap <movie name, List of actors>, determine which pairs of actors correspond
        // to the movie based on totalMap. For each pair, add an edge to the graph if the edge does not already exist.
        // the edge should be an empty set. then add the movie to the edge.
        for (Map.Entry<String, List<String>> entry : mapMoviesActors.entrySet()) {

            List<String> actorsInMovie = entry.getValue();
            for (int i = 0; i < actorsInMovie.size(); i++) {
                for (int j = i + 1; j < actorsInMovie.size(); j++) {
                    String actor1 = actorsInMovie.get(i);
                    String actor2 = actorsInMovie.get(j);
                    if (!finalGraph.hasEdge(actor1, actor2)) {
                        finalGraph.insertUndirected(actor1, actor2, new HashSet<String>());
                    }
                    finalGraph.getLabel(actor1, actor2).add(entry.getKey());
                }

            }
        }
        return finalGraph;
    }

    /**
     * change the center of the Kevin Bacon algorithm to a new name
     * @param name
     */
    public void changeCenter(String name) {
        center = name;
    }
}
