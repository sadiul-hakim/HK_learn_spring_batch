package xyz.sadiulhakim.basic_project.pojo;

public class DataAnomaly {

    private String date;
    private AnomalyType type;
    private double value;

    public DataAnomaly() {
    }

    public DataAnomaly(String date, AnomalyType type, double value) {
        this.date = date;
        this.type = type;
        this.value = value;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public AnomalyType getType() {
        return type;
    }

    public void setType(AnomalyType type) {
        this.type = type;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}

