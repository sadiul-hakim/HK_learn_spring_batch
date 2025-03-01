package xyz.sadiulhakim.basic_project.pojo;

import com.thoughtworks.xstream.security.ExplicitTypePermission;
import org.springframework.oxm.xstream.XStreamMarshaller;

import java.util.HashMap;
import java.util.Map;

public class SensorData {

    private String date;
    private double min;
    private double avg;
    private double max;

    public static final String ROOT_FRAGMENT_NAME = "daily-data";

    public SensorData() {
    }

    public SensorData(String date, double max, double avg, double min) {
        this.date = date;
        this.max = max;
        this.avg = avg;
        this.min = min;
    }

    public static XStreamMarshaller getMarshaller() {
        XStreamMarshaller marshaller = new XStreamMarshaller();

        Map<String, Class> aliases = new HashMap<>();
        aliases.put(ROOT_FRAGMENT_NAME, SensorData.class);
        aliases.put("date", String.class);
        aliases.put("min", Double.class);
        aliases.put("avg", Double.class);
        aliases.put("max", Double.class);

        ExplicitTypePermission typePermission = new ExplicitTypePermission(new Class[]{SensorData.class});

        marshaller.setAliases(aliases);
        marshaller.setTypePermissions(typePermission);
        return marshaller;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getAvg() {
        return avg;
    }

    public void setAvg(double avg) {
        this.avg = avg;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }
}
