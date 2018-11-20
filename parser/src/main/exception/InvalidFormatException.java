package main.exception;

import java.io.IOException;

public class InvalidFormatException extends IOException {

    public InvalidFormatException(String msg) {
        super(msg);
    }

}
