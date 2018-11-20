package main.fa;

import main.exception.FAException;
import main.util.Regex;

import java.util.Set;

public class DFA {

    private static int nextId = 0;

    private Bag[] bags;
    private Set<Integer> acceptStates;
    private String name;
    private int innerCode;

    DFA(Bag[] bags, Set<Integer> acceptStates) {
        this.bags = bags;
        this.acceptStates = acceptStates;
    }

    public DFA(Regex regex) throws FAException {
        NFA nfa = new NFA(regex.getRegex());
        DFA dfa = nfa.toDFA();
        this.bags = dfa.bags;
        this.acceptStates = dfa.acceptStates;
        this.name = regex.getName();
        this.innerCode = nextId++;
    }

    private void optimize() {
        // TODO
    }

    /**
     * 生成的 DFA 的边上的标记有：
     * 1. 字母表中字符（字母/数字/下划线）
     * 2. 任意字符 '.'
     * 3. 范围 [<c1>-<c2>]
     * 4. "" 中的文字，其中可能有转义字符
     * 5. 转义字符
     */
    public int move(int state, char ch) {
        assert state >= 0 : "State Mustn't Be Less Than Zero";

        for (Transition t : bags[state]) {
            String label = t.getSymbol();
            if (".".equals(label)) {
                return t.getToState();
            } else if (label.startsWith("[")) {
                char lowerBound = label.charAt(1);
                char upperBound = label.charAt(3);
                if (lowerBound <= ch && ch <= upperBound) {
                    return t.getToState();
                }
            } else if (label.startsWith("\"")) {
                char[] chs = label.toCharArray();
                if (chs[1] == '\\') {
                    if (ch == chs[2]) {
                        return t.getToState();
                    }
                } else {
                    if (ch == chs[1]) {
                        return t.getToState();
                    }
                }
            } else if (label.startsWith("\\")) {
                char c = label.charAt(1);
                if (ch == c) {
                    return t.getToState();
                }
            } else if (label.length() == 1) {
                char c = label.charAt(0);
                if (ch == c) {
                    return t.getToState();
                }
            } else {
                System.err.println("Wrong Character: " + ch);
                return -1;
            }
        }
        return -1;
    }

    public boolean isAcceptState(int state) {
        return acceptStates.contains(state);
    }

    public String getName() {
        return name;
    }

    public int getInnerCode() {
        return innerCode;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{").append(System.lineSeparator());
        sb.append("  [").append(System.lineSeparator());
        for (int i = 0; i < bags.length; i++) {
            sb.append("    ").append(i).append(": ").append(bags[i]);
            if (i != bags.length - 1) {
                sb.append(",");
            }
            sb.append(System.lineSeparator());
        }
        sb.append("  ], ").append(System.lineSeparator());
        sb.append("  accept states: ").append(acceptStates).append(System.lineSeparator());
        sb.append("}");
        return sb.toString();
    }
}
