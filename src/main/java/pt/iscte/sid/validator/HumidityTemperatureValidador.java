package pt.iscte.sid.validator;

import pt.iscte.sid.HumidityTemperature;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Validator for HumidityTemperature
 */
public class HumidityTemperatureValidador {

    private HumidityTemperature humidityTemperature;
    private boolean valid;

    public HumidityTemperatureValidador() {
        valid = false;
    }

    public HumidityTemperatureValidador(HumidityTemperature humidityTemperature) {
        this.humidityTemperature = humidityTemperature;
        valid = false;
    }

    public HumidityTemperature getHumidityTemperature() {
        return humidityTemperature;
    }

    public void setHumidityTemperature(HumidityTemperature humidityTemperature) {
        this.humidityTemperature = humidityTemperature;
    }

    public boolean isValid() {
        return valid;
    }

    public void processValidation() {
        if (processDate()) {
            if (processTime()) {
                valid = true;
            } else {
                valid = false;
            }
        } else {
            valid = false;
        }
    }

    private boolean processDate() {
        LocalDate atualDate = LocalDate.now();
        String[] list = humidityTemperature.getDate().split("/");
        if (list.length == 3) {
            int day = Integer.parseInt(list[0]);
            int month = Integer.parseInt(list[1]);
            int year = Integer.parseInt(list[2]);
            LocalDate sensorDate = LocalDate.of(year, month, day);
            if (atualDate.getYear() == sensorDate.getYear() && atualDate.getMonth() == sensorDate.getMonth()
                    && atualDate.getDayOfMonth() == sensorDate.getDayOfMonth()) {
                return true;
            }
        }
        return false;
    }

    private boolean processTime() {
        LocalTime atualTime = LocalTime.now();
        String[] list = humidityTemperature.getTime().split(":");
        if (list.length == 3) {
            int hours = Integer.parseInt(list[0]);
            int minutes = Integer.parseInt(list[1]);
            int seconds = Integer.parseInt(list[2]);
            if ((hours <= 23 && hours >= 0) && (minutes <= 59 && minutes >= 0) && (seconds <= 59 && seconds >= 0)
                    && (atualTime.getHour() == hours) && (atualTime.getMinute() == minutes)) {
                return true;
            }
        }
        return false;
    }
}
