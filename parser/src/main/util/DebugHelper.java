package main.util;

import main.core.Production;
import main.core.action.Action;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class DebugHelper {

    public static void printProductions(Map<String, Set<Production>> map) {
        System.out.println("productions: ");
        for (Map.Entry<String, Set<Production>> entry : map.entrySet()) {
            for (Production p : entry.getValue()) {
                System.out.println("  " + p);
            }
        }
        System.out.println();
    }

    public static void printParseTable(Set<String> terminals, Set<String> nonTerminals, List<Map<String, Action>> parseTable) {
        final int stateOrder = (int) Math.floor(Math.log10(parseTable.size())) + 1;
        final int minWidth = stateOrder + 2;
        final String indent = "";

        System.out.println("PARSE TABLE: ");
        System.out.println();
        System.out.print(indent);
        for (int i = 0; i < stateOrder; i++) {
            System.out.print(" ");
        }

        for (String terminal : terminals) {
            if (!terminal.equals("$")) {
                System.out.printf("%" + Math.max(terminal.length() + 1, minWidth) + "s", terminal);
            }
        }
        for (int i = 0; i < stateOrder + 1; i++) {
            System.out.print(" ");
        }
        System.out.print("$");

        for (String nonTerminal : nonTerminals) {
            System.out.printf("%" + Math.max(nonTerminal.length() + 1, minWidth) + "s", nonTerminal);
        }
        System.out.println();

        for (int i = 0; i < parseTable.size(); i++) {
            System.out.printf(indent + "%" + stateOrder + "d", i);
            Map<String, Action> actionMap = parseTable.get(i);
            for (String terminal : terminals) {
                if (!terminal.equals("$")) {
                    Action action = actionMap.get(terminal);
                    if (action != null) {
                        System.out.printf("%" + Math.max(terminal.length() + 1, minWidth) + "s", action);
                    } else {
                        for (int j = 0; j < Math.max(terminal.length() + 1, minWidth); j++) {
                            System.out.print(" ");
                        }
                    }
                }
            }
            Action action = actionMap.get("$");
            if (action != null) {
                System.out.printf("%" + (stateOrder + 2) + "s", action);
            } else {
                for (int j = 0; j < stateOrder + 2; j++) {
                    System.out.print(" ");
                }
            }
            for (String nonTerminal : nonTerminals) {
                Action action2 = actionMap.get(nonTerminal);
                if (action2 != null) {
                    System.out.printf("%" + Math.max(nonTerminal.length() + 1, minWidth) + "s", action2.toString().substring(1));
                } else {
                    for (int j = 0; j < Math.max(nonTerminal.length() + 1, minWidth); j++) {
                        System.out.print(" ");
                    }
                }
            }
            System.out.println();
        }
    }
}
