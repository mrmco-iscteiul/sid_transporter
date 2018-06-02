package pt.iscte.sid;

import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.MongoClient;
import org.bson.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PersistResult implements SingleResultCallback<Void> {

    private static final Logger LOGGER = Logger.getLogger(PersistResult.class.getName());
    private ArrayList<Document> humidityTemperatureList;
    private Document humidityTemperature;
    private MongoClient mongoClient;

    public PersistResult() {

    }

    public PersistResult(
            ArrayList<Document> humidityTemperatureList, Document humidityTemperature, MongoClient mongoClient) {
        this.humidityTemperatureList = humidityTemperatureList;
        this.humidityTemperature = humidityTemperature;
        this.mongoClient = mongoClient;
    }

    @Override
    public void onResult(Void aVoid, Throwable throwable) {
        if (throwable == null) {
            LOGGER.log(Level.FINE, "Success saving Document.");
            // Check if list is not empty
            humidityTemperatureList
                    .forEach(humidityTemperature -> {
                        mongoClient
                                .getDatabase(ApplicationConstants.MONGO_DATABASE)
                                .getCollection(ApplicationConstants.MONGO_COLLECTION)
                                .insertOne(humidityTemperature, this);
                    });
        } else {
            if (throwable.getCause() instanceof IOException) {
                LOGGER.log(Level.SEVERE, "Connection to MongoDB was Lost.");
                humidityTemperatureList.add(humidityTemperature);
            }
        }
    }
}
