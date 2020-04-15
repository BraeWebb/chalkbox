package chalkbox2.api;

import chalkbox.api.collections.Data;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;


public class Submission {

    private String id = "";
    private Data data = new Data();
    private boolean failed = false;


    public Submission() {
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public void loadData(String json) {
        this.data = new Data(json);
    }

    public Data getData() {
        return data;
    }

    public static void save(Submission submission,
                            String path) throws IOException {
        var gson = new GsonBuilder().setPrettyPrinting().create();
        var writer = new FileWriter(path);
        gson.toJson(submission, writer);
        writer.flush();
        writer.close();
    }

    @Override
    public String toString() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }
}
