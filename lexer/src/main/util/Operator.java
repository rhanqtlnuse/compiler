package main.util;

public class Operator {

    public static final String CLOSURE = "*";
    public static final String ALTERNATE = "|";
    public static final String CONCAT = "#";

    public static boolean isOperator(String operator) {
        return Operator.CLOSURE.equals(operator) || Operator.ALTERNATE.equals(operator) || Operator.CONCAT.equals(operator);
    }

    static int priorityOf(String operator) {
        switch (operator) {
            case "(":
                return 0;
            case Operator.CONCAT:
                return 1;
            case Operator.ALTERNATE:
                return 2;
            case Operator.CLOSURE:
                return 3;
            default:
                return -1;
        }
    }
}
