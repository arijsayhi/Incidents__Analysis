import weka.core.*;
import weka.core.converters.CSVLoader;
import weka.classifiers.trees.REPTree;
import weka.classifiers.Evaluation;
import java.io.File;
import java.util.Random;

public class SLAPredictor {
    
    private final REPTree tree;
    private final Instances data;
    private String evaluationMetrics;

    public SLAPredictor(String filePath) throws Exception {
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File(filePath));
        data = loader.getDataSet();
        data.setClassIndex(data.numAttributes() - 1);

        tree = new REPTree();
        tree.buildClassifier(data);

        Evaluation evaluation = new Evaluation(data);
        evaluation.crossValidateModel(tree, data, 10, new Random(1));

        // Store the evaluation metrics
        evaluationMetrics = "Correlation Coefficient: " + evaluation.correlationCoefficient()
                          + "\nMean Absolute Error: " + evaluation.meanAbsoluteError()
                          + "\nRoot Mean Squared Error: " + evaluation.rootMeanSquaredError();
    }

    public String getEvaluationMetrics() {
        return evaluationMetrics;
    }

    public String predictSLA(double[] instanceValues) throws Exception {
        Instance newInstance = new DenseInstance(1.0, instanceValues);
        Instances newData = new Instances(data, 0);
        newData.add(newInstance);
        newData.setClassIndex(data.numAttributes() - 1);

        double prediction = tree.classifyInstance(newData.firstInstance());
        return (prediction >= 0.5) ? "SLA Violation" : "NO SLA Violation";
    }
}
