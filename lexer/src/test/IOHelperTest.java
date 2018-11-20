package test;

import main.util.IOHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

public class IOHelperTest {

    private File inputFile;
    private File outputFile;

    @Before
    public void setUp() throws IOException {
        inputFile = new File("io_helper_test.in");
        if (!inputFile.exists()) {
            if (!inputFile.createNewFile()) {
                System.exit(-1);
            }
        }
        FileWriter writer = new FileWriter(inputFile);
        writer.write("12345    67 890\r\nabc               d");
        writer.flush();
        writer.close();
        outputFile = new File("io_helper_test.out");
    }

    @Test
    public void getBlock() throws IOException {
        IOHelper helper = new IOHelper(new FileInputStream(inputFile), new FileOutputStream(outputFile));
        String block1 = helper.getBlock();
        String block2 = helper.getBlock();
        String block3 = helper.getBlock();
        Assert.assertEquals("12345", block1);
        Assert.assertEquals("67", block2);
        Assert.assertEquals("890", block3);
    }

    @After
    public void tearDown() {
        if (inputFile.exists()) {
            inputFile.delete();
        }
        if (outputFile.exists()) {
            outputFile.delete();
        }
    }
}