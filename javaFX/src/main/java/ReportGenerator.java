import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.io.ByteArrayOutputStream;

public class ReportGenerator extends DataVisualizer {
    private final AbstractDataAnalyzer dataAnalyzer;
    private final DataAnalyzer detailedAnalyzer;

    public ReportGenerator(AbstractDataAnalyzer dataAnalyzer, DataAnalyzer detailedAnalyzer, SLAPredictor slaPredictor) {
        this.dataAnalyzer = dataAnalyzer;
        this.detailedAnalyzer = detailedAnalyzer;
    }

    @Override
    public void generateReport(String filePath, Map<Date, Integer> dateFrequencies) {
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // Add title
            document.add(new Paragraph("Incident Analysis Report"));
            document.add(new Paragraph("\n"));

            // Capture Summary Statistics
            document.add(new Paragraph("Summary Statistics:"));
            String summaryStats = captureOutput(() -> dataAnalyzer.computeSummaryStatistics());
            document.add(new Paragraph(summaryStats));
            document.add(new Paragraph("\n"));

            // Capture Correlation Analysis
            document.add(new Paragraph("Correlation Analysis:"));
            String correlationAnalysis = captureOutput(() -> {
                try {
                    detailedAnalyzer.calculateCorrelation();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
            document.add(new Paragraph(correlationAnalysis));
            document.add(new Paragraph("\n"));

            // Capture Regression Analysis
            document.add(new Paragraph("Regression Analysis:"));
            String regressionAnalysis = captureOutput(() -> detailedAnalyzer.runRegression());
            document.add(new Paragraph(regressionAnalysis));
            document.add(new Paragraph("\n"));

            // Capture Date Frequency Analysis
            document.add(new Paragraph("Date Frequency Analysis:"));
            try {
                String dateFrequency = captureOutput(() -> {
                    try {
                        detailedAnalyzer.analyzeDateFrequency();
                    } catch (SQLException | ParseException e) {
                        e.printStackTrace();
                    }
                });
                document.add(new Paragraph(dateFrequency));
            } catch (Exception e) {
                document.add(new Paragraph("Error in date frequency analysis: " + e.getMessage()));
            }
            document.add(new Paragraph("\n"));

            // Generate and add the table to the PDF
            if (dateFrequencies != null && !dateFrequencies.isEmpty()) {
                PdfPTable table = generateDataTable(dateFrequencies);
                document.add(table);
            }

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        } finally {
            document.close();
        }
    }

    private PdfPTable generateDataTable(Map<Date, Integer> dateFrequencies) {
        // Create a PDF table with two columns
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        // Add column headers
        table.addCell("Date");
        table.addCell("Frequency");

        // Formatting date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        // Add data rows
        for (Map.Entry<Date, Integer> entry : dateFrequencies.entrySet()) {
            table.addCell(dateFormat.format(entry.getKey()));
            table.addCell(entry.getValue().toString());
        }

        return table;
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
}
