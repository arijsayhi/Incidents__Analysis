import java.sql.*;

public abstract class AbstractDataAnalyzer implements DataOperations {
    private final String dbUrl = "jdbc:postgresql://localhost:5432/Incident";
    private final String dbUser = "postgres";
    private final String dbPassword = "Postgres25";

    public abstract void analyzeData();

    public  void computeSummaryStatistics() {
        // Query for incidents' opened_at and resolved_at
        String incidentQuery = "SELECT opened_at, resolved_at FROM incidents";

        // Variables to calculate duration statistics
        long totalDurationInMillis = 0; // To accumulate total duration
        int count = 0; // To count the number of incidents
        long minDurationInMillis = Long.MAX_VALUE; // To track the minimum duration
        long maxDurationInMillis = Long.MIN_VALUE; // To track the maximum duration
        long sumOfSquaredDifferences = 0; // To accumulate squared differences for variance calculation

        // Query for most frequent location
        String locationQuery = "SELECT location, COUNT(*) as count FROM incidents GROUP BY location ORDER BY count DESC LIMIT 1";

        // Query for active incidents (count of true values in active column)
        String activeQuery = "SELECT COUNT(*) as total, SUM(CASE WHEN active = TRUE THEN 1 ELSE 0 END) as activeCount FROM incidents";

        // Query for total number of rows in incidents table
        String rowCountQuery = "SELECT COUNT(*) as totalRows FROM incidents";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            
            // Get the total number of rows in the incidents table
            try (PreparedStatement stmt = conn.prepareStatement(rowCountQuery);
                 ResultSet rs = stmt.executeQuery()) {
                
                // Retrieve the total row count
                if (rs.next()) {
                    int totalRows = rs.getInt("totalRows");

                    // Print the total number of rows
                    System.out.println("Total number of incidents detected: " + totalRows);
                } else {
                    System.out.println("No incidents found in the incidents table.");
                }
            }

            // First, process the incident data
            try (PreparedStatement stmt = conn.prepareStatement(incidentQuery);
                 ResultSet rs = stmt.executeQuery()) {

                // Retrieve the dates from the database
                while (rs.next()) {
                    java.sql.Date openedAt = rs.getDate("opened_at");
                    java.sql.Date resolvedAt = rs.getDate("resolved_at");

                    if (openedAt != null && resolvedAt != null) {
                        // Calculate the duration in milliseconds
                        long durationInMillis = resolvedAt.getTime() - openedAt.getTime();

                        // Accumulate the total duration
                        totalDurationInMillis += durationInMillis;

                        // Update min and max durations
                        if (durationInMillis < minDurationInMillis) {
                            minDurationInMillis = durationInMillis;
                        }
                        if (durationInMillis > maxDurationInMillis) {
                            maxDurationInMillis = durationInMillis;
                        }

                        // Accumulate squared differences for variance calculation
                        sumOfSquaredDifferences += Math.pow(durationInMillis - (totalDurationInMillis / (double) count), 2);

                        count++; // Increment the incident count
                    }
                }

                // Calculate the average duration in milliseconds
                if (count > 0) {
                    long averageDurationInMillis = totalDurationInMillis / count;

                    // Convert the average duration to days
                    long averageDurationInDays = averageDurationInMillis / (1000 * 60 * 60 * 24);

                    // Convert min and max durations to days
                    long minDurationInDays = minDurationInMillis / (1000 * 60 * 60 * 24);
                    long maxDurationInDays = maxDurationInMillis / (1000 * 60 * 60 * 24);

                    // Calculate variance (sample variance)
                    double variance = sumOfSquaredDifferences / (double) count; // Use count for population variance

                    // Calculate standard deviation
                    double standardDeviation = Math.sqrt(variance);

                    // Print the duration statistics
                    System.out.println("Average Duration: " + averageDurationInDays + " days");
                    System.out.println("Minimum Duration: " + minDurationInDays + " days");
                    System.out.println("Maximum Duration: " + maxDurationInDays + " days");
                    System.out.println("Variance: " + variance + " milliseconds^2");
                    System.out.println("Standard Deviation: " + standardDeviation + " milliseconds");
                } else {
                    System.out.println("No incidents found.");
                }
            }

            // Then, get the most frequently used location
            try (PreparedStatement stmt = conn.prepareStatement(locationQuery);
                 ResultSet rs = stmt.executeQuery()) {

                // Retrieve the most frequent location
                if (rs.next()) {
                    String mostFrequentLocation = rs.getString("location");
                    int countLocations = rs.getInt("count");

                    // Print the most frequently used location
                    System.out.println("Most frequently used location: " + mostFrequentLocation);
                    System.out.println("Occurrence count: " + countLocations);
                } else {
                    System.out.println("No locations found.");
                }
            }

            // Finally, calculate the percentage of active incidents
            try (PreparedStatement stmt = conn.prepareStatement(activeQuery);
                 ResultSet rs = stmt.executeQuery()) {

                // Retrieve the active incidents count and total count
                if (rs.next()) {
                    int totalIncidents = rs.getInt("total");
                    int activeIncidents = rs.getInt("activeCount");

                    if (totalIncidents > 0) {
                        // Calculate the percentage of active incidents
                        double activePercentage = (double) activeIncidents / totalIncidents * 100;

                        // Print the active incidents percentage
                        System.out.println("Percentage of active incidents: " + String.format("%.2f", activePercentage) + "%");
                    } else {
                        System.out.println("No incidents found.");
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }
}

