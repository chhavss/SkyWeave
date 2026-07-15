package algorithms;

import models.Flight;

import java.util.*;

public class BFS {

    public static class BFSResult {
        public List<String> visitOrder;
        public Map<String, Integer> hops;
        public List<String> logs;

        public BFSResult() {
            this.visitOrder = new ArrayList<>();
            this.hops = new HashMap<>();
            this.logs = new ArrayList<>();
        }
    }

    public static BFSResult traverse(Graph graph, String startAirport) {
        BFSResult result = new BFSResult();
        result.logs.add("Starting BFS Traversal from: " + startAirport);

        if (graph.getAirport(startAirport) == null) {
            result.logs.add("Airport " + startAirport + " not found.");
            return result;
        }

        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();

        queue.add(startAirport);
        visited.add(startAirport);
        result.hops.put(startAirport, 0);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            result.visitOrder.add(current);
            int currentHops = result.hops.get(current);
            result.logs.add("De-queued airport: " + current + " (Hops: " + currentHops + ")");

            for (Flight flight : graph.getFlightsFrom(current)) {
                if ("Cancelled".equalsIgnoreCase(flight.getStatus())) {
                    continue;
                }
                String neighbor = flight.getDestination();
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    result.hops.put(neighbor, currentHops + 1);
                    queue.add(neighbor);
                    result.logs.add("  Discovered neighbor: " + neighbor + " (Queued at Hops: " + (currentHops + 1) + ")");
                }
            }
        }

        result.logs.add("BFS Traversal complete. Total visited airports: " + result.visitOrder.size());
        return result;
    }
}
