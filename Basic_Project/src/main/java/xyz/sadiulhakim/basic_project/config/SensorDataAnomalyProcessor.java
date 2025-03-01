package xyz.sadiulhakim.basic_project.config;

import org.springframework.batch.item.ItemProcessor;
import xyz.sadiulhakim.basic_project.pojo.AnomalyType;
import xyz.sadiulhakim.basic_project.pojo.DataAnomaly;
import xyz.sadiulhakim.basic_project.pojo.SensorData;

public class SensorDataAnomalyProcessor implements ItemProcessor<SensorData, DataAnomaly> {

    private static final double THRESHOLD = 0.9;

    @Override
    public DataAnomaly process(SensorData item) throws Exception {

        if ((item.getMin() / item.getAvg()) < THRESHOLD) {
            return new DataAnomaly(item.getDate(), AnomalyType.MINIMUM, item.getMin());
        } else if ((item.getAvg() / item.getMax()) < THRESHOLD) {
            return new DataAnomaly(item.getDate(), AnomalyType.MAXIMUM, item.getMax());
        } else {
            return null;
        }
    }
}
