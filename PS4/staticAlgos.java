import java.util.*;

public class staticAlgos {

    /**
     * create a tree using bfs that links adjacent neighbors of children, starting from the root of the bfs
     * @param graph
     * @param source
     * @param PathOrSep
     * @return
     * @param <V>
     * @param <E>
     * @throws Exception
     */
    public static <V, E> AdjacencyMapGraph<V, E> bfs(AdjacencyMapGraph<V, E> graph, V source, String PathOrSep)
            throws Exception{

        // create a new Graph to return
        // create a queue of "to Visit" and a map of visited actors. The map will contain keys of visited
        // and ALSO map the visited vertices back to their predecessor.
        AdjacencyMapGraph<V, E> bfs = new AdjacencyMapGraph<>();
        SLLQueue<V> toVisit = new SLLQueue<>();
        Map<V, V> visitedAndBacktrack = new HashMap<>();

        //enqueue the start, then add the start to "visited"
        bfs.insertVertex(source);
        toVisit.enqueue(source);
        visitedAndBacktrack.put(source, null); // Start actor has no predecessor

        // while there are still elements in the queue (meaning there are breadths not searched yet!)
        while (!toVisit.isEmpty()) {
            // currentActor is the front of the queue
            V currentActor = toVisit.dequeue();
            if (!graph.hasVertex(currentActor) || graph.out.get(currentActor) == null) {
                throw new IllegalStateException("Vertex " + currentActor + " does not exist in the graph or has a null adjacency map");
            }

            // Queue up all the connected actors. For each neighbor in the set of outNeighbors of
            // the current vertex, if the neighbor isn't in the "visited" map, put it in the visited map (since
            // it's been seen and will eventually be visited) and add it to toVisit

            for (V neighbor : graph.outNeighbors(currentActor)) {
                if (!visitedAndBacktrack.containsKey(neighbor)) {
                    visitedAndBacktrack.put(neighbor, currentActor);
                    bfs.insertVertex(neighbor);
                    if (PathOrSep.equals("Path")) {
                        // insert a directed edge pointing to the parent
                        bfs.insertDirected(neighbor, currentActor, graph.getLabel(currentActor, neighbor));
                    }
                    if (PathOrSep.equals("Sep")){
                        //insert a directed edge pointing to the child
                        bfs.insertDirected(currentActor, neighbor, graph.getLabel(currentActor, neighbor));
                    }
                    toVisit.enqueue(neighbor);
                }
            }
        }
        return bfs;
    }

    /**
     * return the shortest path using the bfs tree to the goal vertex
     * @param tree
     * @param goal
     * @return
     * @param <V>
     * @param <E>
     */
    public static <V,E>List<V> getPath(AdjacencyMapGraph<V,E> tree, V goal){
        if (!tree.hasVertex(goal)){
            System.out.println("goal not in path");
            return new ArrayList<>();
        }
        // create a new path
        ArrayList<V> path = new ArrayList<>();
        // assign goal to a tracker
        // assign parent to a tracker
        V currentActor = goal;
        V child = null;
        int j = 0;
        // while the goal is not equal to the child
        while (currentActor != child && j < 10){
            System.out.println(j);
            j++;
            // add the tracker to the path.
//            System.out.println("Current Actor:"+ currentActor);
            path.add(currentActor);
            //
            V next = null;
            for (V i: tree.outNeighbors(currentActor)) {
//                System.out.println("step 1 executed");
                next = i;
//                System.out.println("next:" + next);
            }
            if (next != null){
//                System.out.println("step 2 executed");
                child = currentActor;
                currentActor = next;
            } else {
                child = currentActor;
            }
 //           System.out.println("Child: " + child);
        }
        return path;
    }

    /**
     * come up with a set of the missing vertices that are in the graph parameter but not in the subgraph parameter
     * @param graph
     * @param subgraph
     * @return
     * @param <V>
     * @param <E>
     */
    public static <V,E> Set<V> missingVertices(Graph<V,E> graph, Graph<V,E> subgraph){
        HashSet<V> returnSet = new HashSet<>();
        for (V vertex: graph.vertices()){
            if (!subgraph.hasVertex(vertex)){
                returnSet.add(vertex);
            }
        }
        return returnSet;
    }

    public static ArrayList<Double> depths = new ArrayList<>();

    /**
     * find the average separation of the root vertex to all available leaf nodes
     * @param graph
     * @param root
     * @return
     * @param <V>
     * @param <E>
     * @throws Exception
     */
    public static <V,E> double averageSeparation(AdjacencyMapGraph<V, E> graph, V root) throws Exception {
        depths.clear();
        AdjacencyMapGraph<V,E> tree = bfs(graph, root, "Sep");
        averageSeparationHelper(tree, null, root, 0);
        double total = sum(depths);
        double length = depths.size();
        return total/length;

    }

    /**
     * sum of the elements of a list (elements must be doubles)
     * @param array
     * @return
     */
    public static double sum(ArrayList<Double> array) {
        double total = 0;
        for (double number : array) {
            total += number;
        }
        return total;
    }

    /**
     * helper method for averageSeparation function. Recursively traverses bfs tree and calculates the depth of each
     * leaf, then adds all those depths to a static list "depths"
     * @param graph
     * @param parent
     * @param vertex
     * @param depth
     * @param <V>
     * @param <E>
     */
    public static <V, E> void averageSeparationHelper(Graph<V,E> graph, V parent, V vertex, double depth){
        // do post-order traversal:
        // if it is a leaf, add the depth to the list.
        ArrayList<V> children = new ArrayList<>();

        for (V neighbor: graph.outNeighbors(vertex)){
            if (!neighbor.equals(parent)){
                children.add(neighbor);
            }
        }

        if (children.isEmpty()){
            depths.add(depth);
        }

        for (V child: children ){
            averageSeparationHelper(graph, vertex, child, depth+1);

        }
        //Perform a DFS from the root, keeping track of the current depth.
        //Each time you reach a leaf (a node with no children), record the depth.
        //After the DFS is complete, calculate the average of all recorded depths.
    }

}
