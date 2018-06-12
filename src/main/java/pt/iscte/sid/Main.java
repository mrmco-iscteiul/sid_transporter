package pt.iscte.sid;

import com.mongodb.*;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.connection.ClusterSettings;
import it.sauronsoftware.junique.AlreadyLockedException;
import it.sauronsoftware.junique.JUnique;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static final String serverURIMqttClient = "tcp://iot.eclipse.org:1883";

    public static void main(String[] args) {
        boolean alreadyRunning;
        try {
            JUnique.acquireLock(ApplicationConstants.ID, message -> {
                LOGGER.info("Application already running!");
                return "Application already running!";
            });
            alreadyRunning = false;
        } catch (AlreadyLockedException e) {
            alreadyRunning = true;
        }

        if (!alreadyRunning) {
            try {
                WatchService watchService = FileSystems.getDefault().newWatchService();
                Path path = Paths.get(System.getProperty("user.dir"));
                if (!new File(path.toString() + System.lineSeparator() + "config.properties").exists()) {
                    LOGGER.info("The configuration file doesn't exist. Please try again with the configuration file.");
                    System.exit(1);
                }
                path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
                WatchKey key;
                ServerMonitor serverMonitor = new ServerMonitor();
                processConfigFile();
                MqttClient client = new MqttClient(serverURIMqttClient, MqttClient.generateClientId());
                MongoClient mongoClient = createConnectionToMongoDB(serverMonitor);
                client.setCallback(new SimpleMqttCallback(client, mongoClient, serverMonitor));
                client.connect();
                client.subscribe(ApplicationConstants.TOPIC);
                String currentTopic = ApplicationConstants.TOPIC;
                while ((key = watchService.take()) != null) {
                    for (WatchEvent<?> event: key.pollEvents()) {
                        if (event.context().toString().equals("config.properties")) {
                            processConfigFile();
                            // mongoClient.close();
                            // mongoClient = createConnectionToMongoDB(serverMonitor);
                            client.unsubscribe(currentTopic);
                            if (client.isConnected()) {
                                currentTopic = ApplicationConstants.TOPIC;
                                client.subscribe(currentTopic);
                            } else {
                                // client = new MqttClient(serverURIMqttClient, MqttClient.generateClientId());
                                // client.setCallback(new SimpleMqttCallback(client, mongoClient, serverMonitor));
                                currentTopic = ApplicationConstants.TOPIC;
                                client.connect();
                                client.subscribe(currentTopic);
                            }
                        }
                    }
                    key.reset();
                }
            } catch (Exception e) {
                LOGGER.info("The program stopped responding. Please restart and try again.");
                System.exit(1);
            }
        }
    }

    private static void runSetup(MqttClient client, ServerMonitor serverMonitor, MongoClient mongoClient) {
        try {
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            client.setCallback(new SimpleMqttCallback(client, mongoClient, serverMonitor));
            client.connect(connOpts);
            client.subscribe(ApplicationConstants.TOPIC);
            // client.setCallback(new SimpleMqttCallback(client, mongoClient, ApplicationConstants.TOPIC));
            // client.connect();
            // client.subscribe(ApplicationConstants.TOPIC);
            infiniteMessagePublisher(client);
        } catch (MqttException e) {
            LOGGER.info("Something went wrong with the MqttClient.");
        }
    }

    /**
     * Processes the configuration file properties
     */
    private static void processConfigFile() {
        Properties properties = new Properties();
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(System.getProperty("user.dir") + System.lineSeparator() + "config.properties");
            properties.load(inputStream);
            ApplicationConstants.IP = properties.getProperty("ip");
            ApplicationConstants.PORT = properties.getProperty("port");
            ApplicationConstants.MONGO_USER = properties.getProperty("dbUser");
            ApplicationConstants.MONGO_PWD = properties.getProperty("dbPass");
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

    private static void infiniteMessagePublisher(MqttClient client) {
        while (true) {
            try {
                Thread.sleep(5000);
                String content = "{\"date\": \"11/06/2018\", \"time\": \"12:" + LocalTime.now().getMinute() + ":00\", \"humidity\": 60.00, \"temperature\": 25.00}";
                // System.out.println("Publishing message: " + content);
                MqttMessage message = new MqttMessage(content.getBytes());
                message.setQos(0);
                client.publish(ApplicationConstants.TOPIC, message);
                // System.out.println("Message published");
                // client.disconnect();
            } catch (MqttException|InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static MongoClient createConnectionToMongoDB(ServerMonitor serverMonitor) {
        MongoCredential credential = MongoCredential.createCredential(
                ApplicationConstants.MONGO_USER,
                ApplicationConstants.MONGO_DATABASE,
                ApplicationConstants.MONGO_PWD.toCharArray());
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyToServerSettings(block -> block.addServerMonitorListener(serverMonitor))
                .applyToClusterSettings(block -> block.applySettings(ClusterSettings.builder()
                        .maxWaitQueueSize(1)
                        .serverSelectionTimeout(5000, TimeUnit.MILLISECONDS)
                        .hosts(Collections.singletonList(
                                new ServerAddress(ApplicationConstants.IP, Integer.parseInt(ApplicationConstants.PORT))))
                        .build()))
                .credential(credential)
                .build();
        return MongoClients.create(settings);
    }
}
