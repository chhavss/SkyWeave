package algorithms;

import models.Airport;
import models.Flight;

import java.util.*;

public class Graph {
    private final Map<String, Airport> airports;
    private final Map<String, List<Flight>> adjacencyList;

    public Graph() {
        this.airports = new HashMap<>();
        this.adjacencyList = new HashMap<>();
    }

    public void addAirport(Airport airport) {
        airports.put(airport.getCode(), airport);
        adjacencyList.putIfAbsent(airport.getCode(), new ArrayList<>());
    }

    public void addFlight(Flight flight) {
        if (adjacencyList.containsKey(flight.getSource())) {
            adjacencyList.get(flight.getSource()).add(flight);
        }
    }

    public Airport getAirport(String code) {
        return airports.get(code);
    }

    public Collection<Airport> getAllAirports() {
        return airports.values();
    }

    public List<Flight> getFlightsFrom(String code) {
        return adjacencyList.getOrDefault(code, new ArrayList<>());
    }

    public Map<String, List<Flight>> getAdjacencyList() {
        return adjacencyList;
    }
}
