package main.core.action;

public class ReduceAction implements Action {

    private int productionId;

    public ReduceAction(int productionId) {
        this.productionId = productionId;
    }

    public int getProductionId() {
        return productionId;
    }

    @Override
    public String toString() {
        return "r" + productionId;
    }
}
