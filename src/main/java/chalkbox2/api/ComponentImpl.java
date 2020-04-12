package chalkbox2.api;

import chalkbox2.commands.java.Conformance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

public class ComponentImpl implements Component, Loggable {

    // what does this need to be, does it even need anything fancy?, most of these will use reactive stuff
    // within themselves but other than that could just be normal

    @Override
    public void init() throws Exception {

    }

    @Override
    public Submission run(Submission submission) throws IOException, Exception {
        return submission;
    }

    @Override
    public List<Submission> run(List<Submission> submissions) throws Exception {
        for (var submission : submissions) {
            run(submission);
        }

        return submissions;
    }

    @Override
    public void after() {

    }
}
