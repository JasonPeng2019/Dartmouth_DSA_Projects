import src.ArrayListStack;

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
                if (!movieActorsMap.containsKey(movieID)){
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

        for (Map.Entry<String, String> entry: mapActors.entrySet()){
            finalGraph.insertVertex(entry.getValue());
        }
        // then for each movie in the totalMap <movie name, List of actors>, determine which pairs of actors correspond
        // to the movie based on totalMap. For each pair, add an edge to the graph if the edge does not already exist.
        // the edge should be an empty set. then add the movie to the edge.
        for (Map.Entry<String, List<String>> entry: mapMoviesActors.entrySet()){

            List <String> actorsInMovie = entry.getValue();
            for (int i = 0; i < actorsInMovie.size(); i++){
                for (int j = i+1; j < actorsInMovie.size(); j++){
                    String actor1 = actorsInMovie.get(i);
                    String actor2 = actorsInMovie.get(j);
                    if (!finalGraph.hasEdge(actor1, actor2)){
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
    public void changeCenter(String name){
        center = name;
    }

    /**
     * XC FUNCTIONALITY:
     * a bfs search that STOPS when goal is found. More space conservative and can be used later for larger data sets.
     * Tried to use with Wikipedia data set but the data set was still too big. Will try random-walk next.
     * @param graph
     * @param goal
     * @return
     * @throws Exception
     */
    public List<String> bfsSearch(AdjacencyMapGraph<String, Set<String>> graph, String goal)
            throws Exception{

        if (!graph.hasVertex(goal)){
            System.out.println("goal not in graph");
            return new ArrayList<>();
        }

        // create a queue of "to Visit" and a map of visited actors. The map will contain keys of visited
        // and ALSO map the visited vertices back to their predecessor.
        AdjacencyMapGraph<String, Set<String>> bfs = new AdjacencyMapGraph<>();
        SLLQueue<String> toVisit = new SLLQueue<>();
        Map<String, String> visitedAndBacktrack = new HashMap<>();

        //enqueue the start, then add the start to "visited"
        bfs.insertVertex(center);
        toVisit.enqueue(center);
        visitedAndBacktrack.put(center, null); // Start actor has no predecessor

        // while there are still elements in the queue (meaning there are breadths not searched yet!)
        while (!toVisit.isEmpty()) {
            // currentActor is the front of the queue
            String currentActor = toVisit.dequeue();
            if (currentActor.equals(goal)) {
                // Reconstruct the path from startActor to goalActor. Do this by getting the backtrack map,
                // iterating through the map, and adding each element of the backtrack map to the front of the path
                // so that the beginning of the path is the center of the graph and the end is the goal.
                LinkedList<String> path = new LinkedList<>();
                for (String actor = goal; actor != null; actor = visitedAndBacktrack.get(actor)) {
                    path.add(0, actor);
                }

                return path;
            }

            // Queue up all the connected actors. For each neighbor in the set of outNeighbors of
            // the current vertex, if the neighbor isn't in the "visited" map, put it in the visited map (since
            // it's been seen and will eventually be visited) and add it to toVisit
            for (String neighbor : graph.outNeighbors(currentActor)) {
                if (!visitedAndBacktrack.containsKey(neighbor)) {
                    visitedAndBacktrack.put(neighbor, currentActor);
                    toVisit.enqueue(neighbor);
                }
            }
        }
        System.out.println("goal actor unreachable");
        return new ArrayList<>();
    }


    /**
     * XC FUNCTIONALITY:
     * a function that finds the number of unique actors within a certain degree of separation
     * @param graph
     * @param steps
     * @return
     * @throws Exception
     */
    public int inPathLength(AdjacencyMapGraph<String, Set<String>> graph, int steps) throws Exception{
        // do a BFS.

        // 1: keep track of breadths. When reach limit, stop search. Whenever a vertex
        // that hasn't been visited before is reached, add to a counter.
        // how do we keep track of breadths?
        // assign a "breadth" to each value in the queue and add it to a map that keeps track of the breadth value
        // for each vertex.
        // when you hit a "cycle through the neighbors". for each of the neighbors, increment the breadth by 1
        // and add those neighbors to the map.


        // create a queue of "to Visit" and a map of visited actors. The map will contain keys of visited
        // and ALSO map the visited vertices back to their predecessor.
        SLLQueue<String> toVisit = new SLLQueue<String>();
        Map<String, String> visitedAndBacktrack = new HashMap<>();

        // create a "breadth" tracker
        Map<String, Integer> breadth = new HashMap<>();
        int n = 0;
        int counter = 0;

        //enqueue the start, then add the start to "visited"
        toVisit.enqueue(center);
        visitedAndBacktrack.put(center, null); // Start actor has no predecessor
        breadth.put(center, n);

        // while there are still elements in the queue (meaning there are breadths not searched yet!)
        while (!toVisit.isEmpty()) {
            // currentActor is the front of the queue
            String currentActor = toVisit.dequeue();
            // increment the counter for every "visited" actor
            counter += 1;
            // check if we've hit the breadth depth:
            if (breadth.get(currentActor) < steps) {
                // Queue up all the connected actors. For each neighbor in the set of outNeighbors of
                // the current vertex, if the neighbor isn't in the "visited" map, put it in the visited map (since
                // it's been seen and will eventually be visited) and add it to toVisit.
                // add the neighbor to the breadth map (as it's been seen), and increment the breadth by 1.
                for (String neighbor : graph.outNeighbors(currentActor)) {
                    if (!visitedAndBacktrack.containsKey(neighbor)) {
                        visitedAndBacktrack.put(neighbor, currentActor);
                        toVisit.enqueue(neighbor);
                        breadth.put(neighbor, breadth.get(currentActor) + 1);
                    }
                }
            }
        }
    return counter;
    }

    /**
     * XC FUNCTIONALITY:
     * a function that calculates average path length using BFS. This is a redundant function (didn't realize
     * I already had this functionality until I finished coding and looked back at my static Algorithms).
     * @param graph
     * @return
     * @throws Exception
     */
    public double avgPathLength(AdjacencyMapGraph<String, Set<String>> graph) throws Exception{
        // do full BFS, without stopping
        // keep track of depth throughout
        // sum up all the depths and divide by the number of vertices

        // create a queue of "to Visit" and a map of visited actors. The map will contain keys of visited
        // and ALSO map the visited vertices back to their predecessor.
        SLLQueue<String> toVisit = new SLLQueue<String>();
        Map<String, String> visitedAndBacktrack = new HashMap<>();

        // create a "breadth" tracker
        Map<String, Double> breadth = new HashMap<>();
        double n = 0;

        //enqueue the start, then add the start to "visited"
        toVisit.enqueue(center);
        visitedAndBacktrack.put(center, null); // Start actor has no predecessor
        breadth.put(center, n);

        // while there are still elements in the queue (meaning there are breadths not searched yet!)
        while (!toVisit.isEmpty()) {
            // currentActor is the front of the queue
            String currentActor = toVisit.dequeue();
                // Queue up all the connected actors. For each neighbor in the set of outNeighbors of
                // the current vertex, if the neighbor isn't in the "visited" map, put it in the visited map (since
                // it's been seen and will eventually be visited) and add it to toVisit.
                // add the neighbor to the breadth map (as it's been seen), and increment the breadth by 1.
            for (String neighbor : graph.outNeighbors(currentActor)) {
                if (!visitedAndBacktrack.containsKey(neighbor)) {
                    visitedAndBacktrack.put(neighbor, currentActor);
                    toVisit.enqueue(neighbor);
                    breadth.put(neighbor, breadth.get(currentActor) + 1);
                }
            }
        }

        double sum = 0;
        double counter = 0;
        for (Map.Entry<String, Double> entry: breadth.entrySet()){
            sum += entry.getValue();
            counter += 1;
        }

        return sum/counter;


    }

    /**
     * XC FUNCTIONALITY:
     * a function that sorts the actors by degrees.
     * @param actorsMap
     * @param graph
     * @return
     */
    public ArrayList<String> degreeList(Map<String, String> actorsMap, AdjacencyMapGraph<String, Set<String>> graph){

        // create a priority queue to add actors to
        PriorityQueue<actorClass> actorPQ = new PriorityQueue<>();
        ArrayList<String> actorList = new ArrayList<>();

        for (Map.Entry<String, String> element : actorsMap.entrySet()){
            String actor = element.getValue();
            //for every actor in actors Map, calculate the outdegree of each actor
            int outDegree = graph.outDegree(actor);
            actorClass actorObj = new actorClass(actor, outDegree);
            // add each actor in actorsMap to the priorityQueue
            actorPQ.add(actorObj);
        }
        while (!actorPQ.isEmpty()) {
            actorList.add(actorPQ.poll().getActor()); // Poll from priority queue to maintain order
        }
        return actorList;
    }

    /**
     * XC FUNCTIONALITY:
     * a function that sorts actors by total degrees of separation from everybody
     * @param actorsMap
     * @param graph
     * @return
     * @throws Exception
     */
    public ArrayList<String> pathLengthList(Map<String, String> actorsMap, AdjacencyMapGraph<String,
            Set<String>> graph) throws Exception{
        PriorityQueue<actorClass2> pq = new PriorityQueue<>();
        ArrayList<String> actorList = new ArrayList<>();
        //for each actor in actorsMap, set the center as actor.
        for (Map.Entry<String, String> element: actorsMap.entrySet()){
            // Calculate the average Path length (separation)
            changeCenter(element.getValue());
            double pathLength = avgPathLength(graph);
            actorClass2 actorObj = new actorClass2(element.getValue(), pathLength);
            // for each actor, then assign to a priorityQueue.
            pq.add(actorObj);
        }
        //Then poll the priorityQueue to add to an arrayList.
        while (!pq.isEmpty()){
            actorList.add(pq.poll().getActor());
        }
        return actorList;
    }

    static Map<String, Double> betweenessValues = new HashMap<>();

    /**
     * Betweenness Centrality calculator for an individual target vertex
     * @param actorsMap
     * @param graph
     * @param target
     * @return
     */
    public Double betweennessCentrality (Map<String, String> actorsMap,AdjacencyMapGraph<String, Set<String>> graph,
                                      String target) throws Exception{
        // use Brandes Algorithm (found resources from University of Cambridge epubs)
        // based on reading like... 30+ pages of straight CS and various sources + stack exchange,
        // this is my understanding:

        // 1a) keep a map holding the betweenness values for each node, obviously
        // this is static map declared outside
        //2) traverse the map from a every single node (source) using BFS
        // 3) need a modified BFS:
            //    Keep a map of the various "sigma V" values per traversal
            //    Start BFS from the source node, marking it with a depth of 0 and enqueuing it.
            //    Dequeue a node from the front of the queue, process it, and mark it as visited.
            //    For each unvisited neighbor of this node, mark it with the current depth + 1 and enqueue it.
            //    If a neighbor is already visited and the current depth + 1 is equal to the neighbor's recorded depth, it means another shortest path to this neighbor is found; you can increment the occurrence but do not enqueue it again.
            //    If the current depth + 1 is greater than the neighbor's recorded depth, ignore this neighbor since a shorter path has already been found.
            //    Continue until the queue is empty.
        //4) calculate sigma V by determining how many times the shortest breadth path finds V, but DO NOT include any of the finds past the first depth you found it at
        //5) backtrack and use the Sigma W (taken from the map) and Sigma V(taken from the map) to increment the dependency value of each node (increasing up to the root) using the formula
        //6) after iterating through all the nodes in the graph, you've reached the final dependency values for each node
        // once you reach the final dependency value for each node,

        //2) traverse the map from a every single node (source) using BFS


        //Betweenness Centrality Map (C_b): This is the final output map where the betweenness centrality score for each node is stored.
        Map<String, Double> betweennessMap = new HashMap<>();
        double totalPaths = 0;
        for (Map.Entry<String, String> actor0: actorsMap.entrySet()){
            betweennessMap.put(actor0.getValue(), 0.0);
        }

        for (Map.Entry<String, String> actor: actorsMap.entrySet()){
            // Dependency Map (δ): This map is used during the backtracking
            // process to accumulate the betweenness centrality scores.
            // Initially, all nodes have a dependency score of 0.
            Map <String, Double> dependencyMap = new HashMap<>();
            for (Map.Entry<String, String> actor0: actorsMap.entrySet()){
                dependencyMap.put(actor0.getValue(), 0.0);
            }

            // Sigma Map (σ): This map holds the count of the shortest paths
            // from the source to each node. For the source node, σ[source] = 1,
            // and for all other nodes, it starts at 0 and is updated during the BFS.
            Map <String, Double> sigmaMap = new HashMap<>();
            for (Map.Entry<String, String> actor0: actorsMap.entrySet()){
                sigmaMap.put(actor0.getValue(), 0.0);
            }

            // 3) need a modified BFS:

            // Depths map
            Map<String, Double> depths = new HashMap<>();

            String actorName = actor.getValue();

            // Need a stack to keep track of which nodes visited. Children are always visited after parents,
            // so can just add to stack without caring about the order.
            ArrayListStack<String> actorStack = new ArrayListStack<>();

            // Predecessor Map (P): This map holds the list of predecessors for each node.
            // A predecessor is a node that directly precedes another node on a shortest path
            // from the source. There can be multiple predecessors if there are multiple shortest paths.
            Map<String, List<String>> visitedAndBacktrack = new HashMap<>();


            // Start BFS from the source node, marking it with a depth of 0 and enqueuing it.
            SLLQueue<String> toVisit = new SLLQueue<>();

            toVisit.enqueue(actorName);
            actorStack.push(actorName);
            sigmaMap.put(actorName, 1.0);
            visitedAndBacktrack.put(actorName, new ArrayList<>());
            double depth = 0;
            depths.put(actorName, depth);

            while (!toVisit.isEmpty()) {
                //    Dequeue a node from the front of the queue.
                String currentActor = toVisit.dequeue();
                int i = 0;
                for (String neighbor: graph.outNeighbors(currentActor)) {
                   // System.out.println("!"+neighbor);
                    //System.out.println(visitedAndBacktrack);
                    //System.out.println(depths);
                    //    For each unvisited neighbor of this node, mark it with the current depth + 1 and enqueue it.
                    if (!visitedAndBacktrack.containsKey(neighbor)) {
                        actorStack.push(neighbor);
                        toVisit.enqueue(neighbor);
                        visitedAndBacktrack.put(neighbor, new ArrayList<>());
                        visitedAndBacktrack.get(neighbor).add(currentActor);
                        depths.put(neighbor, depths.get(currentActor) + 1);
                        sigmaMap.put(neighbor, sigmaMap.get(currentActor));
                    }
                    //    If a neighbor is already visited and the current depth + 1 is equal to the neighbor's recorded
                    //    depth, it means another shortest path to this neighbor is found; you can increment the occurrence
                    //    but do not enqueue it again.
                    else if (visitedAndBacktrack.containsKey(neighbor) && depths.get(neighbor).equals(
                            (depths.get(currentActor )+1))){
                        visitedAndBacktrack.get(neighbor).add(currentActor);
                        double getVal = sigmaMap.get(neighbor);
                        getVal += sigmaMap.get(currentActor);
                        sigmaMap.put(neighbor, getVal);
                    }

                    //    If the current depth + 1 is greater than the neighbor's recorded depth,
                    //    ignore this neighbor since a shorter path has already been found.
                    else if (visitedAndBacktrack.containsKey(neighbor) && depths.get(neighbor) <
                            (depths.get(currentActor )+1)) {
                    }
                    else {
                        System.out.println("unexpected error happened. BFS needs debug");
                        i++;
                        System.out.println(i);
                        if (i>1) {
                            System.exit(0);
                        }
                    }
                }
                //    Continue until the queue is empty.
            }
            // now the stack has been built and all appropriate maps are created. Now backtrack:
            // While S is not empty:
            while (!actorStack.isEmpty()) {
                // pop the stack
                String currentActor = actorStack.pop();
                // v = sigmaMap (popped element)
                double sigmaV = sigmaMap.get(currentActor); // should be 0
                // get predecessors
                List<String> WList = visitedAndBacktrack.get(currentActor);
                // for W in predecessors:
                for (String W: WList) {
                    double sigmaW = sigmaMap.get(W);
                    // Sigma(V) = Sigma(V)/Sigma(W) * (1+ Sigma(W))
                    double deltaSigmaV = sigmaV/(sigmaW) *(1+ sigmaW);
                    // dependencyMap.add(V, sigma(V))
                    dependencyMap.put(currentActor, dependencyMap.get(currentActor) + deltaSigmaV);
                }
            }
            //for every item in the dependency map, add the value to the betweenness map
            for (Map.Entry<String, Double> dependency: dependencyMap.entrySet()){
                String currentActor = dependency.getKey();
                Double currentBetweenness = betweennessMap.get(currentActor);
                currentBetweenness += dependency.getValue();
                betweennessMap.put(currentActor, currentBetweenness);

            }

        }
        // normalize betweenness centrality.
        // Cn = C/(.5*(n-1)*(n-2)) to obtain what % of paths run through the target node
        double bigNum = .5*9234*9233;
        return betweennessMap.get(target)/bigNum;
    }

    public AdjacencyMapGraph<String, Set<String>> denseSubGraph(AdjacencyMapGraph<String, Set<String>> ogGraph){
        //    set currentAverageDegree = averageDegree(G)
        AdjacencyMapGraph<String, Set<String>>graph = ogGraph.deepCopy();
        double currentAverageDegree = averageDegreeHelper(graph);
        //    while G has nodes:
        String flag = "true";
        while (flag.equals("true")) {
            //        remove the node with the smallest degree from G
            String smallest = "";
            int smallestVal = 1999999999;
            for (String vertex: graph.vertices()){
                if (graph.outDegree(vertex)<smallestVal){
                    smallestVal = graph.outDegree(vertex);
                    smallest = vertex;
                }
            }

            if (smallest.equals("")){
                flag = "false";
            }

            graph.removeVertex(smallest);

            //        if averageDegree(G) > currentAverageDegree:
            if (averageDegreeHelper(graph) > currentAverageDegree){
                // currentAverageDegree = averageDegree(G)
                currentAverageDegree=averageDegreeHelper(graph);
            } else {
                return graph;
            }
        }
        // return the best SubGraph (most dense)
        return graph;
    }

    public double averageDegreeHelper(AdjacencyMapGraph<String, Set<String>> graph){
        double totalDegrees = 0;
        double size = 0;
        for (String vertex: graph.vertices()){
            totalDegrees += graph.outDegree(vertex);
            size ++;
        }
        return totalDegrees/size;
    }

}


