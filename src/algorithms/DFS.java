package algorithms;

import models.Flight;

import java.util.*;

public class DFS {

    public static class DFSResult {
        public List<String> visitOrder;
        public List<String> logs;

        public DFSResult() {
            this.visitOrder = new ArrayList<>();
            this.logs = new ArrayList<>();
        }
    }

    public static DFSResult traverse(Graph graph, String startAirport) {
        DFSResult result = new DFSResult();
        result.logs.add("Starting DFS Traversal from: " + startAirport);

        if (graph.getAirport(startAirport) == null) {
            result.logs.add("Airport " + startAirport + " not found.");
            return result;
        }

        Set<String> visited = new HashSet<>();
        dfsHelper(graph, startAirport, visited, result);

        result.logs.add("DFS Traversal complete. Total visited airports: " + result.visitOrder.size());
        return result;
    }

    private static void dfsHelper(Graph graph, String u, Set<String> visited, DFSResult result) {
        visited.add(u);
        result.visitOrder.add(u);
        result.logs.add("Visited node: " + u);

        for (Flight flight : graph.getFlightsFrom(u)) {
            if ("Cancelled".equalsIgnoreCase(flight.getStatus())) {
                continue;
            }
            String neighbor = flight.getDestination();
            if (!visited.contains(neighbor)) {
                result.logs.add("  Stepping from " + u + " -> " + neighbor);
                dfsHelper(graph, neighbor, visited, result);
            }
        }
    }
}
