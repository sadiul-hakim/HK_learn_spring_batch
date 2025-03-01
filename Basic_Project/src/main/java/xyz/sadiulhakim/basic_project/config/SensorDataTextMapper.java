package xyz.sadiulhakim.basic_project.config;

import org.springframework.batch.item.file.LineMapper;
import xyz.sadiulhakim.basic_project.pojo.RawDailySensorData;

import java.util.Arrays;
import java.util.List;

public class SensorDataTextMapper implements LineMapper<RawDailySensorData> {

    @Override
    public RawDailySensorData mapLine(String line, int lineNumber) throws Exception {
        RawDailySensorData data = new RawDailySensorData();

        // Set date
        String[] lineArr = line.split(":");
        data.setDate(lineArr[0]);

        // Read values
        String[] valueArr = lineArr[1].split(",");
        List<Double> values = Arrays.stream(valueArr).map(Double::parseDouble).toList();
        data.setMeasurements(values);
        return data;
    }
}
