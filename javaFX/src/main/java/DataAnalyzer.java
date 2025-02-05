import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Date; 
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;


public class DataAnalyzer extends AbstractDataAnalyzer {
    private final String dbUrl = "jdbc:postgresql://localhost:5432/Incident"; 
    private final String dbUser = "postgres"; 
    private final String dbPassword = "Postgres25"; 


    // Method to calculate correlation
    public void calculateCorrelation() throws SQLException {
        String query = "SELECT priority, impact, urgency FROM incidents";
        List<Double> priorities = new ArrayList<>();
        List<Double> impacts = new ArrayList<>();
        List<Double> urgencies = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String priorityStr = rs.getString("priority");
                String impactStr = rs.getString("impact");
                String urgencyStr = rs.getString("urgency");

                try {
                    if (priorityStr != null && impactStr != null && urgencyStr != null) {
                        double priority = Double.parseDouble(priorityStr);
                        double impact = Double.parseDouble(impactStr);
                        double urgency = Double.parseDouble(urgencyStr);

                        priorities.add(priority);
                        impacts.add(impact);
                        urgencies.add(urgency);
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Invalid data encountered, skipping entry: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            // Handle SQL exceptions here
        }

        if (priorities.size() > 1 && impacts.size() > 1 && urgencies.size() > 1) {
            double[] priorityArray = priorities.stream().mapToDouble(Double::doubleValue).toArray();
            double[] impactArray = impacts.stream().mapToDouble(Double::doubleValue).toArray();
            double[] urgencyArray = urgencies.stream().mapToDouble(Double::doubleValue).toArray();

            PearsonsCorrelation correlation = new PearsonsCorrelation();
            System.out.println("Correlation between Priority and Impact: " +
                    correlation.correlation(priorityArray, impactArray));
            System.out.println("Correlation between Priority and Urgency: " +
                    correlation.correlation(priorityArray, urgencyArray));
        } else {
            System.out.println("Not enough valid data for correlation calculation.");
        }
    }

    // Regression analysis method
    public void runRegression() {
        String query = "SELECT priority, urgency, impact, reopen_count, reassignment_count FROM incidents";
        List<double[]> predictorsList = new ArrayList<>();
        List<Double> targetList = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                try {
                    double priority = Double.parseDouble(rs.getString("priority"));
                    double urgency = Double.parseDouble(rs.getString("urgency"));
                    double impact = Double.parseDouble(rs.getString("impact"));
                    double reopenCount = Double.parseDouble(rs.getString("reopen_count"));
                    double reassignmentCount = Double.parseDouble(rs.getString("reassignment_count"));

                    predictorsList.add(new double[]{priority, urgency, impact, reopenCount});
                    targetList.add(reassignmentCount);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid data encountered, skipping entry: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }

        if (!predictorsList.isEmpty() && !targetList.isEmpty()) {
            double[][] predictors = predictorsList.toArray(double[][]::new);
            double[] target = targetList.stream().mapToDouble(Double::doubleValue).toArray();

            OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
            regression.newSampleData(target, predictors);

            double[] coefficients = regression.estimateRegressionParameters();

            System.out.println("Regression Equation Coefficients:");
            for (int i = 0; i < coefficients.length; i++) {
                if (i == 0) {
                    System.out.println("Intercept: " + coefficients[i]);
                } else {
                    System.out.println("Coefficient for Predictor " + i + ": " + coefficients[i]);
                }
            }
        } else {
            System.out.println("Not enough valid data for regression analysis.");
        }
    }

    // Time Series Analysis Method (modified to fetch dates from the database)
    public void analyzeDateFrequency() throws SQLException, ParseException {
        String query = "SELECT resolved_at FROM incidents";
        List<String> dates = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String dateStr = rs.getString("resolved_at");
                if (dateStr != null) {
                    dates.add(dateStr);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching data: " + e.getMessage());
        }

        analyzeDateFrequencyLogic(dates);
    }

    private void analyzeDateFrequencyLogic(List<String> dates) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        TreeMap<Date, Integer> dateCounts = new TreeMap<>();
    
        for (String dateStr : dates) {
            Date date = dateFormat.parse(dateStr);
            dateCounts.put(date, dateCounts.getOrDefault(date, 0) + 1);
        }
        
    
        for (Map.Entry<Date, Integer> entry : dateCounts.entrySet()) {
            System.out.println(dateFormat.format(entry.getKey()) + " -> " + entry.getValue() + " incidents");
        }
    }


    
    @Override
    public void analyzeData() {
        try {
            System.out.println("Calculating Correlation:");
            calculateCorrelation();
            
            System.out.println("\nRunning Regression Analysis:");
            runRegression();
            
            System.out.println("\nAnalyzing Date Frequency:");
            analyzeDateFrequency();
        } catch (SQLException | ParseException e) {
            System.out.println("An error occurred while analyzing data: " + e.getMessage());
        }
    }

    @Override
    public void importData(String filePath) {
        
        throw new UnsupportedOperationException("Unimplemented method 'importData'");
    }

    @Override
    public void preprocessData() {
       
        throw new UnsupportedOperationException("Unimplemented method 'preprocessData'");
    }
    
    

}