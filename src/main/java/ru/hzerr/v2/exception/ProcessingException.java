package ru.hzerr.v2.exception;

public class ProcessingException extends Exception {

    public ProcessingException(String message) {
        super(message);
    }

    public ProcessingException(String message, Exception cause) {
        super(message, cause);
    }
}
