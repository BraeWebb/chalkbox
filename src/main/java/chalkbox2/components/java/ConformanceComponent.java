package chalkbox2.components.java;

import chalkbox.api.collections.Bundle;
import chalkbox.api.common.java.Compiler;
import chalkbox.api.files.FileLoader;
import chalkbox.java.conformance.SourceLoader;
import chalkbox2.api.ComponentImpl;
import chalkbox2.api.Submission;


import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConformanceComponent extends ComponentImpl {

    boolean noInteraction = false;
    String templateFolder;
    String submissionFolder;

    private Map<String, Class> expectedClasses = new HashMap<>();
    private List<String> expectedFiles = new ArrayList<>();

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
        expectedClasses.putAll(load(templateFolder));
        expectedFiles.addAll(FileLoader.loadFiles(templateFolder));
    }

    public Submission run(Submission submission) throws Exception {

        var submissionPath = String.join(File.separator, submissionFolder, submission.getId());

        var submissionFiles = FileLoader.loadFiles(submissionPath);
        var submissionClasses = load(submissionPath);

        var missing = new ArrayList<>(expectedFiles);
        missing.removeAll(submissionFiles);
        var extra = new ArrayList<>(submissionFiles);
        extra.removeAll(expectedFiles);

        System.out.println(missing);
        System.out.println(extra);

        return submission;
    }

    private Map<String, Class> load(String path) throws Exception {
        Bundle expected = new Bundle(new File(path));
        StringWriter output = new StringWriter();
        Bundle out = new Bundle();

        //todo: need to introduce the classpath
        Compiler.compile(Compiler.getSourceFiles(expected), "",
                out.getUnmaskedPath(), output);

        SourceLoader expectedLoader = new SourceLoader(out.getUnmaskedPath());
        return expectedLoader.getClassMap();
    }

}
