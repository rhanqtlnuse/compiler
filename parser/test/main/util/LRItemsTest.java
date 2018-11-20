package main.util;

import main.core.LRItems;
import main.core.Production;
import main.core.Symbols;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class LRItemsTest {

    @Test
    public void testFirstNormal() {
        DataPool pool = new DataPool();

        /*
         * E -> T E'
         * E'-> + T E'
         * E'-> ε
         * T -> F T'
         * T'-> * F T'
         * T'-> ε
         * F -> ( E )
         * F -> a
         */
        Production p1 = new Production(1, "E", Arrays.asList("T", "E_prime"));
        Production p2 = new Production(2, "E_prime", Arrays.asList("PLUS", "T", "E_prime"));
        Production p3 = new Production(3, "E_prime", Collections.singletonList("epsilon"));
        Production p4 = new Production(4, "T", Arrays.asList("F", "T_prime"));
        Production p5 = new Production(5, "T_prime", Arrays.asList("MULT", "F", "T_prime"));
        Production p6 = new Production(6, "T_prime", Collections.singletonList("epsilon"));
        Production p7 = new Production(7, "F", Arrays.asList("LPAREN", "E", "RPAREN"));
        Production p8 = new Production(8, "F", Collections.singletonList("a"));

        pool.addToken("PLUS");
        pool.addToken("MULT");
        pool.addToken("LPAREN");
        pool.addToken("RPAREN");
        pool.addToken("a");

        pool.addNonTerminal("E");
        pool.addNonTerminal("E_prime");
        pool.addNonTerminal("T");
        pool.addNonTerminal("T_prime");
        pool.addNonTerminal("F");

        pool.addProduction("E", p1);
        pool.addProduction("E_prime", p2);
        pool.addProduction("E_prime", p3);
        pool.addProduction("T", p4);
        pool.addProduction("T_prime", p5);
        pool.addProduction("T_prime", p6);
        pool.addProduction("F", p7);
        pool.addProduction("F", p8);

        pool.findNullableSymbols();

        Set<String> res1 = LRItems.first("E", pool, new HashMap<>());
        System.out.println(res1);
        Assert.assertEquals(new HashSet<>(Arrays.asList("a", "LPAREN")), res1);

        Set<String> res2 = LRItems.first("E_prime", pool, new HashMap<>());
        System.out.println(res2);
        Assert.assertEquals(new HashSet<>(Arrays.asList("PLUS", Symbols.EPSILON)), res2);

        Set<String> res3 = LRItems.first("T", pool, new HashMap<>());
        System.out.println(res3);
        Assert.assertEquals(new HashSet<>(Arrays.asList("a", "LPAREN")), res3);

        Set<String> res4 = LRItems.first("T_prime", pool, new HashMap<>());
        System.out.println(res4);
        Assert.assertEquals(new HashSet<>(Arrays.asList("MULT", Symbols.EPSILON)), res4);

        Set<String> res5 = LRItems.first("F", pool, new HashMap<>());
        System.out.println(res5);
        Assert.assertEquals(new HashSet<>(Arrays.asList("a", "LPAREN")), res5);
    }

    @Test
    public void testLeftRecursion() {
        DataPool pool = new DataPool();

        /*
         * A -> AB | C | c
         * B -> b
         * C -> D | d
         * D -> A | a | ε
         */
        Production p1 = new Production(1, "A", Arrays.asList("A", "B"));
        Production p2 = new Production(2, "A", Collections.singletonList("C"));
        Production p3 = new Production(3, "A", Collections.singletonList("c"));
        Production p4 = new Production(4, "B", Collections.singletonList("b"));
        Production p5 = new Production(5, "C", Collections.singletonList("D"));
        Production p6 = new Production(6, "C", Collections.singletonList("d"));
        Production p7 = new Production(7, "D", Collections.singletonList("A"));
        Production p8 = new Production(8, "D", Collections.singletonList("a"));
        Production p9 = new Production(9, "D", Collections.singletonList(Symbols.EPSILON));

        pool.addToken("a");
        pool.addToken("b");
        pool.addToken("c");
        pool.addToken("d");

        pool.addNonTerminal("A");
        pool.addNonTerminal("B");
        pool.addNonTerminal("C");
        pool.addNonTerminal("D");

        pool.addProduction("A", p1);
        pool.addProduction("A", p2);
        pool.addProduction("A", p3);
        pool.addProduction("B", p4);
        pool.addProduction("C", p5);
        pool.addProduction("C", p6);
        pool.addProduction("D", p7);
        pool.addProduction("D", p8);
        pool.addProduction("D", p9);

        pool.findNullableSymbols();

        Set<String> res1 = LRItems.first("A", pool, new HashMap<>());
        System.out.println(res1);
        Assert.assertEquals(new HashSet<>(Arrays.asList("epsilon", "a", "b", "c", "d")), res1);

        Set<String> res2 = LRItems.first("B", pool, new HashMap<>());
        System.out.println(res2);
        Assert.assertEquals(new HashSet<>(Collections.singletonList("b")), res2);

        Set<String> res3 = LRItems.first("C", pool, new HashMap<>());
        System.out.println(res3);
        Assert.assertEquals(new HashSet<>(Arrays.asList("epsilon", "a", "b", "c", "d")), res3);

        Set<String> res4 = LRItems.first("D", pool, new HashMap<>());
        System.out.println(res4);
        Assert.assertEquals(new HashSet<>(Arrays.asList("epsilon", "a", "b", "c", "d")), res4);
    }

    @Test
    public void testFirstList() {
        DataPool pool = new DataPool();

        /*
         * A -> AB | C | c
         * B -> b
         * C -> D | d
         * D -> A | a | ε
         */
        Production p1 = new Production(1, "A", Arrays.asList("A", "B"));
        Production p2 = new Production(2, "A", Collections.singletonList("C"));
        Production p3 = new Production(3, "A", Collections.singletonList("c"));
        Production p4 = new Production(4, "B", Collections.singletonList("b"));
        Production p5 = new Production(5, "C", Collections.singletonList("D"));
        Production p6 = new Production(6, "C", Collections.singletonList("d"));
        Production p7 = new Production(7, "D", Collections.singletonList("A"));
        Production p8 = new Production(8, "D", Collections.singletonList("a"));
        Production p9 = new Production(9, "D", Collections.singletonList(Symbols.EPSILON));

        pool.addToken("a");
        pool.addToken("b");
        pool.addToken("c");
        pool.addToken("d");

        pool.addNonTerminal("A");
        pool.addNonTerminal("B");
        pool.addNonTerminal("C");
        pool.addNonTerminal("D");

        pool.addProduction("A", p1);
        pool.addProduction("A", p2);
        pool.addProduction("A", p3);
        pool.addProduction("B", p4);
        pool.addProduction("C", p5);
        pool.addProduction("C", p6);
        pool.addProduction("D", p7);
        pool.addProduction("D", p8);
        pool.addProduction("D", p9);

        pool.findNullableSymbols();

        Set<String> res = LRItems.first(Arrays.asList("A", "B"), pool);
        System.out.println(res);

        Set<String> res2 = LRItems.first(Arrays.asList("B", "A"), pool);
        System.out.println(res2);
    }

    @Test
    public void testFirstList2() {
        DataPool pool = new DataPool();

        Production p1 = new Production(0, "S'", Collections.singletonList("S"));
        Production p2 = new Production(1, "S", Arrays.asList("C", "C"));
        Production p3 = new Production(2, "C", Arrays.asList("c", "C"));
        Production p4 = new Production(3, "C", Collections.singletonList("d"));

        pool.addToken("c");
        pool.addToken("d");

        pool.addNonTerminal("S");
        pool.addNonTerminal("C");

        pool.addProduction(p1.getHead(), p1);
        pool.addProduction(p2.getHead(), p2);
        pool.addProduction(p3.getHead(), p3);
        pool.addProduction(p4.getHead(), p4);

        pool.findNullableSymbols();

        Set<String> res = LRItems.first(Collections.singletonList("$"), pool);
        System.out.println(res);
    }
}