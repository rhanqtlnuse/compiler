package main.util.io;

import org.junit.Assert;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class LexerStubTest {

    @Test
    public void testEmpty() throws FileNotFoundException {
        LexerStub stub = new LexerStub(new FileInputStream("testfiles/test_TokenStub_empty.in"));
        Assert.assertFalse(stub.hasNextToken());
    }

    @Test
    public void testNormal() throws FileNotFoundException {
        LexerStub stub = new LexerStub(new FileInputStream("sample/tokens.out"));
        String[] expected = new String[]{"<if>", "<id, aName>", "<while>", "<number, 12.54>"};
        int i = 0;
//        while (stub.hasNextToken()) {
//            Assert.assertEquals(expected[i], stub.nextToken());
//            i++;
//        }

        System.out.println(stub.nextToken());
        System.out.println(stub.nextToken());
        System.out.println(stub.nextToken());
    }

    @Test
    public void testUselessChar() throws FileNotFoundException {
        LexerStub stub = new LexerStub(new FileInputStream("testfiles/test_TokenStub_useless.in"));
        Assert.assertFalse(stub.hasNextToken());
    }

    @Test
    public void test() throws FileNotFoundException {
        LexerStub stub = new LexerStub(new FileInputStream("sample/1/tokens.out"));
        while (stub.hasNextToken()) {
            System.out.println(stub.nextToken());
        }
    }
}