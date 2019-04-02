package chalkbox.api.collections;

/**
 * A collection contains a folder bundle, associated metadata and result data
 */
public class Collection {
    private Bundle bundle;
    private Data metadata;
    private Data results;

    /**
     * Construct a new collection with a set of metadata
     *
     * @param metadata The metadata of the collection
     */
    public Collection(Data metadata) {
        this.bundle = new Bundle();
        this.metadata = metadata;
        this.results = new Data();
    }

    public Bundle getBundle() {
        return bundle;
    }

    public Data getMetadata() {
        return metadata;
    }

    public Data getResults() {
        return results;
    }
}
