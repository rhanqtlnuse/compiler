package test;


import main.util.InfixToPostfix;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class InfixToPostfixTest {

    @Test
    public void convert1() {
        List<String> infix = Arrays.asList("a", "|", "[0-9]", "*");
        List<String> expected = Arrays.asList("a", "[0-9]", "*", "|");
        Assert.assertEquals(expected, InfixToPostfix.convert(infix));
    }

    @Test
    public void convert2() {
        List<String> infix = Arrays.asList("a", "#", "b", "*");
        List<String> expected = Arrays.asList("a", "b", "*", "#");
        Assert.assertEquals(expected, InfixToPostfix.convert(infix));
    }
}