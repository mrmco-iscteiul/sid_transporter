package pt.iscte.sid;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.async.client.MongoClient;
import org.eclipse.paho.client.mqttv3.*;

import java.util.ArrayList;

public class SimpleMqttCallback implements MqttCallback {

    private MqttClient client;
    private MongoClient mongoClient;
    private ArrayList<HumidityTemperature> humidityTemperatureList;
    private String topic;
    private ObjectMapper mapper;

    public SimpleMqttCallback(MqttClient client, MongoClient mongoClient, String topic) {
        this.client = client;
        this.mongoClient = mongoClient;
        this.humidityTemperatureList = new ArrayList<>();
        this.topic = topic;
        mapper = new ObjectMapper();
    }

    public void connectionLost(Throwable throwable) {
        System.out.println("No Connection");
        if (!client.isConnected()) {
            try {
                client.connect();
                client.subscribe(topic);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        String messageArrived = new String(mqttMessage.getPayload());
        HumidityTemperature humidityTemperature = mapper.readValue(messageArrived, HumidityTemperature.class);

        // Convert Date
        humidityTemperature.setDate(Utils.convertDate(humidityTemperature.getDate()));

        HumidityTemperatureValidator validator = new HumidityTemperatureValidator(humidityTemperature);
        validator.processMessage();
        if (validator.isValid()) {
            // We are going to persist the data
            PersistData persistData = new PersistData(mongoClient, humidityTemperatureList, humidityTemperature);
            persistData.execute();
        }
    }

    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

        System.out.println("Message was delivered completely!!!");
        // not used in this example

    }
}
