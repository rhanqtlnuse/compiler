package test;

import main.exception.InvalidFormatException;
import main.exception.RegexException;
import main.core.Regex;
import main.core.Symbol;
import org.junit.Assert;
import org.junit.Test;

public class RegexTest {

    @Test
    public void parsePreprocess() throws RegexException, InvalidFormatException {
        String line1 = "      abc              range    ";
        Regex regex1 = Regex.parse(line1);
        Assert.assertEquals("Regex{ abc [r, a, n, g, e] }", regex1.toString());
    }

    @Test
    public void parseQuotation() throws RegexException, InvalidFormatException {
        String line1 = "testQuotationSetUp [0-9]";
        String line2 = "testQuotation {testQuotationSetUp}";
        Regex regex1 = Regex.parse(line1);
        Regex regex2 = Regex.parse(line2);
    }

    @Test
    public void parseEscape() throws RegexException, InvalidFormatException {
        String line1 = "testEscapeNewLine \\n";
        Regex regex1 = Regex.parse(line1);
        Assert.assertEquals("Regex{ testEscapeNewLine [\n] }", regex1.toString());

        String line2 = "testEscapeTable \\t";
        Regex regex2 = Regex.parse(line2);
        Assert.assertEquals("Regex{ testEscapeTable [\t] }", regex2.toString());

        String line3 = "testEscapeSupportedOperator \\|";
        Regex regex3 = Regex.parse(line3);
        Assert.assertEquals("Regex{ testEscapeSupportedOperator [\\|] }", regex3.toString());

        String line4 = "testEscapeUnSupportedOperator \\%";
        boolean exceptionOccurred = false;
        try {
            Regex.parse(line4);
        } catch (RegexException ex) {
            exceptionOccurred = true;
        }
        Assert.assertTrue(exceptionOccurred);
    }

    @Test
    public void parseQuoteMark() throws RegexException, InvalidFormatException {
        String line1 = "testQuoteMark \"abc\\\\xy\"";
        Regex regex1 = Regex.parse(line1);
        Assert.assertEquals("Regex{ testQuoteMark [\"abc\\\\xy\"] }", regex1.toString());

        String line2 = "testQuoteMarkUnSupportedOperator \"abc\\%de\"";
        boolean exceptionOccurred = false;
        try {
            Regex.parse(line2);
        } catch (RegexException ex) {
            exceptionOccurred = true;
        }
        Assert.assertTrue(exceptionOccurred);

        String line3 = "testQuoteMarkEscapeQuoteMark \"\\\"\"";
        Regex regex3 = Regex.parse(line3);
        Assert.assertEquals("Regex{ testQuoteMarkEscapeQuoteMark [\"\\\"\"] }", regex3.toString());
    }

    @Test
    public void parseOption1() throws RegexException, InvalidFormatException {
        String line1 = "testOptionLetter a?";
        String line2 = "testOptionDigit 6?";
        String line3 = "testOptionUnderScore _?";
        String line4 = "testOptionArbitrary .?";
        Regex regex1 = Regex.parse(line1);
        Regex regex2 = Regex.parse(line2);
        Regex regex3 = Regex.parse(line3);
        Regex regex4 = Regex.parse(line4);
        Assert.assertEquals("Regex{ testOptionLetter [(, " + Symbol.EPSILON + ", |, a, )] }", regex1.toString());
        Assert.assertEquals("Regex{ testOptionDigit [(, " + Symbol.EPSILON + ", |, 6, )] }", regex2.toString());
        Assert.assertEquals("Regex{ testOptionUnderScore [(, " + Symbol.EPSILON + ", |, _, )] }", regex3.toString());
        Assert.assertEquals("Regex{ testOptionArbitrary [(, " + Symbol.EPSILON + ", |, ., )] }", regex4.toString());
    }

    @Test
    public void parseOption2() throws RegexException, InvalidFormatException {
        String line1 = "testOptionBrace (a*(b)|c)?";
        Regex regex1 = Regex.parse(line1);
        Assert.assertEquals("Regex{ testOptionBrace [(, " + Symbol.EPSILON + ", |, (, a, *, (, b, ), |, c, ), )] }", regex1.toString());

        String line2 = "testOptionSquare [0-9]?";
        Regex regex2 = Regex.parse(line2);
        Assert.assertEquals("Regex{ testOptionSquare [(, " + Symbol.EPSILON + ", |, [0-9], )] }", regex2.toString());

        String line3 = "testOptionQuoteMark \"abc\"?";
        Regex regex3 = Regex.parse(line3);
        Assert.assertEquals("Regex{ testOptionQuoteMark [(, " + Symbol.EPSILON + ", |, \"abc\", )] }", regex3.toString());

        String line4 = "testOptionBackSlash \\\\?";
        Regex regex4 = Regex.parse(line4);
        Assert.assertEquals("Regex{ testOptionBackSlash [(, " + Symbol.EPSILON + ", |, \\\\, )] }", regex4.toString());

        String line5 = "testOptionNewLine \\n?";
        Regex regex5 = Regex.parse(line5);
        Assert.assertEquals("Regex{ testOptionNewLine [(, " + Symbol.EPSILON +  ", |, \n, )] }", regex5.toString());

        String line6 = "testOptionTable \\t?";
        Regex regex6 = Regex.parse(line6);
        Assert.assertEquals("Regex{ testOptionTable [(, " + Symbol.EPSILON + ", |, \t, )] }", regex6.toString());
    }

    @Test
    public void parseBracesBalance() throws RegexException, InvalidFormatException {
        String line1 = "testBracesBalanceNested ((((((((()))))))))";
        Regex regex1 = Regex.parse(line1);
        Assert.assertEquals("Regex{ testBracesBalanceNested [(, (, (, (, (, (, (, (, (, ), ), ), ), ), ), ), ), )] }", regex1.toString());

        String line2 = "testBracesBalanceOpposite ()()()()()()()";
        Regex regex2 = Regex.parse(line2);
        Assert.assertEquals("Regex{ testBracesBalanceOpposite [(, ), (, ), (, ), (, ), (, ), (, ), (, )] }", regex2.toString());

        String line3 = "testBracesBalanceMixed (()(()())((()))())";
        Regex regex3 = Regex.parse(line3);
        Assert.assertEquals("Regex{ testBracesBalanceMixed [(, (, ), (, (, ), (, ), ), (, (, (, ), ), ), (, ), )] }", regex3.toString());
    }

    @Test
    public void test() throws RegexException, InvalidFormatException {
        String line = "testC (a|[1-7]?)*";
        Regex.parse(line);
    }

    @Test
    public void testEscape() throws RegexException, InvalidFormatException {
        String line = "testEscape \"\\t\\\\\\?\"";
        Regex regex = Regex.parse(line);
        System.out.println(regex);
    }

    @Test
    public void testQuoteMark() throws RegexException, InvalidFormatException {
        String line1 = "testA \"ab\\t\\\\ef\\?\"";
        Regex regex = Regex.parse(line1);
        System.out.println(regex);
    }
}