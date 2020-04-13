package chalkbox2.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public interface Saveable {
    public default void save(Submission submission, String location, boolean silent) throws Exception {
        Submission.save(submission, location);
    }
}
