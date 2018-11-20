package main.exception;

import java.io.IOException;

public class InvalidFileFormatException extends IOException {

    public InvalidFileFormatException(String msg) {
        super(msg);
    }

}
