package pt.iscte.sid;

import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import it.sauronsoftware.junique.AlreadyLockedException;
import it.sauronsoftware.junique.JUnique;
import it.sauronsoftware.junique.MessageHandler;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static final String serverURIMqttClient = "tcp://iot.eclipse.org:1883";

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
                MqttClient client = new MqttClient(serverURIMqttClient, MqttClient.generateClientId());
                MongoClient mongoClient = MongoClients.create(ApplicationConstants.MONGO_LOGIN_AUTH);
                client.setCallback(new SimpleMqttCallback(client, mongoClient, ApplicationConstants.TOPIC));
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
