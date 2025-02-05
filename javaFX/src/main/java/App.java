import java.sql.SQLException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("login.fxml")); 
        primaryStage.setScene(new Scene(root, 679, 503)); 
        primaryStage.show();
    }

    public static void main(String[] args) throws SQLException {
        launch(args);

    }
}

