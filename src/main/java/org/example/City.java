package org.example;

public class City {
    private String name;
    private int rank;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    @Override
    public String toString() {
        return "{name: " + name + ", rank: " + rank + "}";
    }
}