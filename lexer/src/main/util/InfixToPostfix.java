package main.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class InfixToPostfix {

    public static List<String> convert(List<String> infixExpr) {
        List<String> postfixExpr = new LinkedList<>();

        Stack<String> stack = new Stack<>();
        for (String token : infixExpr) {
            if ("(".equals(token)) {
                stack.push(token);
            } else if (")".equals(token)) {
                while (!stack.empty() && !stack.peek().equals("(")) {
                    postfixExpr.add(stack.pop());
                }
                stack.pop();
            } else if (Operator.isOperator(token)) {
                int priorityOfToken = Operator.priorityOf(token);
                while (!stack.empty() && Operator.priorityOf(stack.peek()) >= priorityOfToken) {
                    postfixExpr.add(stack.pop());
                }
                stack.push(token);
            } else {
                postfixExpr.add(token);
            }
        }
        while (!stack.empty()) {
            postfixExpr.add(stack.pop());
        }

        return postfixExpr;
    }
}
