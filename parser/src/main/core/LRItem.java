package main.core;

import com.sun.istack.internal.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class LRItem {

    private final Production production;
    private final String predictiveSymbol;
    private final int dot;
    private boolean reducible;

    LRItem(@NotNull Production production, @NotNull String predictiveSymbol) {
        this(production, predictiveSymbol, 0);
    }

    LRItem(@NotNull Production production, @NotNull String predictiveSymbol, int dot) {
        this.production = production;
        this.predictiveSymbol = predictiveSymbol;
        this.dot = dot;
        if (dot == production.getBody().size()) {
            this.reducible = true;
        } else if (0 <= dot && dot < production.getBody().size()) {
            this.reducible = false;
        } else {
            throw new IllegalArgumentException("!!! invalid dot value !!!");
        }
    }

    Production getProduction() {
        return production;
    }

    String getPredictiveSymbol() {
        return predictiveSymbol;
    }

    int getDotPosition() {
        return dot;
    }

    boolean isReducible() {
        return reducible;
    }

    String nextSymbol() {
        return production.symbolAt(dot);
    }

    List<String> fromBeta() {
        if (dot < production.getBody().size() - 1) {
            List<String> retList = new ArrayList<>(production.getBody().subList(dot + 1, production.getBody().size()));
            retList.add(predictiveSymbol);
            return retList;
        } else {
            return Collections.singletonList(predictiveSymbol);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LRItem lrItem = (LRItem) o;
        return dot == lrItem.dot &&
                reducible == lrItem.reducible &&
                Objects.equals(production, lrItem.production) &&
                Objects.equals(predictiveSymbol, lrItem.predictiveSymbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(production, predictiveSymbol, dot, reducible);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(production.getHead()).append(" ->");
        for (int i = 0; i < production.getBody().size(); i++) {
            if (i == dot) {
                sb.append(" ·");
            }
            sb.append(" ").append(production.getBody().get(i));
        }
        return sb.toString();
    }

}
