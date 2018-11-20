package main;

import main.core.Lexer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Demo {

    public static void main(String[] args) throws IOException {
        Lexer lexer = new Lexer("sample/sample.l");
        lexer.analyze(new FileInputStream("sample/program.in"), new FileOutputStream("sample/tokens.out"));
    }

}
