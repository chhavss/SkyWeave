package models;

public class Flight {
    private int id;
    private String flightNumber;
    private String airline;
    private String source;
    private String destination;
    private int distance;
    private int duration;
    private int cost;
    private String status;

    public Flight(int id, String flightNumber, String airline, String source, String destination, 
                  int distance, int duration, int cost, String status) {
        this.id = id;
        this.flightNumber = flightNumber;
        this.airline = airline;
        this.source = source;
        this.destination = destination;
        this.distance = distance;
        this.duration = duration;
        this.cost = cost;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public String getAirline() {
        return airline;
    }

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public int getDistance() {
        return distance;
    }

    public int getDuration() {
        return duration;
    }

    public int getCost() {
        return cost;
    }

    public String getStatus() {
        return status;
    }

    public int getWeight(String optimizationType) {
        switch (optimizationType.toLowerCase()) {
            case "cost":
            case "lowest cost":
            case "price":
                return cost;
            case "duration":
            case "fastest route":
            case "time":
                return duration;
            case "distance":
            case "shortest distance":
            default:
                return distance;
        }
    }
}
