package main.util.io;

import java.io.InputStream;
import java.util.Scanner;

public class LexerStub {

    private static final String CATALOG_PATTERN = "[_a-zA-Z]\\w*";
    private static final String LEXEME_PATTERN = "\\S+";
    private static final String TOKEN_PATTERN = String.format("<%s(, %s)?>", CATALOG_PATTERN, LEXEME_PATTERN);

    private Scanner in;

    public LexerStub(InputStream src) {
        this.in = new Scanner(src);
    }

    public boolean hasNextToken() {
        return in.hasNextLine();
    }

    public String nextToken() {
        String token = in.findInLine(TOKEN_PATTERN);
        if (in.hasNextLine()) {
            in.nextLine();
        }
        return token;
    }
}
