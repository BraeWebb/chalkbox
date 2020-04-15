package chalkbox2.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface Loggable {
    public default Logger logger() {
        return LogManager.getLogger(this.getClass().getName());
    }
}
