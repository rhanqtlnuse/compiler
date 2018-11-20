package main;

import main.core.Parser;
import main.exception.ParseException;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Demo {

    public static void main(String[] args) throws IOException, ParseException {
        //                                        这里要改 ↓
        Parser parser = new Parser("sample/3/sample.y");
        //                                      这里要改 ↓
        parser.parse(new FileInputStream("sample/3/tokens.out")
                , new FileOutputStream("sample/3/parsing.out"));
        //                                    这里要改 ↑
    }
}
