package chalkbox.api.common.java;

/**
 * An exception which occurs when there is an issue parsing JUnitParser output.
 */
public class JUnitParseException extends Exception {
    public JUnitParseException() {
        super();
    }

    public JUnitParseException(String message) {
        super(message);
    }
}
