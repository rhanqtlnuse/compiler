package main.core;

import main.util.DataPool;

import java.util.*;

public class LRItems {

    /**
     * 移点，构建新的 LR 项集时使用
     *
     * @param item 当前 LR 项集中的一项
     * @param s    符号
     * @return 新的 LR 项
     * 如果当前 LR 项是可规约项或者点后的符号不是 s，返回 null
     */
    static LRItem shiftDot(LRItem item, String s) {
        if (!item.isReducible()) {
            String nextSymbol = item.nextSymbol();
            if (nextSymbol.equals(s)) {
                return new LRItem(item.getProduction(),
                        item.getPredictiveSymbol(), item.getDotPosition() + 1);
            }
        }
        return null;
    }

    public static Set<String> first(String symbol, DataPool pool, Map<String, Set<String>> map) {
        if (pool.tokenExists(symbol)) {
            return Collections.singleton(symbol);
        } else if (symbol.equals(Symbols.EPSILON)) {
            return Collections.singleton(Symbols.EPSILON);
        } else {
            Set<String> res = new HashSet<>();
            for (Production p : pool.productionsOf(symbol)) {
                List<String> body = p.getBody();
                int i = 0;
                for (; i < body.size(); i++) {
                    String s = body.get(i);
                    if (s.equals(p.getHead())) {
                        map.put(s, new HashSet<>());
                        if (!pool.isNullable(s)) {
                            break;
                        }
                    } else {
                        Set<String> set;
                        if (map.containsKey(s)) {
                            set = map.get(s);
                        } else {
                            set = first(s, pool, map);
                            map.put(s, set);
                        }
                        for (String x : set) {
                            if (!x.equals(Symbols.EPSILON)) {
                                res.add(x);
                            }
                        }
                        if (!pool.isNullable(s)) {
                            break;
                        }
                    }
                }
                if (i == body.size()) {
                    res.add(Symbols.EPSILON);
                }
            }
            return res;
        }
    }

    public static Set<String> first(List<String> symbolString, DataPool pool) {
        if (pool.tokenExists(symbolString.get(0))) {
            return Collections.singleton(symbolString.get(0));
        } else if (symbolString.get(0).equals(Symbols.EPSILON)) {
            return Collections.singleton(Symbols.EPSILON);
        } else {
            int i = 0;
            Set<String> res = new HashSet<>();
            Set<String> subset;
            do {
                subset = first(symbolString.get(i), pool, new HashMap<>());
                res.addAll(subset);
                i++;
            } while (i < symbolString.size() && subset.contains(Symbols.EPSILON));
            return res;
        }
    }
}
