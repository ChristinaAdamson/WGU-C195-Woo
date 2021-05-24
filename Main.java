import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * This Main class initializes the Appointment Manager Software
 * @author Woo Jin An
 */
public class Main extends Application{

    public static void main(String[] args){
        launch(args);
    }

    /**
     * Opens the Login Screen for Authentication to the Software
     * @param mainStage JavaFX Stage object inputted by JavaFX launch()
     * @throws Exception Throws Exception for FMLLoader
     */
    @Override
    public void start(Stage mainStage) throws Exception {

        try {
            Scene mainScene;
            Parent loginForm = FXMLLoader.load(getClass().getResource("LoginScreen.fxml"));
            mainScene = new Scene(loginForm);
            mainStage.setScene(mainScene);
            if (System.getProperty("user.language").equals("fr")) {
                mainStage.setTitle("système de connexion");
            }
            else{
                mainStage.setTitle("Login System");
            }
            mainStage.show();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
