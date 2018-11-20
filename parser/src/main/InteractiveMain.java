package main;

import main.core.Parser;
import main.exception.ParseException;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class InteractiveMain {

    public static void main(String[] args) throws IOException, ParseException {
        Scanner in = new Scanner(System.in);

        System.out.print("Input the path of your yacc source file: ");
        String lexSourceFilePath = in.nextLine();
        System.out.print("Input the path of the file to be parsed: ");
        String inputFilePath = in.nextLine();
        System.out.print("Input the path of the output file ([Enter] as the default): ");
        String outputFilePath = in.nextLine();

        if ("".equals(outputFilePath.trim())) {
            Parser parser = new Parser(lexSourceFilePath);
            parser.parse(new FileInputStream(inputFilePath));
        } else {
            Parser parser = new Parser(lexSourceFilePath);
            parser.parse(new FileInputStream(inputFilePath), new FileOutputStream(outputFilePath));
        }
    }

}
