package test;

import main.exception.FAException;
import main.exception.InvalidFormatException;
import main.exception.RegexException;
import main.core.fa.DFA;
import main.core.fa.NFA;
import main.util.InfixToPostfix;
import main.core.Regex;
import org.junit.Test;

public class DFATest {

    @Test
    public void constructor() throws RegexException, InvalidFormatException, FAException {
        String line = "test (a|b)*abb";
        Regex regex = Regex.parse(line);
        System.out.println(regex);
        System.out.println();
        System.out.println(InfixToPostfix.convert(regex.getRegex()));
        System.out.println();
        NFA nfa = new NFA(regex.getRegex());
        System.out.println(nfa);
        System.out.println();
        DFA dfa = new DFA(regex);
        System.out.println(dfa);
    }

    @Test
    public void escape() throws FAException, RegexException, InvalidFormatException {
        String line = "testEscape \\\\\\*";
        Regex regex = Regex.parse(line);
        NFA nfa = new NFA(regex.getRegex());
        DFA dfa = new DFA(regex);
        System.out.println(regex);
        System.out.println();
        System.out.println(nfa);
        System.out.println();
        System.out.println(dfa);
    }

    @Test
    public void testQuoteMark() throws RegexException, InvalidFormatException, FAException {
        String line = "testQuoteMark \"abc\"?|[1-9]";
        Regex regex = Regex.parse(line);
        System.out.println(regex);
        NFA nfa = new NFA(regex.getRegex());
        System.out.println(nfa);
        DFA dfa = new DFA(regex);
        System.out.println(dfa);
    }
}