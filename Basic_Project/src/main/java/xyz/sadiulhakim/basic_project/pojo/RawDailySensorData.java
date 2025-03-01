package xyz.sadiulhakim.basic_project.pojo;

import java.util.List;

public class RawDailySensorData {

    private String date;
    private List<Double> measurements;

    public RawDailySensorData() {
    }

    public RawDailySensorData(String date, List<Double> measurements) {
        this.date = date;
        this.measurements = measurements;
    }

    public String getDate() {
        return date;
    }

    public List<Double> getMeasurements() {
        return measurements;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setMeasurements(List<Double> measurements) {
        this.measurements = measurements;
    }
}
