package pt.iscte.sid;

import java.sql.Timestamp;

/**
 * HumidityTemperature Dto
 */
public class HumidityTemperature {
    private Double temperature;
    private Double humidity;
    private String date;
    private String time;

    public HumidityTemperature() {

    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getHumidity() {
        return humidity;
    }

    public void setHumidity(Double humidity) {
        this.humidity = humidity;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

}
