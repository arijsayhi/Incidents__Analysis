import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.io.*;
import java.util.*;
import java.util.regex.*;


public class CSVDataHandler extends AbstractDataHandler {
    private List<String[]> data = new ArrayList<>();
    private final String dbUrl = "jdbc:postgresql://localhost:5432/Incident"; 
    private final String dbUser = "postgres"; 
    private final String dbPassword = "Postgres25";

    @Override
    public void importData(String filePath) {
        validateFileFormat(filePath);
        System.out.println("Importing data from: " + filePath);

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            
            reader.readNext(); 
            data = reader.readAll(); 
            System.out.println("Data successfully imported. Number of rows: " + data.size());
        } catch (IOException | CsvException e) {
            System.err.println("Error while importing data: " + e.getMessage());
        }
    }

    @Override
    public void preprocessData() {
        System.out.println("Preprocessing CSV data...");
        try {
            System.out.println("1. Handling missing values...");
            data.removeIf(row -> Arrays.stream(row).anyMatch(cell -> cell == null || cell.isEmpty() || "?".equals(cell)));

            System.out.println("2. Normalizing data...");
            for (String[] row : data) {
                for (int i = 0; i < row.length; i++) {
                    row[i] = row[i].trim().toLowerCase();
                }
            }
            System.out.println("3. Removing duplicates...");
            Set<String[]> uniqueData = new LinkedHashSet<>(data);
            data = new ArrayList<>(uniqueData);
            
            System.out.println("4. Transforming Impact, Urgency, and Priority...");
            for (String[] row : data) {
                row[19] = extractNumberFromText(row[19]); 
                row[20] = extractNumberFromText(row[20]);
                row[21] = extractNumberFromText(row[21]);
            }
            System.out.println("5. Saving processed data to PostgreSQL database...");
            saveToDatabase();

        } catch (Exception e) {
            System.err.println("Error during preprocessing: " + e.getMessage());
        }
    }

private String extractNumberFromText(String input) {
        if (input != null) {
            Pattern pattern = Pattern.compile("^\\d+");
            Matcher matcher = pattern.matcher(input);
            if (matcher.find()) {
                return matcher.group(0); 
            }
        }
        return input; 
    }
    public void saveToDatabase() {
    String insertSQL = "INSERT INTO incidents (" +
            "number, incident_state, active, reassignment_count, reopen_count, sys_mod_count, made_sla, caller_id, " +
            "opened_by, opened_at, sys_created_by, sys_created_at, sys_updated_by, sys_updated_at, contact_type, " +
            "location, category, subcategory, u_symptom, impact, urgency, priority, assignment_group, assigned_to, " +
            "knowledge, u_priority_confirmation, notify, closed_code, resolved_by, resolved_at, closed_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
         PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

        // Skip the header row and iterate over the processed data
        for (int i = 1; i < data.size(); i++) {
            String[] row = data.get(i);

            
            pstmt.setString(1, row[0]); 
            pstmt.setString(2, row[1]); 
            pstmt.setBoolean(3, Boolean.parseBoolean(row[2])); 
            pstmt.setInt(4, Integer.parseInt(row[3])); 
            pstmt.setInt(5, Integer.parseInt(row[4])); 
            pstmt.setInt(6, Integer.parseInt(row[5])); 
            pstmt.setBoolean(7, Boolean.parseBoolean(row[6])); 
            pstmt.setString(8, row[7]); 
            pstmt.setString(9, row[8]); 
            pstmt.setTimestamp(10, parseTimestamp(row[9])); 
            pstmt.setString(11, row[10]); 
            pstmt.setTimestamp(12, parseTimestamp(row[11])); 
            pstmt.setString(13, row[12]);
            pstmt.setTimestamp(14, parseTimestamp(row[13])); 
            pstmt.setString(15, row[14]); 
            pstmt.setString(16, row[15]); 
            pstmt.setString(17, row[16]); 
            pstmt.setString(18, row[17]); 
            pstmt.setString(19, row[18]); 
            pstmt.setString(20, row[19]); 
            pstmt.setString(21, row[20]); 
            pstmt.setString(22, row[21]); 
            pstmt.setString(23, row[22]); 
            pstmt.setString(24, row[23]); 
            pstmt.setBoolean(25, Boolean.parseBoolean(row[24])); 
            pstmt.setBoolean(26, Boolean.parseBoolean(row[25])); 
            pstmt.setString(27, row[26]); 
            pstmt.setString(28, row[27]); 
            pstmt.setString(29, row[28]); 
            pstmt.setTimestamp(30, parseTimestamp(row[29])); 
            pstmt.setTimestamp(31, parseTimestamp(row[30])); 

            pstmt.addBatch(); 
        }

        pstmt.executeBatch(); 
        System.out.println("Data successfully saved to the database.");
    } catch (SQLException e) {
        System.err.println("Error while saving data to the database: " + e.getMessage());
    }
}
    private Timestamp parseTimestamp(String timestampStr) {
    String[] patterns = {"M/d/yyyy H:mm", "yyyy-MM-dd HH:mm", "d/M/yyyy H:mm"}; 
    for (String pattern : patterns) {
        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(pattern);
            LocalDateTime dateTime = LocalDateTime.parse(timestampStr, inputFormatter);
            return Timestamp.valueOf(dateTime);
        } catch (DateTimeParseException e) {
            
        }
    }
    System.err.println("Invalid timestamp format: " + timestampStr);
    return null;
        }

    @Override
    public void analyzeData() {
       
        throw new UnsupportedOperationException("Unimplemented method 'analyzeData'");
    }

    
    
}
