package main.util;

public class IDGenerator {

    private int nextId;

    public IDGenerator() {
        this.nextId = 1;
    }

    public int assignID() {
        return nextId++;
    }

}
