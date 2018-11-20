package main.util;

import main.core.Production;
import main.core.Symbols;

import java.util.*;

public class DataPool {

    private Set<String> tokens;
    private Set<String> nonTerminals;
    private Set<String> symbols;

    private Set<String> nullableSymbols;

    private Map<Production, Integer> productionToId;
    private Map<Integer, Production> idToProduction;

    private Map<String, Set<Production>> headToProductions;

    public DataPool() {
        this.headToProductions = new HashMap<>();
        this.idToProduction = new HashMap<>();
        this.tokens = new HashSet<>();
        this.nonTerminals = new HashSet<>();
        this.symbols = new HashSet<>();
        this.productionToId = new HashMap<>();
        this.nullableSymbols = new HashSet<>();
        tokens.add("$");
        symbols.add("$");
    }

    public void findNullableSymbols() {
        Set<Production> productions = new HashSet<>();
        for (Map.Entry<String, Set<Production>> entry : headToProductions.entrySet()) {
            productions.addAll(entry.getValue());
        }

        Set<String> set = new HashSet<>();
        // 找到直接 epsilon 产生式
        for (Production p : productions) {
            String firstSymbol = p.getBody().get(0);
            if (firstSymbol.equals(Symbols.EPSILON)) {
                set.add(firstSymbol);
            }
        }
        Set<String> oldSet;
        do {
            oldSet = new HashSet<>(set);
            for (Production p : productions) {
                List<String> body = p.getBody();
                boolean epsilon = true;
                for (String s : body) {
                    epsilon = epsilon && set.contains(s);
                }
                if (epsilon) {
                    set.add(p.getHead());
                }
            }
        } while (!set.equals(oldSet));

        this.nullableSymbols = set;
    }

    public void addToken(String token) {
        tokens.add(token);
        symbols.add(token);
    }

    public void addNonTerminal(String nonTerminal) {
        nonTerminals.add(nonTerminal);
        symbols.add(nonTerminal);
    }

    public void addProduction(String head, Production production) {
        Set<Production> productions = headToProductions.getOrDefault(head, new HashSet<>());
        productions.add(production);
        headToProductions.put(head, productions);
        productionToId.put(production, production.getId());
        idToProduction.put(production.getId(), production);
    }

    public boolean tokenExists(String token) {
        return tokens.contains(token);
    }

    public Set<Production> productionsOf(String nonTerminal) {
        return headToProductions.getOrDefault(nonTerminal, new HashSet<>());
    }

    public Production getProductionById(int id) {
        return idToProduction.get(id);
    }

    public int getIdByProduction(Production p) {
        return productionToId.get(p);
    }

    public Set<String> getSymbols() {
        return symbols;
    }

    public boolean isNullable(String symbol) {
        return nullableSymbols.contains(symbol);
    }

    public Set<String> terminals() {
        return tokens;
    }

    public Set<String> nonTerminals() {
        return nonTerminals;
    }

    public Map<String, Set<Production>> getHeadToProductions() {
        return headToProductions;
    }

}
