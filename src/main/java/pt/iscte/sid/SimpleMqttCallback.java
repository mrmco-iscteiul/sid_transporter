package pt.iscte.sid;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.async.client.MongoClient;
import org.bson.Document;
import org.eclipse.paho.client.mqttv3.*;
import pt.iscte.sid.utils.Utils;
import pt.iscte.sid.validator.HumidityTemperatureValidador;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SimpleMqttCallback implements MqttCallback {

    private static final Logger LOGGER = Logger.getLogger(SimpleMqttCallback.class.getName());
    private MqttClient client;
    private MongoClient mongoClient;
    private ArrayList<Document> humidityTemperatureList;
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
        if (!client.isConnected()) {
            try {
                client.connect();
                client.subscribe(topic);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    public void messageArrived(String topic, MqttMessage mqttMessage){
        try {
            String messageArrived = new String(mqttMessage.getPayload());
            HumidityTemperature humidityTemperature = mapper.readValue(messageArrived, HumidityTemperature.class);
            HumidityTemperatureValidador validador = new HumidityTemperatureValidador(humidityTemperature);
            validador.processValidation();
            if (validador.isValid()) {
                humidityTemperature.setDate(Utils.convertDate(humidityTemperature.getDate()));
                PersistData persistData = new PersistData(mongoClient, humidityTemperatureList, humidityTemperature);
                persistData.execute();
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "There was a problem mapping the HumidityTemperature DTO");
        }
    }

    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

        System.out.println("Message was delivered completely!!!");
        // not used in this example

    }
}
