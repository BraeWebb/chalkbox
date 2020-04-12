package chalkbox2.api;

import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;

public class Submission {

    private String id;
    public Error error;
    public Map<String, SubmissionComponent> components = new HashMap<>();


    public Submission() {
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setError(Error error) {
        this.error = error;
    }

    public Error getError() {
        return error;
    }

    public void createComponent(String name) {
        components.put(name, new SubmissionComponent());
    }

    public SubmissionComponent getComponent(String name) {
        return components.get(name);
    }

    @Override
    public String toString() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }
}
