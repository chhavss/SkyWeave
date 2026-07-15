package algorithms;

import models.Airport;
import models.Flight;

import java.util.*;

public class Dijkstra {

    public static class RouteResult {
        public List<Flight> path;
        public int totalDistance;
        public int totalCost;
        public int totalDuration;
        public int stops;
        public boolean found;
        public List<String> logs;

        public RouteResult() {
            this.path = new ArrayList<>();
            this.totalDistance = 0;
            this.totalCost = 0;
            this.totalDuration = 0;
            this.stops = 0;
            this.found = false;
            this.logs = new ArrayList<>();
        }
    }

    private static class Node implements Comparable<Node> {
        String airportCode;
        int weight;

        Node(String airportCode, int weight) {
            this.airportCode = airportCode;
            this.weight = weight;
        }

        @Override
        public int compareTo(Node other) {
            return Integer.compare(this.weight, other.weight);
        }
    }

    public static RouteResult findShortestPath(Graph graph, String source, String destination, String optimizationType) {
        RouteResult result = new RouteResult();
        result.logs.add("Initializing Dijkstra's Algorithm...");
        result.logs.add("Optimizing by: " + optimizationType);
        result.logs.add("Searching path from " + source + " to " + destination);

        if (graph.getAirport(source) == null || graph.getAirport(destination) == null) {
            result.logs.add("Error: Source or Destination airport does not exist in the routing network.");
            result.found = false;
            return result;
        }

        if (source.equals(destination)) {
            result.logs.add("Source and Destination are the same.");
            result.found = true;
            return result;
        }

        Map<String, Integer> distances = new HashMap<>();
        Map<String, Flight> edgeTo = new HashMap<>();
        PriorityQueue<Node> pq = new PriorityQueue<>();

        for (Airport airport : graph.getAllAirports()) {
            distances.put(airport.getCode(), Integer.MAX_VALUE);
        }
        distances.put(source, 0);
        pq.add(new Node(source, 0));

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            String u = current.airportCode;

            if (u.equals(destination)) {
                result.logs.add("Destination " + destination + " reached with optimal weight.");
                break;
            }

            if (current.weight > distances.get(u)) {
                continue;
            }

            result.logs.add("Relaxing edges for airport: " + u);
            for (Flight flight : graph.getFlightsFrom(u)) {
                if ("Cancelled".equalsIgnoreCase(flight.getStatus())) {
                    continue;
                }

                String v = flight.getDestination();
                int weight = flight.getWeight(optimizationType);
                int newDist = distances.get(u) + weight;

                if (newDist < distances.get(v)) {
                    distances.put(v, newDist);
                    edgeTo.put(v, flight);
                    pq.add(new Node(v, newDist));
                    result.logs.add("  Found shorter path to " + v + " via flight " + flight.getFlightNumber() + " (New weight: " + newDist + ")");
                }
            }
        }

        if (distances.get(destination) == Integer.MAX_VALUE) {
            result.logs.add("No path found between " + source + " and " + destination);
            result.found = false;
            return result;
        }

        List<Flight> pathList = new ArrayList<>();
        String currentCode = destination;
        while (edgeTo.containsKey(currentCode)) {
            Flight flight = edgeTo.get(currentCode);
            pathList.add(flight);
            currentCode = flight.getSource();
        }
        Collections.reverse(pathList);

        result.path = pathList;
        result.found = true;
        result.stops = pathList.size() - 1;

        for (Flight flight : pathList) {
            result.totalDistance += flight.getDistance();
            result.totalCost += flight.getCost();
            result.totalDuration += flight.getDuration();
        }

        result.logs.add("Path successfully computed. Total Stops: " + result.stops);
        return result;
    }
}
