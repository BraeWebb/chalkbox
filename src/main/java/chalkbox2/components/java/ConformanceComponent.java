package chalkbox2.components.java;

import chalkbox.api.collections.Bundle;
import chalkbox.api.common.java.Compiler;
import chalkbox.java.conformance.SourceLoader;
import chalkbox2.api.ComponentImpl;
import chalkbox2.api.Submission;


import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

public class ConformanceComponent extends ComponentImpl {

    boolean noInteraction = false;
    String templateFolder;
    String submissionFolder;

    private Map<String, Class> expectedClasses;

    public ConformanceComponent() {
    }

    public ConformanceComponent setNoInteraction(boolean noInteraction) {
        this.noInteraction = noInteraction;
        return this;
    }

    public ConformanceComponent setSubmissionFolder(String submissionFolder) {
        this.submissionFolder = submissionFolder;
        return this;
    }

    public ConformanceComponent setTemplateFolder(String templateFolder) {
        this.templateFolder = templateFolder;
        return this;
    }

    public void init() throws Exception {
        // get the expected file structure
        // compile the templates so we can test against the submissions
        loadExpected();
    }

    public Submission run(Submission submission) throws IOException {
        submission.createComponent("conformance");

        System.out.println(expectedClasses.toString());

        return submission;
    }

    private void loadExpected() throws Exception {
        Bundle expected = new Bundle(new File(templateFolder));
        StringWriter output = new StringWriter();

        Bundle out = new Bundle();

        Compiler.compile(Compiler.getSourceFiles(expected), "",
                out.getUnmaskedPath(), output);

        SourceLoader expectedLoader = new SourceLoader(out.getUnmaskedPath());
        expectedClasses = expectedLoader.getClassMap();
    }

}
