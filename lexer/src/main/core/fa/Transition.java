package main.core.fa;

class Transition {

    private String symbol;
    private int toState;

    Transition(String symbol, int toState) {
        this.symbol = symbol;
        this.toState = toState;
    }

    String getSymbol() {
        return symbol;
    }

    void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    int getToState() {
        return toState;
    }

    @Override
    public String toString() {
        return "('" + symbol + "', " + toState + ")";
    }
}
