package me.alex_s168.worldmod.exc;

public class InvalidPatternException extends Exception {
    public InvalidPatternException(String message) {
        super(message);
    }

    public InvalidPatternException(String message, Throwable cause) {
        super(message, cause);
    }
}
