package chalkbox2.api;

import java.io.IOException;

public interface Component {

    void init() throws Exception;

    Submission run(Submission submission) throws IOException;

    void after();
}
