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
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        while (true) {
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

                    MongoClient mongoClient = MongoClients.create(ApplicationConstants.MONGO_LOGIN_AUTH);
                    MongoCollection<Document> collection = mongoClient
                            .getDatabase(ApplicationConstants.MONGO_DATABASE)
                            .getCollection(ApplicationConstants.MONGO_COLLECTION);
                    PersistResult singleResultCallback = new PersistResult();
                    ArrayList<Document> humidityTemperatureList = new ArrayList<>();

                    for (int i = 0; i < 4; i++) {
                        HumidityTemperature tmp = new HumidityTemperature();
                        tmp.setHumidity((double) i);
                        tmp.setTemperature((double) i);
                        tmp.setTime("23:00:00");
                        tmp.setDate("2018-06-01");
                        humidityTemperatureList.add(
                                new Document("temperature", tmp.getTemperature())
                                        .append("humidity", tmp.getHumidity())
                                        .append("date", tmp.getDate())
                                        .append("time", tmp.getTime())
                                        .append("created_at", new BsonTimestamp())
                        );
                    }
                    System.out.println("GOING TO START MIGRATION NOW!!!!");

                    for (Document document : humidityTemperatureList) {
                        collection.insertOne(document, singleResultCallback);
                        Thread.sleep(1);
                    }
                    try {
                        WatchService watchService = FileSystems.getDefault().newWatchService();
                        String dir = System.getProperty("user.dir") + "/src/main/resources/";
                        Path path = Paths.get(dir);
                        WatchKey watchKey = path.register(
                                watchService, StandardWatchEventKinds.ENTRY_MODIFY);
                        WatchKey key;
                        while ((key = watchService.take()) != null) {
                            for (WatchEvent<?> event : key.pollEvents()) {
                                System.out.println("Changed!!!" + event.kind());
                                System.out.println(key.pollEvents().toArray().toString());
                                processConfigFile();

                            }
                            key.reset();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    client.connect();
                    client.subscribe(ApplicationConstants.TOPIC);
                } catch (MqttSecurityException e) {
                    e.printStackTrace();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
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
            System.out.println(ApplicationConstants.MONGO_LOGIN_AUTH);
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

