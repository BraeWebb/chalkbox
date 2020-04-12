package chalkbox2.api;

import chalkbox.api.collections.Data;
import com.google.gson.GsonBuilder;


public class Submission {

    private String id = "";
    private Data data = new Data();


    public Submission() {
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void loadData(String json) {
        this.data = new Data(json);
    }

    public Data getData() {
        return data;
    }

    @Override
    public String toString() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }
}
