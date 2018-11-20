package main.core.action;

public class ShiftAction implements Action {

    private int nextState;

    public ShiftAction(int nextState) {
        this.nextState = nextState;
    }

    public int getNextState() {
        return nextState;
    }

    @Override
    public String toString() {
        return "s" + nextState;
    }
}
