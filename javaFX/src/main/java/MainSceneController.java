import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.Button;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.stage.FileChooser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;



public class MainSceneController extends DataVisualizer {
    
    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private void handleLoginButtonAction(ActionEvent event) throws Exception {
        String email = emailField.getText();
        String password = passwordField.getText();
        String sql = "SELECT COUNT(*) FROM IT_consultant WHERE email = ?";
        try (Connection conn = DBUtil.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
             
                if (LoginService.authenticate(email, password)) {
                    showAlert("Login successful!", Alert.AlertType.INFORMATION);
                    changeScene(event,"Dashbord.fxml");
                } else {
                    showAlert("Incorrect password.", Alert.AlertType.ERROR);
                }
            } else {
                showAlert("User does not exist.", Alert.AlertType.ERROR);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("An error occurred: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void changeScene(ActionEvent event, String fxmlFile) throws Exception {
        Node node = (Node) event.getSource();
        Stage stage = (Stage) node.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }   
    private CSVDataHandler csvHandler = new CSVDataHandler();
    @FXML
    private void handleImportFileAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            csvHandler.importData(selectedFile.getAbsolutePath());
        }
    }
    
    @FXML
    private void handleCleanFileAction(ActionEvent event) {
        csvHandler.preprocessData();
    }
    @FXML
    private void handleSaveToDatabase(ActionEvent event) {
        try {
            CSVDataHandler dataHandler = new CSVDataHandler();
            
            dataHandler.importData("/path/to/your/input_data.csv");
            dataHandler.preprocessData();
            dataHandler.saveToDatabase();
            
            showAlert("Success", "Data saved successfully to the database.", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            
            showAlert("Error", "Failed to save data to the database: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);  
        alert.setContentText(content);
        alert.showAndWait();  
    }
    @FXML
    private void handleAnalyzeFileAction(ActionEvent event) throws Exception {
        changeScene(event, "Analysis.fxml"); 
    }


    @FXML
    private void handleDiagnosticAnalysisAction(ActionEvent event) {
        DataAnalyzer analyzer = new DataAnalyzer();
        // Capture correlation and regression output separately
        String correlationResults = captureOutput(() -> {
            try {
                analyzer.calculateCorrelation();
            } catch (SQLException e) {
               
                e.printStackTrace();
            }
        });
        String regressionResults = captureOutput(analyzer::runRegression);
        // Capture date frequency analysis results
        String analysisResults = captureOutput(analyzer::analyzeData);

        showAnalysisResults(correlationResults, regressionResults);
        processAndDisplayResults(analysisResults);
    }
private void showAnalysisResults(String correlationResults, String regressionResults) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Analysis Results");
    alert.setHeaderText(null);
    
    VBox contentBox = new VBox(10);
    contentBox.setPadding(new Insets(10));
    
    // Correlation Results Section
    Label correlationLabel = new Label("Correlation Results:");
    correlationLabel.setStyle("-fx-font-weight: bold");
    TextArea correlationTextArea = new TextArea(correlationResults);
    correlationTextArea.setEditable(false);
    correlationTextArea.setPrefRowCount(3);
    correlationTextArea.setWrapText(true);
    
    // Regression Results Section
    Label regressionLabel = new Label("Regression Equation Coefficients:");
    regressionLabel.setStyle("-fx-font-weight: bold");
    
    TableView<RegressionCoefficient> table = new TableView<>();
    table.setPrefHeight(200);
    
    TableColumn<RegressionCoefficient, String> termColumn = new TableColumn<>("Term");
    termColumn.setCellValueFactory(new PropertyValueFactory<>("term"));
    termColumn.setPrefWidth(200);
    
    TableColumn<RegressionCoefficient, Double> coefficientColumn = new TableColumn<>("Coefficient");
    coefficientColumn.setCellValueFactory(new PropertyValueFactory<>("coefficient"));
    coefficientColumn.setPrefWidth(150);
    
    table.getColumns().addAll(termColumn, coefficientColumn);
    
    // Parse regression results and populate table
    List<RegressionCoefficient> coefficients = parseRegressionResults(regressionResults);
    table.setItems(FXCollections.observableArrayList(coefficients));
    
    contentBox.getChildren().addAll(
        correlationLabel, 
        correlationTextArea,
        regressionLabel,
        table
    );
    
    alert.getDialogPane().setContent(contentBox);
    alert.getDialogPane().setMinWidth(400);
    alert.getDialogPane().setMinHeight(500);
    
    alert.showAndWait();
}
private String captureOutput(Runnable task) {
    PrintStream originalOut = System.out;
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    PrintStream newOut = new PrintStream(bos);
    System.setOut(newOut);
    try {
        task.run();
    } finally {
        System.setOut(originalOut);
        newOut.close();
    }
    return bos.toString();
}

private void processAndDisplayResults(String results) {
    String[] parts = results.split("\n");
    StringBuilder correlationsAndRegressions = new StringBuilder();
    Map<Date, Integer> dateFrequencies = new TreeMap<>();
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    for (String part : parts) {
        if (part.startsWith("Correlation") || part.startsWith("Regression") || part.startsWith("Not enough valid")) {
            correlationsAndRegressions.append(part).append("\n");
        } else if (part.matches("\\d{4}-\\d{2}-\\d{2} -> \\d+ incidents")) {
            try {
                String[] split = part.split(" -> ");
                Date date = dateFormat.parse(split[0]);
                Integer count = Integer.parseInt(split[1].replace(" incidents", ""));
                dateFrequencies.put(date, count);
            } catch (ParseException e) {
                System.err.println("Error parsing date in date frequency analysis: " + e.getMessage());
            }
        }
    }

    showAlerts("Correlation and Regression Analysis", correlationsAndRegressions.toString(), Alert.AlertType.INFORMATION);
    generateChart(dateFrequencies);
}
private List<RegressionCoefficient> parseRegressionResults(String results) {
    List<RegressionCoefficient> coefficients = new ArrayList<>();
    String[] lines = results.split("\n");
    
    for (String line : lines) {
        if (line.startsWith("Intercept:") || line.startsWith("Coefficient for Predictor")) {
            String[] parts = line.split(": ");
            String term = parts[0].trim();
            double value = Double.parseDouble(parts[1].trim());
            
            if (line.startsWith("Coefficient for Predictor")) {
                // Convert "Coefficient for Predictor 1" to more meaningful names
                int predictor = Integer.parseInt(term.replaceAll("\\D+", ""));
                term = getPredictorName(predictor);
            }
            
            coefficients.add(new RegressionCoefficient(term, value));
        }
    }
    return coefficients;
}

private String getPredictorName(int predictor) {
    switch (predictor) {
        case 1: return "Priority";
        case 2: return "Urgency";
        case 3: return "Impact";
        case 4: return "Reopen Count";
        default: return "Predictor " + predictor;
    }
}

// Helper class for regression coefficients
public static class RegressionCoefficient {
    private final String term;
    private final double coefficient;
    
    public RegressionCoefficient(String term, double coefficient) {
        this.term = term;
        this.coefficient = coefficient;
    }
    
    public String getTerm() { return term; }
    public double getCoefficient() { return coefficient; }
}
private void showAlerts(String title, String content, Alert.AlertType type) {
    Alert alert = new Alert(type);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE); // Ensure the alert is large enough
    alert.showAndWait();
}

@Override
public void generateChart(Map<Date, Integer> dateFrequencies) {
    Stage stage = new Stage();
    stage.setTitle("Date Frequency Analysis");

    CategoryAxis xAxis = new CategoryAxis();
    NumberAxis yAxis = new NumberAxis();
    LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
    lineChart.setTitle("Incident Frequency Over Time");

    XYChart.Series<String, Number> series = new XYChart.Series<>();
    series.setName("Daily Incidents");

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    for (Map.Entry<Date, Integer> entry : dateFrequencies.entrySet()) {
        series.getData().add(new XYChart.Data<>(dateFormat.format(entry.getKey()), entry.getValue()));
    }

    lineChart.getData().add(series);
    Scene scene = new Scene(lineChart, 800, 600);
    stage.setScene(scene);
    stage.show();
}


@FXML
    private void handleAnalysisAction() {
        String results = captureSummaryStatistics();
        showResults("Descriptive Analysis Results", results);
    }

    private String captureSummaryStatistics() {
        final PrintStream originalOut = System.out;
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bos));
        try {
            new AbstractDataAnalyzer() {
                @Override
                public void analyzeData() {
                    
                }

                @Override
                public void importData(String filePath) {
                    
                    throw new UnsupportedOperationException("Unimplemented method 'importData'");
                }

                @Override
                public void preprocessData() {
                    
                    throw new UnsupportedOperationException("Unimplemented method 'preprocessData'");
                }
            }.computeSummaryStatistics();
        } finally {
            System.setOut(originalOut);
        }
        return bos.toString();
    }

    private void showResults(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    

    private SLAPredictor slaPredictor;

    @FXML
    private Button predictiveAnalysisButton;

    public MainSceneController() {
        super();
        initializeSLAPredictor();
    }

    private void initializeSLAPredictor() {
        try {
            // Assuming the data file path is predetermined or configured elsewhere
            slaPredictor = new SLAPredictor("ML_data.csv");
        } catch (Exception e) {
            e.printStackTrace();
            showAlertss("Initialization Error", "Failed to initialize SLA Predictor: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * 
     */
    @FXML
    private void handlePredictiveAnalysisAction() {
        try {
            if (slaPredictor != null) {
                
                int reassignmentCount = promptForInput("Enter reassignment_count:");
                int reopenCount = promptForInput("Enter reopen_count:");
                int sysModCount = promptForInput("Enter sys_mod_count:");
                int impact = promptForInput("Enter impact (1-3):");
                int urgency = promptForInput("Enter urgency (1-3):");
                int priority = promptForInput("Enter priority (1-4):");

                // Create instance and predict
                double[] instanceValues = new double[]{reassignmentCount, reopenCount, sysModCount, impact, urgency, priority};
                String predictionResult = slaPredictor.predictSLA(instanceValues);
                String evaluationMetrics = slaPredictor.getEvaluationMetrics();


                // Show result in an alert
                showAlertss("Prediction Result", predictionResult + "\n\n" + evaluationMetrics, Alert.AlertType.INFORMATION);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlertss("Error", "Failed to perform prediction: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private int promptForInput(String promptText) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Input Required");
        dialog.setHeaderText(null);
        dialog.setContentText(promptText);

        Optional<String> result = dialog.showAndWait();
        return result.map(Integer::parseInt).orElse(0); // Default to 0 if no input
    }

    private void showAlertss(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }


    @FXML
private void handleGenerateReportAction() {
    try {
        // Define the output path for the PDF report
        String outputPath = "src/main/resources/IncidentReport.pdf";  // Updated path to use resources folder

        // Create instances of the required classes
        AbstractDataAnalyzer abstractAnalyzer = new DataAnalyzer();  // Use AbstractDataAnalyzer as the base class
        DataAnalyzer detailedAnalyzer = new DataAnalyzer();          // Detailed analysis
        // Create the PDFReportGenerator instance
        ReportGenerator reportGenerator = new ReportGenerator(abstractAnalyzer, detailedAnalyzer, slaPredictor);

        // Generate the PDF report
        reportGenerator.generateReport(outputPath, null);

        // Show a confirmation alert
        showConfirmationAlert("Report Generated", "The PDF report has been generated successfully at: " + outputPath);
    } catch (Exception e) {
        // Show an error alert if something goes wrong
        showErrorAlert("Error Generating Report", "Failed to generate the report: " + e.getMessage());
    }
}

private void showConfirmationAlert(String title, String content) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    alert.showAndWait();
}

private void showErrorAlert(String title, String content) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    alert.showAndWait();
}


}