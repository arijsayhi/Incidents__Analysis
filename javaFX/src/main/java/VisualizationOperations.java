import java.util.Date;
import java.util.Map;

public interface VisualizationOperations {
    public void generateChart(Map<Date, Integer> dateFrequencies);
    public void generateReport(String filePath, Map<Date, Integer> dateFrequencies);
}
