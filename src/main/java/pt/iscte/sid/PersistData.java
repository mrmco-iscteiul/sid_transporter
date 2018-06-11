package pt.iscte.sid;

import com.mongodb.MongoSocketOpenException;
import com.mongodb.MongoTimeoutException;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoCollection;
import org.bson.BsonTimestamp;
import org.bson.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PersistData {

    private static final Logger LOGGER = Logger.getLogger(PersistData.class.getName());
    private HumidityTemperature humidityTemperature;
    private ArrayList<Document> humidityTemperatureList;
    private ArrayList<Document> tmpList;
    private int counter = 0;

    private MongoClient mongoClient;
    private ServerMonitor serverMonitor;

    public PersistData(MongoClient mongoClient, ServerMonitor serverMonitor) {
        this.mongoClient = mongoClient;
        this.serverMonitor = serverMonitor;
        humidityTemperatureList = new ArrayList<>();
        tmpList = new ArrayList<>();
    }

    public PersistData(MongoClient mongoClient, HumidityTemperature humidityTemperature) {
        this.humidityTemperature = humidityTemperature;
        this.mongoClient = mongoClient;
    }

    public void execute() {
        Document data = createDatabaseObject(humidityTemperature);
        if (serverMonitor.isAlive()) {
            MongoCollection<Document> collection = mongoClient
                    .getDatabase(ApplicationConstants.MONGO_DATABASE)
                    .getCollection(ApplicationConstants.MONGO_COLLECTION);
            if (humidityTemperatureList.size() > 0) {
                humidityTemperatureList.add(data);
                tmpList.addAll(humidityTemperatureList);
                tmpList.forEach(tmpHumidityTemperature -> {
                    humidityTemperatureList.remove(tmpHumidityTemperature);
                    insertDocument(tmpHumidityTemperature, collection);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        LOGGER.info("Interruption on Thread.");
                    }
                });
                tmpList = new ArrayList<>();
            } else {
                insertDocument(data, collection);
            }
        } else {
            humidityTemperatureList.add(data);
        }
    }

    private Document createDatabaseObject(HumidityTemperature humidityTemperature) {
        return new Document("temperature", humidityTemperature.getTemperature())
                .append("humidity", humidityTemperature.getHumidity())
                .append("date", humidityTemperature.getDate())
                .append("time", humidityTemperature.getTime())
                .append("created_at", new BsonTimestamp());
    }

    private void insertDocument(Document data, MongoCollection<Document> collection) {
        collection.insertOne(data, (avoid, throwable) -> {
            if (throwable == null) {
                LOGGER.info("Success saving Document.");
            } else {
                if (throwable.getCause() instanceof IOException) {
                    LOGGER.info("Connection to MongoDB was Lost.");
                    LOGGER.log(Level.SEVERE, "Connection LOST.");
                }
                LOGGER.info("Since something went wrong, we are going to save the element.");
                humidityTemperatureList.add(data);
            }
        });
    }

    public HumidityTemperature getHumidityTemperature() {
        return humidityTemperature;
    }

    public void setHumidityTemperature(HumidityTemperature humidityTemperature) {
        this.humidityTemperature = humidityTemperature;
    }

    public ArrayList<Document> getHumidityTemperatureList() {
        return humidityTemperatureList;
    }

    public void setHumidityTemperatureList(ArrayList<Document> humidityTemperatureList) {
        this.humidityTemperatureList = humidityTemperatureList;
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }

    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }
}
