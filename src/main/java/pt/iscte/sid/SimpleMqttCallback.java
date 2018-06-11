package pt.iscte.sid;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoTimeoutException;
import com.mongodb.async.client.MongoClient;
import org.bson.Document;
import org.eclipse.paho.client.mqttv3.*;
import pt.iscte.sid.utils.Utils;
import pt.iscte.sid.validator.HumidityTemperatureValidador;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SimpleMqttCallback implements MqttCallback {

    private static final Logger LOGGER = Logger.getLogger(SimpleMqttCallback.class.getName());
    private MqttClient client;
    private ObjectMapper mapper;
    private PersistData persistData;

    public SimpleMqttCallback(MqttClient client, MongoClient mongoClient, ServerMonitor serverMonitor) {
        this.client = client;
        mapper = new ObjectMapper();
        persistData = new PersistData(mongoClient, serverMonitor);
    }

    public void connectionLost(Throwable throwable) {
        if (!client.isConnected()) {
            try {
                client.connect();
                client.subscribe(ApplicationConstants.TOPIC);
            } catch (MqttException e) {
                LOGGER.info("MqttClient not able to connect");
            }
        }
    }

    public void messageArrived(String topic, MqttMessage mqttMessage){
        String messageArrived = new String(mqttMessage.getPayload());
        try {
            HumidityTemperature humidityTemperature = mapper.readValue(messageArrived, HumidityTemperature.class);
            HumidityTemperatureValidador validador = new HumidityTemperatureValidador(humidityTemperature);
            validador.processValidation();
            if (validador.isValid()) {
                humidityTemperature.setDate(Utils.convertDate(humidityTemperature.getDate()));
                persistData.setHumidityTemperature(humidityTemperature);
                persistData.execute();
            }
        } catch (IOException e) {
            LOGGER.info("Not possible to convert");
        }
    }

    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        LOGGER.info("Message delivered with success.");
    }
}
