package main.exception;

public class RegexException extends Exception {

    public RegexException(String msg) {
        super(msg);
    }

    public RegexException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
