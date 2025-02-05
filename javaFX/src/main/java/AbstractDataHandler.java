public abstract class AbstractDataHandler implements DataOperations {

    public void validateFileFormat(String fileName) {
        if (!fileName.endsWith(".csv")) {
            throw new IllegalArgumentException("Invalid file format. Only CSV files are supported.");
        }
    }

    public abstract void importData(String filePath);
    public abstract void preprocessData();
}
