package chalkbox2.api;

import java.io.IOException;
import java.util.List;

public interface Component {

    void init() throws Exception;

    Submission run(Submission submission) throws Exception;

    List<Submission> run(List<Submission> submissions) throws Exception;

    void after();
}
