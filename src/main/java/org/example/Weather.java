package org.example;

public class Weather {
    private City city;
    private double temp;

    public Weather(City city, double temp) {
        this.city = city;
        this.temp = temp;
    }

    @Override
    public String toString() {
        return city.getName() + " : " + temp;
    }
}
