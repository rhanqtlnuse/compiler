package main.exception;

import java.io.IOException;

public class InvalidNameException extends IOException {

    public InvalidNameException(String msg) {
        super(msg);
    }

}
