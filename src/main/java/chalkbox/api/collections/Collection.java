package chalkbox.api.collections;

import java.io.File;
import java.io.IOException;

/**
 * A collection contains a folder bundle, associated metadata and result data
 */
public class Collection {
    private Bundle bundle;
    private Bundle working;
    private Data results;

    /**
     * Construct a new collection with a set of metadata
     *
     * @param metadata The metadata of the collection
     */
    public Collection(Data metadata) {
        try {
            this.working = new Bundle();
        } catch (IOException e) {
            System.err.println("Fatal Error: Unable to create working directory");
            System.exit(2);
        }
        this.bundle = new Bundle(new File(metadata.get("root").toString()));
        this.results = metadata;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public Bundle getWorking() {
        return working;
    }

    public Data getResults() {
        return results;
    }

    @Override
    public String toString() {
        return results.toString() + " " + bundle.toString() + " " + working.toString();
    }
}
