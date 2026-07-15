package models;

public class Airport {
    private int id;
    private String code;
    private String name;
    private String city;
    private String country;

    public Airport(int id, String code, String name, String city, String country) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.city = city;
        this.country = country;
    }

    public int getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    @Override
    public String toString() {
        return city + " (" + code + ")";
    }
}
