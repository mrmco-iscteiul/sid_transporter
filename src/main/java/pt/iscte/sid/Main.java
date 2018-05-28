package pt.iscte.sid;

import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import it.sauronsoftware.junique.AlreadyLockedException;
import it.sauronsoftware.junique.JUnique;
import it.sauronsoftware.junique.MessageHandler;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

public class Main {
    public static void main(String[] args) {
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
                client.connect();
                client.subscribe(ApplicationConstants.TOPIC);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }
}
