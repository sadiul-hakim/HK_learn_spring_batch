package xyz.sadiulhakim.basic_project.config;

import org.springframework.batch.item.ItemProcessor;
import xyz.sadiulhakim.basic_project.pojo.RawDailySensorData;
import xyz.sadiulhakim.basic_project.pojo.SensorData;

public class RawToSensorDataItemProcessor implements ItemProcessor<RawDailySensorData, SensorData> {

    @Override
    public SensorData process(RawDailySensorData item) throws Exception {
        double min = item.getMeasurements().getFirst();
        double max = min;
        double sum = 0;

        for (double measurement : item.getMeasurements()) {
            min = Math.min(min, measurement);
            max = Math.min(max, measurement);
            sum += measurement;
        }

        double avg = sum / item.getMeasurements().size();

        return new SensorData(item.getDate(), convertToCelsius(min), convertToCelsius(avg), convertToCelsius(max));
    }

    private static double convertToCelsius(double fahT) {
        return (5 * (fahT - 32)) / 9;
    }
}
