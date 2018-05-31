package pt.iscte.sid;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.MongoSocketOpenException;
import com.mongodb.ServerAddress;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.connection.ClusterSettings;
import it.sauronsoftware.junique.AlreadyLockedException;
import it.sauronsoftware.junique.JUnique;
import it.sauronsoftware.junique.MessageHandler;
import org.bson.BsonTimestamp;
import org.bson.Document;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        processConfigFile();
        boolean alreadyRunning;
        try {
            JUnique.acquireLock(ApplicationConstants.ID, new MessageHandler() {
                @Override
                public String handle(String s) {
                    System.out.println("This is some new Argument Received!");
                    return "There is something going ON!!!";
                }
            });
            alreadyRunning = false;
        } catch (AlreadyLockedException e) {
            alreadyRunning = true;
        }

        if (!alreadyRunning) {
            try {
                MqttClient client = new MqttClient("tcp://iot.eclipse.org:1883", MqttClient.generateClientId());
                // MqttClient client = new MqttClient("tcp://iot.eclipse.org:1883", "js-utility-oDpOk");
                MongoClient mongoClient = MongoClients.create(ApplicationConstants.MONGO_LOGIN_AUTH);
                client.setCallback(new SimpleMqttCallback(client, mongoClient, ApplicationConstants.TOPIC));
                MongoCollection<Document> collection = mongoClient
                        .getDatabase(ApplicationConstants.MONGO_DATABASE)
                        .getCollection(ApplicationConstants.MONGO_COLLECTION);
                PersistResult singleResultCallback = new PersistResult();
                ArrayList<Document> humidityTemperatureList = new ArrayList<>();

                for (int i = 0; i < 1000; i++) {
                    HumidityTemperature tmp = new HumidityTemperature();
                    tmp.setHumidity((double) i);
                    tmp.setTemperature((double) i);
                    tmp.setTime("23:30:00");
                    tmp.setDate("2018-05-31");
                    humidityTemperatureList.add(new Document("temperature", tmp.getTemperature())
                            .append("humidity", tmp.getHumidity())
                            .append("date", tmp.getDate())
                            .append("time", tmp.getTime())
                            .append("created_at", new BsonTimestamp()));
                }
                System.out.println("GOING TO START MIGRATION NOW!!!!");
                mongoClient
                        .getDatabase(ApplicationConstants.MONGO_DATABASE)
                        .getCollection(ApplicationConstants.MONGO_COLLECTION)
                        .insertMany(humidityTemperatureList, singleResultCallback);
                System.out.println("Finished the a COMPLETE TRANSACTION");
                client.connect();
                client.subscribe(ApplicationConstants.TOPIC);
            } catch (MqttException e) {
               e.printStackTrace();
           }
        }
    }

    /**
     * Processes the configuration file properties
     */
    public static void processConfigFile() {
        Properties properties = new Properties();
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream("src/main/resources/config.properties");
            properties.load(inputStream);
            ApplicationConstants.ID = properties.getProperty("unique_id");
            ApplicationConstants.TOPIC = properties.getProperty("topic");
            ApplicationConstants.MONGO_DATABASE = properties.getProperty("database");
            ApplicationConstants.MONGO_COLLECTION = properties.getProperty("collection");
            String auth = "mongodb://" + properties.getProperty("dbUser") + ":"
                    + properties.getProperty("dbPass") + "@" + properties.getProperty("ip")
                    + ":" + properties.getProperty("port") + "/" + ApplicationConstants.MONGO_DATABASE;
            ApplicationConstants.MONGO_LOGIN_AUTH = auth;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
