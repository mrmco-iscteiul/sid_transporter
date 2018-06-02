package pt.iscte.sid;

import com.mongodb.async.client.MongoClient;
import org.bson.BsonTimestamp;
import org.bson.Document;

import java.util.ArrayList;

public class PersistData {

    private HumidityTemperature humidityTemperature;
    private ArrayList<Document> humidityTemperatureList;
    private MongoClient mongoClient;

    public PersistData(MongoClient mongoClient,
                       ArrayList<Document> humidityTemperatureList,
                       HumidityTemperature humidityTemperature) {
        this.humidityTemperature = humidityTemperature;
        this.humidityTemperatureList = humidityTemperatureList;
        this.mongoClient = mongoClient;
    }

    public void execute() {
        Document data = createDatabaseObject(humidityTemperature);
        mongoClient
                .getDatabase(ApplicationConstants.MONGO_DATABASE)
                .getCollection(ApplicationConstants.MONGO_COLLECTION)
                .insertOne(data, new PersistResult(humidityTemperatureList, data, mongoClient));
    }

    private Document createDatabaseObject(HumidityTemperature humidityTemperature) {
        return new Document("temperature", humidityTemperature.getTemperature())
                .append("humidity", humidityTemperature.getHumidity())
                .append("date", humidityTemperature.getDate())
                .append("time", humidityTemperature.getTime())
                .append("created_at", new BsonTimestamp());
    }
}
