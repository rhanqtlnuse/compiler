package main.fa;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

class Bag implements Iterable<Transition> {

    private Set<Transition> bag;

    Bag() {
        this.bag = new HashSet<>();
    }

    boolean add(Transition e) {
        return bag.add(e);
    }

    boolean contains(Transition e) {
        return bag.contains(e);
    }

    @Override
    public Iterator<Transition> iterator() {
        return bag.iterator();
    }

    @Override
    public String toString() {
        return bag.toString();
    }
}
