package pt.iscte.sid;

import com.mongodb.async.SingleResultCallback;
import org.bson.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PersistResult implements SingleResultCallback<Void> {

    private static final Logger LOGGER = Logger.getLogger(PersistResult.class.getName());
    private ArrayList<Document> humidityTemperatureList;
    private Document humidityTemperature;

    public PersistResult() {

    }

    public PersistResult(
            ArrayList<Document> humidityTemperatureList, Document humidityTemperature) {
        this.humidityTemperatureList = humidityTemperatureList;
        this.humidityTemperature = humidityTemperature;
    }

    @Override
    public void onResult(Void aVoid, Throwable throwable) {
        if (throwable == null) {
            LOGGER.log(Level.FINE, "Success saving Document.");
        } else {
            if (throwable.getCause() instanceof IOException) {
                LOGGER.log(Level.SEVERE, "Connection to MongoDB was Lost.");
                humidityTemperatureList.add(humidityTemperature);
            }
        }
    }
}
