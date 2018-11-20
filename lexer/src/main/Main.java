package main;

import main.core.Lexer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException {
        Scanner in = new Scanner(System.in);

        System.out.print("Input the path of your lex source file: ");
        String lexSourceFilePath = in.nextLine();
        System.out.print("Input the path of the file to be analyzed: ");
        String inputFilePath = in.nextLine();
        System.out.print("Input the path of the output file ([Enter] as the default): ");
        String outputFilePath = in.nextLine();

        if ("".equals(outputFilePath.trim())) {
            Lexer lexer = new Lexer(lexSourceFilePath);
            lexer.analyze(new FileInputStream(inputFilePath));
        } else {
            Lexer lexer = new Lexer(lexSourceFilePath);
            lexer.analyze(new FileInputStream(inputFilePath), new FileOutputStream(outputFilePath));
        }
    }
}
