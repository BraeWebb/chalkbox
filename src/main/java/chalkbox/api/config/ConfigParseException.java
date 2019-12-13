package chalkbox.api.config;

public class ConfigParseException extends Exception {
    public ConfigParseException(String message) {
        super(message);
    }

    public ConfigParseException(String message, Exception exception) {
        super(message, exception);
    }
}
