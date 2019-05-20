package chalkbox.output;

import chalkbox.api.annotations.ConfigItem;
import chalkbox.api.annotations.Output;
import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.PrintStream;
import java.util.List;

public class UploadMongo {
    @ConfigItem(description = "MongoDB URL to upload data")
    public String mongoURL;

    @ConfigItem(description = "Assessment identifier (e.g. ass1)")
    public String assessment;

    @ConfigItem(description = "MongoDB Database Name", required = false)
    public String database = "feedback";

    @Output(stream = "submissions")
    public void output(PrintStream stream, List<Collection> collections) {
        MongoClient mongoClient = new MongoClient(new MongoClientURI(mongoURL));

        MongoDatabase db = mongoClient.getDatabase(database);
        MongoCollection<Document> mongoCollection = db.getCollection(assessment);

        for (Collection collection : collections) {
            Data results = collection.getResults();
            results.set("timestamp", System.currentTimeMillis() / 1000L);

            mongoCollection.insertOne(Document.parse(results.toString()));
        }
    }
}
