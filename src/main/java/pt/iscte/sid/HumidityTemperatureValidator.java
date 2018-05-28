package pt.iscte.sid;

public class HumidityTemperatureValidator {

    private boolean valid;
    private HumidityTemperature humidityTemperature;

    public HumidityTemperatureValidator(HumidityTemperature humidityTemperature) {
        this.humidityTemperature = humidityTemperature;
        valid = false;
    }

    public boolean isValid() {
        return valid;
    }

    public void processMessage() {
        if (humidityTemperature.getTemperature() == null
                || humidityTemperature.getHumidity() == null
                || humidityTemperature.getDate() == null || humidityTemperature.getDate().isEmpty()
                || humidityTemperature.getTime() == null || humidityTemperature.getTime().isEmpty()) {
            valid = false;
        } else {
            valid = true;
        }
    }
}
