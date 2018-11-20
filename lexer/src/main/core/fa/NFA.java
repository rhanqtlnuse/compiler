package main.core.fa;

import main.exception.FAException;
import main.util.InfixToPostfix;
import main.util.Operator;
import main.core.Symbol;

import java.util.*;

public class NFA {

    private Bag[] bags;
    private Set<String> alphabet = new HashSet<>();
    private Set<Integer> acceptStates = new HashSet<>();

    private NFA(String symbol) {
        this.bags = new Bag[2];
        for (int i = 0; i < bags.length; i++) {
            bags[i] = new Bag();
        }
        bags[0].add(new Transition(symbol, 1));
        this.acceptStates.add(1);
    }

    private NFA(Bag[] bags) {
        if (bags == null) {
            throw new NullPointerException();
        }
        if (bags.length < 2) {
            // TODO : 改为更准确的异常
            throw new NullPointerException();
        }
        this.bags = bags;
    }

    public NFA(List<String> regex) throws FAException {
        NFA res = constructNFA(InfixToPostfix.convert(regex));
        this.bags = res.bags;
        this.acceptStates = res.acceptStates;
    }

    private NFA constructNFA(List<String> postfixRegex) throws FAException {
        Stack<NFA> nfaStack = new Stack<>();
        for (String token : postfixRegex) {
            if (Operator.isOperator(token)) {
                try {
                    switch (token) {
                        case Operator.CONCAT: {
                            // op2
                            NFA nfa1 = nfaStack.pop();
                            // op1
                            NFA nfa2 = nfaStack.pop();
                            nfaStack.push(nfa2.concat(nfa1));
                            break;
                        }
                        case Operator.CLOSURE:
                            NFA nfa = nfaStack.pop();
                            nfaStack.push(nfa.closure());
                            break;
                        case Operator.ALTERNATE: {
                            // op2
                            NFA nfa1 = nfaStack.pop();
                            // op1
                            NFA nfa2 = nfaStack.pop();
                            nfaStack.push(nfa2.alternate(nfa1));
                            break;
                        }
                    }
                } catch (EmptyStackException ex) {
                    throw new FAException("Something Wrong In The Regular Expression");
                }
            } else {
                nfaStack.push(new NFA(token));
                if (!String.valueOf(Symbol.EPSILON).equals(token)) {
                    alphabet.add(token);
                }
            }
        }

        return nfaStack.pop();
    }

    DFA toDFA() {
        List<Bag> tran = new ArrayList<>();

        int nextId = 0;
        Map<Set<Integer>, Integer> newStateNumbers = new HashMap<>();
        Queue<Set<Integer>> untaggedStates = new LinkedList<>();
        Set<Integer> acceptStates = new HashSet<>();
        Set<Integer> closureOfStart = epsilonClosure(0);
        newStateNumbers.put(closureOfStart, nextId++);
        untaggedStates.add(closureOfStart);
        tran.add(new Bag());
        if (intersect(this.acceptStates, closureOfStart)) {
            acceptStates.add(0);
        }

        while (!untaggedStates.isEmpty()) {
            Set<Integer> t = untaggedStates.poll();
            for (String s : alphabet) {
                Set<Integer> toStates = move(t, s);
                if (!toStates.isEmpty()) {
                    Set<Integer> u = epsilonClosure(toStates);
                    if (!u.isEmpty()) {
                        int tid = newStateNumbers.get(t);
                        int uid;
                        if (!newStateNumbers.containsKey(u)) {
                            uid = nextId;
                            newStateNumbers.put(u, uid);
                            untaggedStates.offer(u);
                            tran.add(new Bag());
                            if (intersect(u, this.acceptStates)) {
                                acceptStates.add(uid);
                            }
                            nextId++;
                        } else {
                            uid = newStateNumbers.get(u);
                        }
                        tran.get(tid).add(new Transition(s, uid));
                    }
                }
            }
        }

        return new DFA(tran.toArray(new Bag[0]), acceptStates);
    }

    private boolean intersect(Set<Integer> one, Set<Integer> other) {
        Set<Integer> thisCopy = new HashSet<>(one);
        Set<Integer> thatCopy = new HashSet<>(other);
        thisCopy.retainAll(thatCopy);
        return !thisCopy.isEmpty();
    }

    private NFA concat(NFA that) {
        final int thisBagLength = this.bags.length;
        final int thatBagLength = that.bags.length;

        Bag[] bags = new Bag[thisBagLength + thatBagLength - 1];
        for (int i = 0; i < bags.length; i++) {
            bags[i] = new Bag();
        }

        for (int i = 0; i < thisBagLength - 1; i++) {
            bags[i] = this.bags[i];
        }
        for (int i = 0; i < thatBagLength; i++) {
            int j = i + thisBagLength - 1;
            Bag bag = that.bags[i];
            for (Transition t : bag) {
                Transition transition = new Transition(t.getSymbol(), t.getToState() + thisBagLength - 1);
                bags[j].add(transition);
            }
        }

        NFA nfa = new NFA(bags);
        nfa.acceptStates.add(thisBagLength + thatBagLength - 2);

        return nfa;
    }

    private NFA alternate(NFA that) {
        final int thisBagLength = this.bags.length;
        final int thatBagLength = that.bags.length;

        Bag[] bags = new Bag[thisBagLength + thatBagLength + 2];
        for (int i = 0; i < bags.length; i++) {
            bags[i] = new Bag();
        }

        bags[0].add(new Transition(String.valueOf(Symbol.EPSILON), 1));
        bags[0].add(new Transition(String.valueOf(Symbol.EPSILON), thisBagLength + 1));

        for (int i = 0; i < thisBagLength; i++) {
            Bag bag = this.bags[i];
            for (Transition t : bag) {
                Transition transition = new Transition(t.getSymbol(), t.getToState() + 1);
                bags[i + 1].add(transition);
            }
        }
        bags[thisBagLength].add(new Transition(String.valueOf(Symbol.EPSILON), thisBagLength + thatBagLength + 1));
        for (int i = 0; i < thatBagLength; i++) {
            Bag bag = that.bags[i];
            for (Transition t : bag) {
                Transition transition = new Transition(t.getSymbol(), t.getToState() + thisBagLength + 1);
                bags[i + thisBagLength + 1].add(transition);
            }
        }
        bags[thisBagLength + thatBagLength].add(new Transition(String.valueOf(Symbol.EPSILON), thisBagLength + thatBagLength + 1));

        NFA nfa = new NFA(bags);
        nfa.acceptStates.add(thisBagLength + thatBagLength + 1);

        return nfa;
    }

    private NFA closure() {
        final int thisBagLength = this.bags.length;

        Bag[] bags = new Bag[thisBagLength + 2];
        for (int i = 0; i < bags.length; i++) {
            bags[i] = new Bag();
        }

        bags[0].add(new Transition(String.valueOf(Symbol.EPSILON), 1));
        bags[0].add(new Transition(String.valueOf(Symbol.EPSILON), thisBagLength + 1));
        for (int i = 0; i < thisBagLength; i++) {
            Bag bag = this.bags[i];
            for (Transition t : bag) {
                Transition transition = new Transition(t.getSymbol(), t.getToState() + 1);
                bags[i + 1].add(transition);
            }
        }
        bags[thisBagLength].add(new Transition(String.valueOf(Symbol.EPSILON), 1));
        bags[thisBagLength].add(new Transition(String.valueOf(Symbol.EPSILON), thisBagLength + 1));

        NFA nfa = new NFA(bags);
        nfa.acceptStates.add(thisBagLength + 1);

        return nfa;
    }

    private Set<Integer> epsilonClosure(Set<Integer> states) {
        Set<Integer> nextStates = new HashSet<>();
        for (Integer state : states) {
            nextStates.addAll(epsilonClosure(state));
        }
        return nextStates;
    }

    private Set<Integer> epsilonClosure(int currentState) {
        Set<Integer> closure = new HashSet<>();
        closure.add(currentState);
        Queue<Integer> queue = new LinkedList<>();
        queue.offer(currentState);
        while (!queue.isEmpty()) {
            Integer state = queue.poll();
            Set<Integer> nextStates = move(state, String.valueOf(Symbol.EPSILON));
            for (Integer nextState : nextStates) {
                if (!closure.contains(nextState)) {
                    closure.add(nextState);
                    queue.offer(nextState);
                }
            }
        }
        return closure;
    }

    private Set<Integer> move(Set<Integer> states, String symbol) {
        Set<Integer> nextStates = new HashSet<>();
        for (Integer state : states) {
            nextStates.addAll(move(state, symbol));
        }
        return nextStates;
    }

    private Set<Integer> move(int state, String symbol) {
        Set<Integer> nextStates = new HashSet<>();
        for (Transition t : bags[state]) {
            if (t.getSymbol().equals(symbol)) {
                nextStates.add(t.getToState());
            }
        }
        return nextStates;
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
