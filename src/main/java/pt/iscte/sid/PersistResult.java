package pt.iscte.sid;

import com.mongodb.MongoSocketOpenException;
import com.mongodb.async.SingleResultCallback;
import org.bson.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class PersistResult implements SingleResultCallback<Void> {

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
            //System.out.println("SUCCESS");
        } else {
            if (throwable.getCause() instanceof IOException) {
//                System.out.println("OH NO!!!");
//                System.out.println("Connection Was LOST!!!");
            }
            throwable.printStackTrace();
//            System.out.println("There was a problem persisting the data!!!");
//            System.out.println("Lets save the element to the List.");
            // humidityTemperatureList.add(humidityTemperature);
        }
    }
}
